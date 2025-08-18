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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CacheMap_CategoryPartition_Test {

    private CacheMap cacheMap;

    @Before
    public void setup() {
        // Cache map con impostazioni di default
        // Non LRU e con dimensione 1000
        cacheMap = new CacheMap();
    }

    /* -- Metodo: get -- */

    // Key: presente
    @Test
    public void testCacheMap_get_01() {
        // Chiave valida
        String existentKey = "existentKey";
        Object validValue = new Object();
        cacheMap.put(existentKey, validValue);
        Object result = cacheMap.get(existentKey);
        Assert.assertSame("Object expected to be the same", validValue, result);
    }

    // Key: non presente
    @Test
    public void testCacheMap_get_02() {
        // Chiave valida
        String nonExistentKey = "nonExistentKey";
        Object result = cacheMap.get(nonExistentKey);
        Assert.assertNull("Object expected to be null", result);
    }

    // Key: null
    @Test
    public void testCacheMap_get_03() {
        // Chiave valida
        Object result = cacheMap.get(null);
        Assert.assertNull("Object expected to be null", result);
    }

    /* -- Metodo: put -- */

    // Key: valido (non nullo)
    // Value: valido (non nullo)
    @Test
    public void testCacheMap_put_01() {
        // Chiave e valore validi
        Object validKey = new Object();
        Object validValue = new Object();
        cacheMap.put(validKey, validValue);
        Object result = cacheMap.get(validKey);
        Assert.assertSame("Object expected to be the same", validValue, result);
    }

    // Key: valido (non nullo)
    // Value: nullo
    @Test
    public void testCacheMap_put_02() {
        // Chiave valida, valore nullo
        Object validKey = new Object();
        cacheMap.put(validKey, null);
        Object result = cacheMap.get(validKey);
        Assert.assertNull("Object expected to be null", result);
    }

    // Key: nullo
    // Value: valido (non nullo)
    @Test
    public void testCacheMap_put_03() {
        // Chiave nulla, valore valido
        Object validValue = new Object();
        cacheMap.put(null, validValue);
        Object result = cacheMap.get(null);
        Assert.assertSame("Object expected to be the same", validValue, result);
    }

    // Key: nullo
    // Value: nullo
    @Test
    public void testCacheMap_put_04() {
        // Chiave e valore nulli
        cacheMap.put(null, null);
        Object result = cacheMap.get(null);
        Assert.assertNull("Object expected to be null", result);
    }

    /* -- Metodo: putAll -- */

    // map: vuota
    // replaceExisting: false
    @Test
    public void testCacheMap_putAll_01() {
        // Mappa vuota
        Map<Object, Object> emptyMap = Mockito.mock(Map.class);
        Mockito.lenient().when(emptyMap.isEmpty()).thenReturn(true);
        cacheMap.putAll(emptyMap, false);
        Assert.assertTrue("CacheMap should be empty", cacheMap.isEmpty());
    }

    // map: piccola
    // replaceExisting: false
    @Test
    public void testCacheMap_putAll_02() {
        // Mappa piccola con un elemento
        Map<Object, Object> smallMap = Mockito.mock(Map.class);

        Object key = new Object();
        Object value = new Object();
        Mockito.lenient().when(smallMap.size()).thenReturn(1);
        Mockito.lenient().when(smallMap.get(key)).thenReturn(value);
        Mockito.lenient().when(smallMap.containsKey(key)).thenReturn(true);
        Mockito.lenient().when(smallMap.isEmpty()).thenReturn(false);
        Mockito.lenient().when(smallMap.entrySet()).thenReturn(Collections.singletonMap(key, value).entrySet());

        cacheMap.putAll(smallMap, true);
        Assert.assertEquals("CacheMap size should be 1", 1, cacheMap.size());
        Assert.assertSame("Object expected to be the same", value, cacheMap.get(key));

        // Verifica che il valore sia stato aggiornato
        Object newValue = new Object();
        Mockito.lenient().when(smallMap.get(key)).thenReturn(newValue);
        Mockito.when(smallMap.entrySet()).thenReturn(Collections.singletonMap(key, newValue).entrySet());

        cacheMap.putAll(smallMap, true);
        Assert.assertEquals("CacheMap size should be 1", 1, cacheMap.size());
        Assert.assertSame("Object expected to be the same", newValue, cacheMap.get(key));
    }

    // map: grande
    // replaceExisting: false
    @Test
    public void testCacheMap_putAll_03() {
        // Mappa grande con più elementi
        int mapSize = 10000;
        Map<Object, Object> largeMap = new HashMap<>();

        Object[] keys = new Object[mapSize];
        Object[] values = new Object[mapSize];

        for (int i = 0; i < mapSize; i++) {
            keys[i] = new Object();
            values[i] = new Object();
            largeMap.put(keys[i], values[i]);
        }

        cacheMap.putAll(largeMap, false);

        Assert.assertEquals("CacheMap size should be " + mapSize, mapSize, cacheMap.size());
        for (int i = 0; i < mapSize; i++) {
            Assert.assertSame("Object expected to be the same", values[i], cacheMap.get(keys[i]));
        }
    }

    // map: CacheMap
    // replaceExisting: true
    @Test
    public void testCacheMap_putAll_04() {
        // Mappa di tipo CacheMap
        CacheMap cacheMapToPut = new CacheMap();
        Object key = new Object();
        Object value = new Object();
        cacheMapToPut.put(key, value);
        cacheMap.putAll(cacheMapToPut, true);
        Assert.assertEquals("CacheMap size should be 1", 1, cacheMap.size());
        Assert.assertSame("Object expected to be the same", value, cacheMap.get(key));
    }

    // map: null
    // replaceExisting: false
    /*
     * Test per verificare il comportamento quando la mappa da aggiungere è null.
     * In questo caso, non dovrebbe causare errori e la CacheMap dovrebbe rimanere vuota.
     */
    /*@Test
    public void testCacheMap_putAll_05() {
        // Mappa nulla
        cacheMap.putAll(null, false);
        Assert.assertTrue("CacheMap should be empty", cacheMap.isEmpty());
    }*/

    /* -- Metodo: remove -- */

    // Key: presente
    @Test
    public void testCacheMap_remove_01() {
        // Chiave valida
        String existentKey = "existentKey";
        Object validValue = new Object();
        cacheMap.put(existentKey, validValue);
        Object removedValue = cacheMap.remove(existentKey);
        Assert.assertSame("Object expected to be the same", validValue, removedValue);
        Assert.assertNull("Object expected to be null after removal", cacheMap.get(existentKey));
        Assert.assertFalse("CacheMap should not contain the key after removal", cacheMap.containsKey(existentKey));
        Assert.assertTrue("CacheMap should be empty after removal", cacheMap.isEmpty());
    }

    // Key: non presente
    @Test
    public void testCacheMap_remove_02() {
        // Chiave non presente
        String nonExistentKey = "nonExistentKey";
        Object removedValue = cacheMap.remove(nonExistentKey);
        Assert.assertNull("Object expected to be null when removing non-existent key", removedValue);
        Assert.assertFalse("CacheMap should not contain the key", cacheMap.containsKey(nonExistentKey));
        Assert.assertTrue("CacheMap should be empty after removal", cacheMap.isEmpty());
    }

    // Key: null
    @Test
    public void testCacheMap_remove_03() {
        // Chiave nulla
        Object removedValue = cacheMap.remove(null);
        Assert.assertNull("Object expected to be null when removing null key", removedValue);
        Assert.assertFalse("CacheMap should not contain null key", cacheMap.containsKey(null));
        Assert.assertTrue("CacheMap should be empty after removal", cacheMap.isEmpty());
    }
}
