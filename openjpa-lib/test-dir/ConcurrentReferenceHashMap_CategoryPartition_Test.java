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

public class ConcurrentReferenceHashMap_CategoryPartition_Test {

    private ConcurrentReferenceHashMap concurrentReferenceHashMap;

    @Before
    public void setup() {
        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
    }

    /* -- Metodo: get -- */
    
    // key: presente
    @Test
    public void testConcurrentReferenceHashMap_get_01() {
        String key = "k1";
        String value = "v1";
        concurrentReferenceHashMap.put(key, value);
        Object result = concurrentReferenceHashMap.get(key);
        Assert.assertEquals("Value should match expected", value, result);
    }

    // key: non presente
    @Test
    public void testConcurrentReferenceHashMap_get_02() {
        String nonExistentKey = "nonExistentKey";
        Object result = concurrentReferenceHashMap.get(nonExistentKey);
        Assert.assertNull("Object expected to be null", result);
    }

    // key: null con keyType=HARD
    @Test
    public void testConcurrentReferenceHashMap_get_03() {
        String value = "vnull";
        concurrentReferenceHashMap.put(null, value);
        Object result = concurrentReferenceHashMap.get(null);
        Assert.assertEquals("Value should match expected with null key and keyType=HARD", value, result);
    }
    
    // key: null con keyType≠HARD
    @Test
    public void testConcurrentReferenceHashMap_get_04() {
        ConcurrentReferenceHashMap nonHardMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.WEAK,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        Object result = nonHardMap.get(null);
        Assert.assertNull("With keyType≠HARD, get(null) should return null", result);
    }

    /* -- Metodo: put -- */
    
    // key: valido
    // value: valido
    @Test
    public void testConcurrentReferenceHashMap_put_01() {
        String key = "k2";
        String value = "v2";
        concurrentReferenceHashMap.put(key, value);
        Assert.assertEquals("Value should be inserted correctly", value, concurrentReferenceHashMap.get(key));
    }

    // key: null con keyType=HARD
    @Test
    public void testConcurrentReferenceHashMap_put_02() {
        String value = "v3";
        concurrentReferenceHashMap.put(null, value);
        Assert.assertEquals("Value should be retrievable with null key and keyType=HARD", value, concurrentReferenceHashMap.get(null));
    }

    // key: null con keyType≠HARD
    // expected: IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConcurrentReferenceHashMap_put_03() {
        ConcurrentReferenceHashMap nonHardMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.WEAK,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        nonHardMap.put(null, "v");
    }

    // key: valido
    // value: null con valueType=HARD
    @Test
    public void testConcurrentReferenceHashMap_put_04() {
        String key = "k4";
        concurrentReferenceHashMap.put(key, null);
        Assert.assertNull("Null value should be accepted with valueType=HARD", concurrentReferenceHashMap.get(key));
    }

    // key: valido
    // value: null con valueType≠HARD
    // expected: IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConcurrentReferenceHashMap_put_05() {
        ConcurrentReferenceHashMap nonHardMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.WEAK
        );
        nonHardMap.put("k", null);
    }

    /* -- Metodo: remove -- */

    // key: presente
    @Test
    public void testConcurrentReferenceHashMap_remove_01() {
        String key = "k5";
        String value = "v5";
        concurrentReferenceHashMap.put(key, value);
        Object removed = concurrentReferenceHashMap.remove(key);
        Assert.assertEquals("Removed value should match expected", value, removed);
        Assert.assertNull("After remove, key should not be present", concurrentReferenceHashMap.get(key));
    }
    
    // key: non presente
    @Test
    public void testConcurrentReferenceHashMap_remove_02() {
        Object removed = concurrentReferenceHashMap.remove("absent");
        Assert.assertNull("Remove on absent key should return null", removed);
    }
    
    // key: null con keyType=HARD
    @Test
    public void testConcurrentReferenceHashMap_remove_03() {
        String value = "v6";
        concurrentReferenceHashMap.put(null, value);
        Object removed = concurrentReferenceHashMap.remove(null);
        Assert.assertEquals("Remove should correctly handle null key with keyType=HARD", value, removed);
    }

    
    // key: null con keyType≠HARD
    @Test
    public void testConcurrentReferenceHashMap_remove_04() {
        ConcurrentReferenceHashMap nonHardMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.WEAK,
                AbstractReferenceMap.ReferenceStrength.HARD
        );
        Object removed = nonHardMap.remove(null);
        Assert.assertNull("With keyType≠HARD, remove(null) should return null", removed);
    }
}