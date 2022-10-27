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

import io.questdb.cairo.sql.TableRecordMetadata;
import io.questdb.cairo.wal.seq.TableRecordMetadataSink;

public class GenericTableRecordMetadata extends GenericRecordMetadata implements TableRecordMetadata, TableRecordMetadataSink {
    private String tableName;
    private int tableId;
    private long structureVersion;

    @Override
    public void addColumn(
            String columnName,
            int columnType,
            long columnHash,
            boolean columnIndexed,
            int indexValueBlockCapacity,
            boolean symbolTableStatic,
            int writerIndex
    ) {
        add(
                new TableColumnMetadata(
                        columnName,
                        columnHash,
                        columnType,
                        columnIndexed,
                        indexValueBlockCapacity,
                        symbolTableStatic,
                        null,
                        writerIndex
                )
        );
    }

    @Override
    public void of(String tableName, int tableId, int timestampIndex, boolean suspended, long structureVersion, int columnCount) {
        this.tableName = tableName;
        this.tableId = tableId;
        this.timestampIndex = timestampIndex;
        // todo: suspended
        this.structureVersion = structureVersion;
        // todo: maxUncommittedRows where from ?
    }

    @Override
    public void close() {
    }

    @Override
    public long getStructureVersion() {
        return structureVersion;
    }

    @Override
    public int getTableId() {
        return tableId;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void toReaderIndexes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWalEnabled() {
        // this class is only used for WAL-enabled tables
        return true;
    }
}
