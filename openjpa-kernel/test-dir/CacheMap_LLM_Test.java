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
package org.apache.openjpa.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.Collection;

public class CacheMap_LLM_Test {

    private CacheMap cacheMap;

    @Before
    public void setup() {
        cacheMap = new CacheMap();
    }

    @Test
    public void testClear() {
        cacheMap.put("a", 1);
        cacheMap.put("b", 2);
        cacheMap.clear();
        Assert.assertTrue(cacheMap.isEmpty());
        Assert.assertNull(cacheMap.get("a"));
        Assert.assertNull(cacheMap.get("b"));
    }

    @Test
    public void testPinAndUnpin() {
        String key = "pinned";
        Integer value = 42;
        cacheMap.put(key, value);
        boolean pinned = cacheMap.pin(key);
        Assert.assertTrue(pinned);
        Set pinnedKeys = cacheMap.getPinnedKeys();
        Assert.assertTrue(pinnedKeys.contains(key));
        boolean unpinned = cacheMap.unpin(key);
        Assert.assertTrue(unpinned);
        Assert.assertFalse(cacheMap.getPinnedKeys().contains(key));
    }

    @Test
    public void testPinNonExistentKey() {
        String key = "notPresent";
        boolean pinned = cacheMap.pin(key);
        Assert.assertFalse(pinned);
        Assert.assertTrue(cacheMap.getPinnedKeys().contains(key));
    }

    @Test
    public void testUnpinNonExistentKey() {
        String key = "notPresent";
        boolean unpinned = cacheMap.unpin(key);
        Assert.assertFalse(unpinned);
    }

    @Test
    public void testSetCacheSize() {
        cacheMap.setCacheSize(1);
        cacheMap.put("a", 1);
        cacheMap.put("b", 2); // dovrebbe causare overflow
        Assert.assertTrue(cacheMap.size() <= 2);
    }

    @Test
    public void testSetSoftReferenceSize() {
        cacheMap.setSoftReferenceSize(1);
        Assert.assertTrue(cacheMap.getSoftReferenceSize() == 1);
    }

    @Test
    public void testIsLRU() {
        CacheMap lruMap = new CacheMap(true);
        Assert.assertTrue(lruMap.isLRU());
        Assert.assertFalse(cacheMap.isLRU());
    }

    @Test
    public void testGetPinnedKeys() {
        cacheMap.put("a", 1);
        cacheMap.pin("a");
        Set pinnedKeys = cacheMap.getPinnedKeys();
        Assert.assertTrue(pinnedKeys.contains("a"));
    }

    @Test
    public void testContainsKeyAndValue() {
        cacheMap.put("a", 1);
        Assert.assertTrue(cacheMap.containsKey("a"));
        Assert.assertTrue(cacheMap.containsValue(1));
        Assert.assertFalse(cacheMap.containsKey("b"));
        Assert.assertFalse(cacheMap.containsValue(2));
    }

    @Test
    public void testSizeAndIsEmpty() {
        Assert.assertTrue(cacheMap.isEmpty());
        cacheMap.put("a", 1);
        Assert.assertEquals(1, cacheMap.size());
        cacheMap.remove("a");
        Assert.assertTrue(cacheMap.isEmpty());
    }

    @Test
    public void testKeySetValuesEntrySet() {
        cacheMap.put("a", 1);
        cacheMap.put("b", 2);
        Set keys = cacheMap.keySet();
        Collection values = cacheMap.values();
        Set entries = cacheMap.entrySet();
        Assert.assertTrue(keys.contains("a"));
        Assert.assertTrue(keys.contains("b"));
        Assert.assertTrue(values.contains(1));
        Assert.assertTrue(values.contains(2));
        Assert.assertEquals(2, entries.size());
    }

    @Test
    public void testToString() {
        cacheMap.put("a", 1);
        String str = cacheMap.toString();
        Assert.assertTrue(str.contains("CacheMap"));
        Assert.assertTrue(str.contains("a"));
    }
}
