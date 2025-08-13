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
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CacheMap_BranchCoverage_Test {

    private CacheMap cacheMap;

    Map<Object, Object> realMap;
    Map<Object, Object> nullMap;

    @Before
    public void setup() {
        // Cache map con impostazioni di default
        // Non LRU e con dimensione 1000
        cacheMap = new CacheMap();

        // Inizializza una mappa reale con chiavi e valori
        realMap = new HashMap<>();
        for(int i = 0; i < 10; i++) {
            realMap.put("key" + i, "value" + i);
        }
        // Aggiungi chiavi e valori nulli
        nullMap = new HashMap<>();
        nullMap.put("keyNull", null);
        nullMap.put(null, "valueNull");
        nullMap.put(null, null);

    }

    // notifyEntryRemovals
    @Test
    public void testCacheMap_branchCoverage_01() {

        cacheMap.putAll(realMap, true);
        cacheMap.putAll(nullMap, true);
        cacheMap.clear();

        // Verifica che la mappa sia vuota
        Assert.assertTrue("CacheMap should be empty after clear", cacheMap.isEmpty());
        Assert.assertEquals("CacheMap size should be 0 after clear", 0, cacheMap.size());
        // Verifica che le chiavi e i valori nulli siano gestiti correttamente
        Assert.assertNull("Value for keyNull should be null", cacheMap.get("keyNull"));
        Assert.assertNull("Value for null key should be null", cacheMap.get(null));
        Assert.assertFalse("CacheMap should not contain null key", cacheMap.containsKey(null));
        Assert.assertFalse("CacheMap should not contain keyNull", cacheMap.containsKey("keyNull"));
    }

    // containsValue
    // containsKey
    @Test
    public void testCacheMap_branchCoverage_02() {
        Object keyCache = "keyCache";
        Object valueCache = "valueCache";
        Object keyPinned = "keyPinned";
        Object valuePinned = "valuePinned";
        Object keySoft = "keySoft";
        Object valueSoft = "valueSoft";

        // Inserimento in cacheMap
        cacheMap.put(keyCache, valueCache);
        Assert.assertTrue(cacheMap.containsKey(keyCache));
        Assert.assertTrue(cacheMap.containsValue(valueCache));

        // Pinning (sposta la chiave in pinnedMap)
        cacheMap.put(keyPinned, valuePinned);
        cacheMap.pin(keyPinned);
        Assert.assertTrue(cacheMap.containsKey(keyPinned));
        Assert.assertTrue(cacheMap.containsValue(valuePinned));

        // Simulazione softMap: rimuovi da cacheMap e inserisci in softMap tramite overflow
        cacheMap.setCacheSize(1); // forza overflow
        cacheMap.put(keySoft, valueSoft); // va in softMap
        Assert.assertTrue(cacheMap.containsKey(keySoft));
        Assert.assertTrue(cacheMap.containsValue(valueSoft));
        // Mi aspetto che la chiave e il valore precedente
        // non siano più in cacheMap ma in softMap
        // (ma sono comunque presenti)
        Assert.assertTrue(cacheMap.containsKey(keyCache));
        Assert.assertTrue(cacheMap.containsValue(valueCache));

        // Verifica chiave/valore non presenti
        Assert.assertFalse(cacheMap.containsKey("notPresent"));
        Assert.assertFalse(cacheMap.containsValue("notPresentValue"));
    }

    // putAll
    @Test
    public void testCacheMap_branchCoverage_03() {
        cacheMap.putAll(realMap);
        cacheMap.putAll(realMap, false);
        cacheMap.putAll(nullMap);
        cacheMap.putAll(nullMap, false);

        // Verifica che la cacheMap contenga le stesse chiavi e valori della realMap
        Assert.assertEquals("CacheMap size should match realMap size", realMap.size() + nullMap.size(), cacheMap.size());

        // Verifica che le chiavi e i valori nulli siano gestiti correttamente
        Assert.assertNull("Value for keyNull should be null", cacheMap.get("keyNull"));
        Assert.assertNull("Value for null key should be null", cacheMap.get(null));
    }

    // remove
    @Test
    public void testCacheMap_branchCoverage_04() {

        // Rimuovi una chiave esistente su valore nullo
        // su una mappa pinnata
        // Nella mappa pinnata una chiave rimossa rimane comunque
        // presente, ma il valore è nullo.
        String keyToRemove = "key0";
        cacheMap.put(keyToRemove, null);
        cacheMap.pin(keyToRemove);

        // Anche se la chiave è presente nella mappa pinnata,
        // essendo il valore nullo il metodo containsKey
        // non restituisce true
        // Assert.assertTrue(cacheMap.containsKey(keyToRemove));
        Assert.assertTrue(cacheMap.containsValue(null));
        Assert.assertTrue(cacheMap.getPinnedKeys().contains(keyToRemove));
        Assert.assertNull("Value for keyNull should be null", cacheMap.get(keyToRemove));
        cacheMap.remove(keyToRemove);

        // Assert.assertTrue(cacheMap.containsKey(keyToRemove));
        Assert.assertTrue(cacheMap.containsValue(null));
        Assert.assertTrue(cacheMap.getPinnedKeys().contains(keyToRemove));
        Assert.assertNull("Value for keyNull should be null", cacheMap.get(keyToRemove));

        // La mappa dovrebbe essere vuota
        // Gli elementi pinnati uguali a null non vengono calcolati
        // come elementi presenti nella cacheMap
        Assert.assertTrue("CacheMap should be empty after removal", cacheMap.isEmpty());
    }

    // setSoftReferenceSize
    @Test
    public void testCacheMap_branchCoverage_05() {

        // Controlla che la mappa non sia vuota inizialmente
        cacheMap.putAll(realMap, true);
        Assert.assertFalse("CacheMap should not be empty before setting soft reference size", cacheMap.isEmpty());

        // Imposta la dimensione della cache
        cacheMap.setCacheSize(100);
        Assert.assertFalse("CacheMap should not be empty after setting cache size", cacheMap.isEmpty());

        // Imposta la dimensione della cache
        cacheMap.setCacheSize(1);
        Assert.assertFalse("CacheMap should not be empty after setting cache size", cacheMap.isEmpty());

        // Imposta la dimensione delle soft reference
        cacheMap.setSoftReferenceSize(-10);
        Assert.assertEquals("Soft reference size should be -1 (no limit)", -1, cacheMap.getSoftReferenceSize());

        // Imposta la dimensione delle soft reference
        cacheMap.setSoftReferenceSize(5);
        Assert.assertEquals("Soft reference size should be 5", 5, cacheMap.getSoftReferenceSize());

        // Controlla che la mappa completa abbia la dimensione corretta
        Assert.assertEquals("CacheMap size should be 5+1", 6, cacheMap.size());

    }

    // put
    @Test
    public void testCacheMap_branchCoverage_06() {
        // Inserimento di una chiave e valore
        cacheMap.setCacheSize(0);
        String key = "key1";
        String value = "value1";
        // Non mi aspetto che la chiave sia presente
        cacheMap.put(key, value);
        Assert.assertFalse("CacheMap should not contain key after put with cache size 0", cacheMap.containsKey(key));
        Assert.assertFalse("CacheMap should not contain value after put with cache size 0", cacheMap.containsValue(value));
        // Verifica che la mappa sia vuota
        Assert.assertTrue("CacheMap should be empty after put with cache size 0", cacheMap.isEmpty());
    }

    // pin
    @Test
    public void testCacheMap_branchCoverage_07() {
        // Pinning di una chiave presente
        String keyExistent = "keyExistent";
        String valueExistent = "valueExistent";
        cacheMap.put(keyExistent, valueExistent);
        boolean pinned = cacheMap.pin(keyExistent);
        Assert.assertTrue("Key should be pinned", pinned);
        Assert.assertTrue("CacheMap should contain pinned key", cacheMap.getPinnedKeys().contains(keyExistent));
        Assert.assertTrue("CacheMap should contain value for pinned key", cacheMap.containsValue(valueExistent));
        Assert.assertTrue("CacheMap should contain key after pinning", cacheMap.containsKey(keyExistent));

        // Prova a fare il pinning di una chiave che è già pinnata
        boolean alreadyPinned = cacheMap.pin(keyExistent);
        Assert.assertTrue("Key should be already pinned", alreadyPinned);

        // Prova a fare nuovamente il pinning su un elemento null
        cacheMap.put(keyExistent, null);
        boolean pinnedNull = cacheMap.pin(keyExistent);
        Assert.assertFalse("Key with null value should not be pinned", pinnedNull);
    }

    // cacheMapOverflowRemoved
    @Test
    public void testCacheMap_branchCoverage_08() {

        // Controlla che la mappa non sia vuota inizialmente
        cacheMap.putAll(realMap, true);
        Assert.assertFalse("CacheMap should not be empty before setting soft reference size", cacheMap.isEmpty());

        // Imposta la dimensione delle soft reference
        cacheMap.setSoftReferenceSize(5);
        Assert.assertEquals("Soft reference size should be 5", 5, cacheMap.getSoftReferenceSize());

        // Imposta la dimensione della cache
        cacheMap.setCacheSize(1);
        Assert.assertFalse("CacheMap should not be empty after setting cache size", cacheMap.isEmpty());

        // Controlla che la mappa completa abbia la dimensione corretta
        Assert.assertEquals("CacheMap size should be 5+1", 6, cacheMap.size());

    }

}
