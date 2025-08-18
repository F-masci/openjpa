/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.lib.util.concurrent;

import org.apache.openjpa.lib.util.collections.AbstractReferenceMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentReferenceHashMap_MutationCovergae_Test {

    ConcurrentReferenceHashMap concurrentReferenceHashMap;

    @Before
    public void setup() {
        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
    }

    // line 333
    @Test
    public void testConcurrentReferenceHashMap_mutation_01() {

        String key1 = null;
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.WEAK,
                1,
                0.05f
        );

        concurrentReferenceHashMap.put(key1, value1);
        Assert.assertTrue(concurrentReferenceHashMap.containsKey(key1));
        Assert.assertEquals(1, concurrentReferenceHashMap.size());

        // Verifica il rehash
        Assert.assertEquals(3, concurrentReferenceHashMap.capacity());

        concurrentReferenceHashMap.remove(key1);
        Assert.assertFalse(concurrentReferenceHashMap.containsKey(key1));
        Assert.assertEquals(0, concurrentReferenceHashMap.size());
        Assert.assertNull(concurrentReferenceHashMap.get(key1));

        // Forza il rehash

        concurrentReferenceHashMap.put(key2, value2);
        Assert.assertTrue(concurrentReferenceHashMap.containsKey(key2));
        Assert.assertEquals(1, concurrentReferenceHashMap.size());

        // Verifica il rehash
        Assert.assertEquals(7, concurrentReferenceHashMap.capacity());

        // Verifica che la chiave precedente non esista più
        Assert.assertFalse(concurrentReferenceHashMap.containsKey(key1));
        Assert.assertNull(concurrentReferenceHashMap.get(key1));

    }



}
