/*
 *  _  _ ___ ___     _ _
 * | \| | __/ __| __| | |__
 * | .` | _|\__ \/ _` | '_ \
 * |_|\_|_| |___/\__,_|_.__/
 *
 * Copyright (c) 2014-2015. The NFSdb project and its contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nfsdb.ql.impl;

import com.nfsdb.collections.DirectInputStream;
import com.nfsdb.collections.ObjList;
import com.nfsdb.io.sink.CharSink;
import com.nfsdb.ql.Record;
import com.nfsdb.ql.RecordMetadata;

import java.io.OutputStream;

public class SelectedColumnsRecord extends AbstractRecord {
    private final int reindex[];
    private Record base;

    public SelectedColumnsRecord(RecordMetadata metadata, ObjList<String> names) {
        super(metadata);
        int k = names.size();
        this.reindex = new int[k];

        for (int i = 0; i < k; i++) {
            reindex[i] = metadata.getColumnIndex(names.getQuick(i));
        }
    }

    @Override
    public byte get(int col) {
        return base.get(reindex[col]);
    }

    @Override
    public void getBin(int col, OutputStream s) {
        base.getBin(reindex[col], s);
    }

    @Override
    public DirectInputStream getBin(int col) {
        return base.getBin(reindex[col]);
    }

    @Override
    public boolean getBool(int col) {
        return base.getBool(reindex[col]);
    }

    @Override
    public long getDate(int col) {
        return base.getDate(reindex[col]);
    }

    @Override
    public double getDouble(int col) {
        return base.getDouble(reindex[col]);
    }

    @Override
    public float getFloat(int col) {
        return base.getFloat(reindex[col]);
    }

    @Override
    public CharSequence getFlyweightStr(int col) {
        return base.getFlyweightStr(reindex[col]);
    }

    @Override
    public int getInt(int col) {
        return base.getInt(reindex[col]);
    }

    @Override
    public long getLong(int col) {
        return base.getLong(reindex[col]);
    }

    @Override
    public long getRowId() {
        return base.getRowId();
    }

    @Override
    public short getShort(int col) {
        return base.getShort(reindex[col]);
    }

    @Override
    public CharSequence getStr(int col) {
        return base.getStr(reindex[col]);
    }

    @Override
    public void getStr(int col, CharSink sink) {
        base.getStr(reindex[col], sink);
    }

    @Override
    public String getSym(int col) {
        return base.getSym(reindex[col]);
    }

    public void setBase(Record base) {
        this.base = base;
    }
}
