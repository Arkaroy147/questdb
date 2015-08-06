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

package com.nfsdb;

import com.nfsdb.factory.configuration.JournalConfigurationBuilder;
import com.nfsdb.model.SubQuote;
import com.nfsdb.test.tools.JournalTestFactory;
import com.nfsdb.utils.Files;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class SubclassTest {

    @Rule
    public final JournalTestFactory factory = new JournalTestFactory(new JournalConfigurationBuilder().build(Files.makeTempDir()));

    @Test
    public void testSubclass() throws Exception {

        JournalWriter<SubQuote> w = factory.writer(SubQuote.class);

        SubQuote q = new SubQuote().setType((byte) 10);
        q.setTimestamp(System.currentTimeMillis());
        q.setSym("ABC");

        w.append(q);

        SubQuote q2 = w.read(0);
        Assert.assertEquals(q.getSym(), q2.getSym());
        Assert.assertEquals(q.getTimestamp(), q2.getTimestamp());
        Assert.assertEquals(q.getType(), q2.getType());
    }
}
