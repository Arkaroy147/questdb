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

import io.questdb.std.FilesFacadeImpl;
import io.questdb.std.str.Path;
import io.questdb.std.str.StringSink;
import io.questdb.test.tools.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class TableReaderMetadataTimestampTest extends AbstractCairoTest {

    @Test
    public void testReAddColumn() throws Exception {
        try (TableModel model = CairoTestUtils.getAllTypesModel(configuration, PartitionBy.NONE)) {
            model.timestamp();
            CairoTestUtils.create(model);
        }
        final String expected = "int:INT\n" +
                "short:SHORT\n" +
                "byte:BYTE\n" +
                "double:DOUBLE\n" +
                "float:FLOAT\n" +
                "long:LONG\n" +
                "sym:SYMBOL\n" +
                "bool:BOOLEAN\n" +
                "bin:BINARY\n" +
                "date:DATE\n" +
                "timestamp:TIMESTAMP\n" +
                "str:STRING\n";
        assertThatTimestampRemains((w) -> {
            w.removeColumn("str");
            w.addColumn("str", ColumnType.STRING);
        }, expected, 11, 10, 12);
    }

    @Test
    public void testRemoveColumnAfterTimestamp() throws Exception {
        try (TableModel model = new TableModel(configuration, "all", PartitionBy.NONE)
                .col("int", ColumnType.INT)
                .col("short", ColumnType.SHORT)
                .col("byte", ColumnType.BYTE)
                .col("double", ColumnType.DOUBLE)
                .col("float", ColumnType.FLOAT)
                .timestamp()
                .col("long", ColumnType.LONG)
                .col("str", ColumnType.STRING)
                .col("sym", ColumnType.SYMBOL)
                .col("bool", ColumnType.BOOLEAN)
                .col("bin", ColumnType.BINARY)
                .col("date", ColumnType.DATE)) {

            CairoTestUtils.create(model);
        }

        final String expected = "int:INT\n" +
                "short:SHORT\n" +
                "byte:BYTE\n" +
                "double:DOUBLE\n" +
                "float:FLOAT\n" +
                "timestamp:TIMESTAMP\n" +
                "long:LONG\n" +
                "str:STRING\n" +
                "sym:SYMBOL\n" +
                "bool:BOOLEAN\n" +
                "date:DATE\n";
        assertThatTimestampRemains((w) -> w.removeColumn("bin"), expected, 5, 5, 11);
    }

    @Test
    public void testRemoveColumnBeforeTimestamp() throws Exception {
        try (TableModel model = CairoTestUtils.getAllTypesModel(configuration, PartitionBy.NONE)) {
            model.timestamp();
            CairoTestUtils.create(model);
        }
        final String expected = "int:INT\n" +
                "short:SHORT\n" +
                "byte:BYTE\n" +
                "double:DOUBLE\n" +
                "float:FLOAT\n" +
                "long:LONG\n" +
                "sym:SYMBOL\n" +
                "bool:BOOLEAN\n" +
                "bin:BINARY\n" +
                "date:DATE\n" +
                "timestamp:TIMESTAMP\n";
        assertThatTimestampRemains((w) -> w.removeColumn("str"), expected, 11, 10, 11);
    }

    @Test
    public void testRemoveFirstTimestamp() throws Exception {
        try (TableModel model = new TableModel(configuration, "all", PartitionBy.NONE)
                .timestamp()
                .col("int", ColumnType.INT)
                .col("short", ColumnType.SHORT)
                .col("byte", ColumnType.BYTE)
                .col("double", ColumnType.DOUBLE)
                .col("float", ColumnType.FLOAT)
                .col("long", ColumnType.LONG)
                .col("str", ColumnType.STRING)
                .col("sym", ColumnType.SYMBOL)
                .col("bool", ColumnType.BOOLEAN)
                .col("bin", ColumnType.BINARY)
                .col("date", ColumnType.DATE)) {

            CairoTestUtils.create(model);
        }
        final String expected = "int:INT\n" +
                "short:SHORT\n" +
                "byte:BYTE\n" +
                "double:DOUBLE\n" +
                "float:FLOAT\n" +
                "long:LONG\n" +
                "str:STRING\n" +
                "sym:SYMBOL\n" +
                "bool:BOOLEAN\n" +
                "bin:BINARY\n" +
                "date:DATE\n";
        assertThat(expected, 0);
    }

    @Test
    public void testRemoveMiddleTimestamp() throws Exception {
        try (TableModel model = new TableModel(configuration, "all", PartitionBy.NONE)
                .col("int", ColumnType.INT)
                .col("short", ColumnType.SHORT)
                .col("byte", ColumnType.BYTE)
                .col("double", ColumnType.DOUBLE)
                .col("float", ColumnType.FLOAT)
                .timestamp()
                .col("long", ColumnType.LONG)
                .col("str", ColumnType.STRING)
                .col("sym", ColumnType.SYMBOL)
                .col("bool", ColumnType.BOOLEAN)
                .col("bin", ColumnType.BINARY)
                .col("date", ColumnType.DATE)) {

            CairoTestUtils.create(model);
        }

        final String expected = "int:INT\n" +
                "short:SHORT\n" +
                "byte:BYTE\n" +
                "double:DOUBLE\n" +
                "float:FLOAT\n" +
                "long:LONG\n" +
                "str:STRING\n" +
                "sym:SYMBOL\n" +
                "bool:BOOLEAN\n" +
                "bin:BINARY\n" +
                "date:DATE\n";

        assertThat(expected, 5);
    }

    @Test
    public void testRemoveTailTimestamp() throws Exception {
        try (TableModel model = CairoTestUtils.getAllTypesModel(configuration, PartitionBy.NONE)
                .timestamp()) {
            CairoTestUtils.create(model);
        }
        final String expected = "int:INT\n" +
                "short:SHORT\n" +
                "byte:BYTE\n" +
                "double:DOUBLE\n" +
                "float:FLOAT\n" +
                "long:LONG\n" +
                "str:STRING\n" +
                "sym:SYMBOL\n" +
                "bool:BOOLEAN\n" +
                "bin:BINARY\n" +
                "date:DATE\n";
        assertThat(expected, 11);
    }

    private static Path getMetaFilePath(final CharSequence root, final CharSequence tableName) {
        CharSequence fileSystemName = engine.getFileSystemName(tableName);
        return new Path().of(root).concat(fileSystemName).concat(TableUtils.META_FILE_NAME).$();
    }


    private void assertThat(String expected, int expectedInitialTimestampIndex) throws Exception {
        int columnCount = 11;
        TestUtils.assertMemoryLeak(() -> {
            String tableName = "all";
            try (Path path = getMetaFilePath(root, tableName)) {
                try (TableReaderMetadata metadata = new TableReaderMetadata(FilesFacadeImpl.INSTANCE, tableName, path)) {

                    Assert.assertEquals(12, metadata.getColumnCount());
                    Assert.assertEquals(expectedInitialTimestampIndex, metadata.getTimestampIndex());
                    long structureVersion;
                    try (TableWriter writer = new TableWriter(configuration, tableName, metrics)) {
                        writer.removeColumn("timestamp");
                        structureVersion = writer.getStructureVersion();
                    }

                    long pTransitionIndex = metadata.createTransitionIndex(structureVersion);
                    StringSink sink = new StringSink();
                    try {
                        metadata.applyTransitionIndex();
                        Assert.assertEquals(columnCount, metadata.getColumnCount());
                        for (int i = 0; i < columnCount; i++) {
                            sink.put(metadata.getColumnName(i)).put(':').put(ColumnType.nameOf(metadata.getColumnType(i))).put('\n');
                        }

                        TestUtils.assertEquals(expected, sink);
                        Assert.assertEquals(-1, metadata.getTimestampIndex());
                    } finally {
                        TableUtils.freeTransitionIndex(pTransitionIndex);
                    }
                }
            }
        });
    }

    private void assertThatTimestampRemains(TableReaderMetadataTest.ColumnManipulator manipulator,
                                            String expected,
                                            int expectedInitialTimestampIndex,
                                            int expectedFinalTimestampIndex,
                                            int expectedColumnCount) throws Exception {
        TestUtils.assertMemoryLeak(() -> {
            String tableName = "all";
            try (Path path = getMetaFilePath(root, tableName)) {
                try (TableReaderMetadata metadata = new TableReaderMetadata(FilesFacadeImpl.INSTANCE, tableName, path)) {

                    Assert.assertEquals(12, metadata.getColumnCount());
                    Assert.assertEquals(expectedInitialTimestampIndex, metadata.getTimestampIndex());
                    long structVersion;
                    try (TableWriter writer = new TableWriter(configuration, tableName, metrics)) {
                        manipulator.restructure(writer);
                        structVersion = writer.getStructureVersion();
                    }

                    long address = metadata.createTransitionIndex(structVersion);
                    StringSink sink = new StringSink();
                    try {
                        metadata.applyTransitionIndex();
                        Assert.assertEquals(expectedColumnCount, metadata.getColumnCount());
                        for (int i = 0; i < expectedColumnCount; i++) {
                            sink.put(metadata.getColumnName(i)).put(':').put(ColumnType.nameOf(metadata.getColumnType(i))).put('\n');
                        }

                        TestUtils.assertEquals(expected, sink);
                        Assert.assertEquals(expectedFinalTimestampIndex, metadata.getTimestampIndex());
                    } finally {
                        TableUtils.freeTransitionIndex(address);
                    }
                }
            }
        });
    }
}
