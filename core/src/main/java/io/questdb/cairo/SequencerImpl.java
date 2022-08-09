/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2022 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cairo;

import io.questdb.griffin.SqlException;
import io.questdb.griffin.engine.ops.AlterOperation;
import io.questdb.std.FilesFacade;
import io.questdb.std.Misc;
import io.questdb.std.SimpleReadWriteLock;
import io.questdb.std.str.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReadWriteLock;

import static io.questdb.cairo.TableUtils.WAL_INDEX_FILE_NAME;

public class SequencerImpl implements Sequencer {

    private final ReadWriteLock schemaLock = new SimpleReadWriteLock();
    private final CairoEngine engine;
    private final String tableName;
    private final int rootLen;
    private final SequencerMetadata metadata;
    private final TxnCatalog catalog;
    private final IDGenerator walIdGenerator;
    private final Path path;
    private final BinaryAlterFormatter alterCommandWalFormatter = new BinaryAlterFormatter();
    private final SequencerMetadataUpdater sequencerMetadataUpdater;

    SequencerImpl(CairoEngine engine, String tableName) {
        this.engine = engine;
        this.tableName = tableName;

        final CairoConfiguration configuration = engine.getConfiguration();
        final FilesFacade ff = configuration.getFilesFacade();
        try {
            path = new Path().of(configuration.getRoot()).concat(tableName).concat(SEQ_DIR);
            rootLen = path.length();

            createSequencerDir(ff, configuration.getMkDirMode());
            metadata = new SequencerMetadata(ff);
            sequencerMetadataUpdater = new SequencerMetadataUpdater(metadata, tableName);

            walIdGenerator = new IDGenerator(configuration, WAL_INDEX_FILE_NAME);
            walIdGenerator.open(path);
            catalog = new TxnCatalog(ff);
            catalog.open(path);
        } catch (Throwable th) {
            close();
            throw th;
        }
    }

    public void abortClose() {
        schemaLock.writeLock().lock();
        try {
            metadata.abortClose();
            catalog.abortClose();
            Misc.free(walIdGenerator);
            Misc.free(path);
        } finally {
            schemaLock.writeLock().unlock();
        }
    }

    @Override
    public void copyMetadataTo(@NotNull SequencerMetadata copyTo) {
        schemaLock.readLock().lock();
        try {
            copyTo.copyFrom(metadata);
        } finally {
            schemaLock.readLock().unlock();
        }
    }

    @Override
    public int getTableId() {
        return metadata.getTableId();
    }

    @Override
    public long nextStructureTxn(long expectedSchemaVersion, AlterOperation operation) {
        // Writing to Sequencer can happen from multiple threads, so we need to protect against concurrent writes.
        schemaLock.writeLock().lock();
        long txn;
        try {
            if (metadata.getStructureVersion() == expectedSchemaVersion) {
                txn = catalog.addMetadataChangeEntry(expectedSchemaVersion + 1, alterCommandWalFormatter, operation);
                try {
                    applyToMetadata(operation);
                    assert metadata.getStructureVersion() == expectedSchemaVersion + 1;
                } catch (Throwable th) {
                    // TODO: handle errors in updating the metadata by destroying the sequencer and safe reload from txn catalog
                    throw th;
                }
            } else {
                return NO_TXN;
            }
        } finally {
            schemaLock.writeLock().unlock();
        }
        engine.notifyWalTxnCommitted(metadata.getTableId(), tableName, txn);
        return txn;
    }

    @Override
    @NotNull
    public SequencerStructureChangeCursor getStructureChangeCursor(
            @Nullable SequencerStructureChangeCursor reusableCursor,
            long fromSchemaVersion
    ) {
        return catalog.getStructureChangeCursor(reusableCursor, fromSchemaVersion, alterCommandWalFormatter);
    }

    @Override
    public void open() {
        metadata.open(tableName, path, rootLen);
    }

    @Override
    public long nextTxn(long expectedSchemaVersion, int walId, long segmentId, long segmentTxn) {
        // Writing to Sequencer can happen from multiple threads, so we need to protect against concurrent writes.
        schemaLock.writeLock().lock();
        long txn;
        try {
            if (metadata.getStructureVersion() == expectedSchemaVersion) {
                txn = nextTxn(walId, segmentId, segmentTxn);
            } else {
                return NO_TXN;
            }
        } finally {
            schemaLock.writeLock().unlock();
        }
        engine.notifyWalTxnCommitted(metadata.getTableId(), tableName, txn);
        return txn;
    }

    private void applyToMetadata(AlterOperation operation) {
        try {
            operation.apply(sequencerMetadataUpdater, true);
        } catch (SqlException e) {
            throw CairoException.instance(0).put("error applying alter command to sequencer metadata [error=").put(e.getFlyweightMessage()).put(']');
        }
    }

    @Override
    public WalWriter createWal() {
        return new WalWriter(tableName, (int) walIdGenerator.getNextId(), this, engine.getConfiguration());
    }

    @Override
    public SequencerCursor getCursor(long lastCommittedTxn) {
        return catalog.getCursor(lastCommittedTxn);
    }

    @Override
    public void close() {
        schemaLock.writeLock().lock();
        try {
            Misc.free(metadata);
            Misc.free(catalog);
            Misc.free(walIdGenerator);
            Misc.free(path);
        } finally {
            schemaLock.writeLock().unlock();
        }
    }

    private void createSequencerDir(FilesFacade ff, int mkDirMode) {
        if (ff.mkdirs(path.slash$(), mkDirMode) != 0) {
            throw CairoException.instance(ff.errno()).put("Cannot create sequencer directory: ").put(path);
        }
        path.trimTo(rootLen);
    }

    private long nextTxn(int walId, long segmentId, long segmentTxn) {
        return catalog.addEntry(walId, segmentId, segmentTxn);
    }

    void create(int tableId, TableStructure model) {
        schemaLock.writeLock().lock();
        try {
            metadata.create(model, tableName, path, rootLen, tableId);
        } finally {
            schemaLock.writeLock().unlock();
        }
    }
}
