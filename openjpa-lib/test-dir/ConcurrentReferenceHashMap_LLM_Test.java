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
import org.junit.*;
import java.util.*;

public class ConcurrentReferenceHashMap_LLM_Test {

    @Test
    public void testCloneAndEquals() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        map.put("a", "b");
        ConcurrentReferenceHashMap clone = (ConcurrentReferenceHashMap) map.clone();
        Assert.assertEquals(map.size(), clone.size());
        Assert.assertEquals(map.get("a"), clone.get("a"));
    }

    @Test
    public void testRemoveExpiredAndValueExpired() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.WEAK,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        map.put("x", "y");
        map.valueExpired("x"); // dovrebbe non fare nulla
        map.removeExpired();   // dovrebbe non rimuovere nulla
        Assert.assertEquals("y", map.get("x"));
    }

    @Test
    public void testKeyExpired() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        map.put("k", "v");
        map.keyExpired("k"); // dovrebbe non fare nulla
        Assert.assertEquals("v", map.get("k"));
    }

    @Test
    public void testSetMaxSizeNegative() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        map.setMaxSize(-1);
        Assert.assertEquals(Integer.MAX_VALUE, map.getMaxSize());
    }

    @Test
    public void testRandomEntryIteratorEmpty() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        Iterator<?> it = map.randomEntryIterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorRemove() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        map.put("a", "b");
        Iterator<?> it = map.keySet().iterator();
        Assert.assertTrue(it.hasNext());
        it.next();
        it.remove();
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testPutAllWithNullMap() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        try {
            map.putAll(null);
            Assert.fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testRemoveMappingNotEntry() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        Object result = map.entrySet().remove("notAnEntry");
        Assert.assertFalse((Boolean) result);
    }
}
