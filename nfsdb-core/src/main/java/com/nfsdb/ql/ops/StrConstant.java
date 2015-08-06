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

package com.nfsdb.ql.ops;

import com.nfsdb.io.sink.CharSink;
import com.nfsdb.ql.Record;
import com.nfsdb.ql.SymFacade;
import com.nfsdb.storage.ColumnType;

public class StrConstant extends AbstractVirtualColumn {
    private final String value;

    public StrConstant(String value) {
        super(ColumnType.STRING);
        this.value = value;
    }

    @Override
    public CharSequence getFlyweightStr(Record rec) {
        return value;
    }

    @Override
    public CharSequence getStr(Record rec) {
        return value;
    }

    @Override
    public void getStr(Record rec, CharSink sink) {
        sink.put(value);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public void prepare(SymFacade facade) {
    }
}
