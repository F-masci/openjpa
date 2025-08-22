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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import org.apache.openjpa.lib.util.collections.AbstractReferenceMap;
import org.apache.openjpa.lib.util.concurrent.ConcurrentHashMap;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap;
import org.junit.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheMap_MutationCoverage_Test {

    private CacheMap cacheMap;

    Map<Object, Object> realMap;
    Map<Object, Object> nullMap;

    private static double[] originalRandoms;

    @BeforeClass
    public static void saveRandoms() throws Exception {
        // Salva i valori random iniziali
        Field randomsField = ConcurrentHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        originalRandoms = Arrays.copyOf((double[]) randomsField.get(null), ((double[]) randomsField.get(null)).length);
    }

    @After
    public void resetRandoms() throws Exception {
        // Ripristina i valori random
        Field randomsField = ConcurrentHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        double[] randoms = (double[]) randomsField.get(null);
        randoms = Arrays.copyOf(originalRandoms, originalRandoms.length);
    }

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

    // mutation line 93
    @Test
    public void testCacheMap_mutationCoverage_01() throws Exception {

        int maxSize = 10;
        boolean lru = false;

        // Crea una CacheMap con maxSize 10
        cacheMap = new CacheMap(lru, maxSize);
        // Verifica che la dimensione della cache sia stata impostata correttamente
        Assert.assertEquals(maxSize, cacheMap.getCacheSize());

        // Ottieni il campo privato "cacheMap" tramite reflection
        Field cacheMapField = CacheMap.class.getDeclaredField("cacheMap");
        cacheMapField.setAccessible(true); // permette di accedere ai privati
        Object internalMap = cacheMapField.get(cacheMap);

        // Verifica che il campo non sia null
        Assert.assertNotNull(internalMap);

        // Ottieni il campo privato "table" tramite reflection
        Field tableField = ConcurrentHashMap.class.getDeclaredField("table");
        tableField.setAccessible(true);

        VarHandle vh = MethodHandles.privateLookupIn(ConcurrentHashMap.class, MethodHandles.lookup())
                .unreflectVarHandle(tableField);

        Map.Entry<?,?>[] internalTable = (Map.Entry<?,?>[]) vh.get(internalMap);

        // Controlla che la dimensione della tabella sia corretta
        // Mi aspetto che la dimensione della tabella sia 8
        // poiché é la potenza di 2 dopo 5 (ovvero maxSize/2)
        Assert.assertEquals(8, internalTable.length);

        // Crea una CacheMap con maxSize 0
        cacheMap = new TestCacheMap(lru, 0, (Object key, Object value) -> {
            // Non dovrebbe essere chiamato
            Assert.fail("Expected no entries to be added when maxSize is 0");
        }, (Object key, Object value, boolean expired) -> {
            // Non dovrebbe essere chiamato
            Assert.fail("Expected no entries to be removed when maxSize is 0");
        });
        // Verifica che la dimensione della cache sia stata impostata a 0
        Assert.assertEquals(0, cacheMap.getCacheSize());
    }

    // mutation line 150
    @Test
    public void testCacheMap_mutationCoverage_02() throws Exception {

        // Crea una CacheMap con maxSize 0
        cacheMap = new TestCacheMap(false, 0, (Object key, Object value) -> {
            // Non dovrebbe essere chiamato
            Assert.fail("Expected no entries to be added when maxSize is 0");
        }, (Object key, Object value, boolean expired) -> {
            // Non dovrebbe essere chiamato
            Assert.fail("Expected no entries to be removed when maxSize is 0");
        });

        // Verifica che la dimensione della cache sia stata impostata a 0
        Assert.assertEquals(0, cacheMap.getCacheSize());

        // Impoasta la dimensione della soft reference a 0
        cacheMap.setSoftReferenceSize(0);
        // Verifica che la dimensione della soft reference sia stata impostata a 0
        Assert.assertEquals(0, cacheMap.getSoftReferenceSize());

        cacheMap.put("key1", "value1");
        // Verifica che la dimensione della cache sia ancora 0
        Assert.assertEquals(0, cacheMap.size());
    }

    // mutation line 159
    /*
     * Va in errore poiché la dimensione della soft reference é 0.
     * Quando viene chiamata la remove, ritorna un errore dovuto a una
     * divisione per zero.
     *
     *  java.lang.ArithmeticException: / by zero
     *      at org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap.remove(ConcurrentReferenceHashMap.java:456)
     *      at org.apache.openjpa.util.CacheMap.remove(CacheMap.java:192)
     *      at org.apache.openjpa.util.CacheMap.put(CacheMap.java:409)
     *
     * Per calcolare l'indice della tabella soft viene usato
     *
     *  int index = (hash & 0x7FFFFFFF) % tab.length;
     *
     * ma tab.length é 0.
     */
    /*@Test
    public void testCacheMap_mutationCoverage_03a() throws Exception {

        // Crea una CacheMap con maxSize 1
        cacheMap = new TestCacheMap(false, 1, (Object key, Object value) -> {
            // Non dovrebbe essere chiamato dato che va in errore prima
            Assert.assertTrue(key.equals("key1") ||  key.equals("key2"));
            Assert.assertTrue(value.equals("value1") || value.equals("value2"));
        }, (Object key, Object value, boolean expired) -> {
            // Non dovrebbe essere chiamato dato che va in errore prima
            Assert.assertEquals("key1", key);
            Assert.assertEquals("value1", value);
            Assert.assertTrue(expired);
        });

        // Verifica che la dimensione della cache sia stata impostata a 1
        Assert.assertEquals(1, cacheMap.getCacheSize());

        // Imposta la dimensione della soft reference a 0
        cacheMap.setSoftReferenceSize(0);
        // Verifica che la dimensione della soft reference sia stata impostata a 0
        Assert.assertEquals(0, cacheMap.getSoftReferenceSize());

        cacheMap.put("key1", "value1");
        cacheMap.put("key2", "value2");
        // Verifica che la dimensione della cache sia ancora 1
        Assert.assertEquals(1, cacheMap.size());
    }*/

    // mutation line 162
    // mutation line 169
    @Test
    public void testCacheMap_mutationCoverage_03b() throws Exception {

        String newKey = "newKey";
        String newValue = "newValue";

        String fullKey1 = "fullKey1";
        String fullValue1 = "fullValue1";
        String fullKey2 = "fullKey2";
        String fullValue2 = "fullValue2";

        AtomicInteger keyDeleted = new AtomicInteger(0);
        AtomicInteger keyAdded = new AtomicInteger(0);

        // Crea una CacheMap con maxSize 9
        cacheMap = new TestCacheMap(false, 9, (Object key, Object value) -> {
            keyAdded.getAndIncrement();
        }, (Object key, Object value, boolean expired) -> {
            keyDeleted.getAndIncrement();
        });

        Assert.assertEquals(9, cacheMap.getCacheSize());
        cacheMap.setSoftReferenceSize(1);
        Assert.assertEquals(1, cacheMap.getSoftReferenceSize());

        cacheMap.putAll(realMap);
        // Verifica che la dimensione della cache sia 10
        Assert.assertEquals(10, cacheMap.size());

        // Aggiungo 3 nuove chiavi e valori
        // Vengono dunque rimosse due chiavi esistenti
        // Il nuovo inserimento rimuove una chiave esistente
        // nelle hard reference.
        // Mi aspetto che la chiave soft venga non rimossa
        cacheMap.put(newKey, newValue);
        cacheMap.put(fullKey1, fullValue1);
        cacheMap.put(fullKey2, fullValue2);

        cacheMap.setSoftReferenceSize(0);
        Assert.assertEquals(0, cacheMap.getSoftReferenceSize());

        // Verifica che le callback siano state chiamate
        // Mi aspetto 13 inserimenti
        Assert.assertEquals(13, keyAdded.get());
        // Mi aspetto che ci siano state 4 chiavi rimosse
        // 1: La chiave nuova che ha rimosso una vecchia chiave
        // 2: La chiave che é stata rimossa dalla soft reference (3 volte)
        Assert.assertEquals(4, keyDeleted.get());
    }

    // mutation line 238
    // mutation line 258
    // mutation line 306
    // mutation line 311
    // mutation line 325
    // mutation line 456
    // mutation line 470
    // mutation line 482
    // mutation line 491
    // mutation line 560
    @Test(timeout = 500)
    public void testCacheMap_mutationCoverage_04() throws Exception {

        cacheMap = new CacheMap(true);

        for(int i = 0; i < 5; i++) {
            Assert.assertFalse(cacheMap.pin("key" + i));
        }
        for(int i = 5; i < 10; i++) {
            Assert.assertFalse(cacheMap.unpin("key" + i));
        }

        cacheMap.setCacheSize(15);
        cacheMap.setSoftReferenceSize(15);

        cacheMap.putAll(realMap);
        cacheMap.remove("key1");
        cacheMap.put("toString", cacheMap.toString());

        // Questo chiamerà alcune operazioni
        // che proveranno a prendere lo stesso lock
        Thread t = new Thread(() -> {
            Assert.assertTrue(cacheMap.pin("key6"));
            Assert.assertTrue(cacheMap.unpin("key4"));
            cacheMap.setCacheSize(25);
            cacheMap.setSoftReferenceSize(25);
            Assert.assertTrue(cacheMap.pin("key7"));
            cacheMap.remove("key7");
            cacheMap.put("toString", cacheMap.toString());
        });
        t.start();
        t.join();

        // Verifica che il thread sia ancora vivo
        Assert.assertFalse(t.isAlive());
        Assert.assertEquals(realMap.size()-2+1, cacheMap.size());
        Assert.assertEquals(25, cacheMap.getCacheSize());
        Assert.assertEquals(25, cacheMap.getSoftReferenceSize());

        cacheMap.clear();
        cacheMap.put("key1", " value1");

        // Questo chiamerà alcune operazioni
        // che proveranno a prendere lo stesso lock
        t = new Thread(() -> {
            cacheMap.clear();
            cacheMap.put("key2", "value2");
        });
        t.start();
        t.join();

        // Verifica che il thread sia ancora vivo
        Assert.assertFalse(t.isAlive());
        Assert.assertEquals(1, cacheMap.size());
        Assert.assertNull(cacheMap.get("key1"));
        Assert.assertEquals("value2", cacheMap.get("key2"));

    }

    // mutation line 393
    // mutation line 395
    // mutation line 396
    // mutation line 398
    // mutation line 413
    // mutation line 414
    // mutation line 417
    // mutation line 457
    // mutation line 459
    // mutation line 465
    // mutation line 466
    // mutation line 481
    // mutation line 485
    // mutation line 489
    @Test
    public void testCacheMap_mutationCoverage_05() throws Exception {

        AtomicInteger keyAdded = new AtomicInteger(0);
        AtomicInteger keyDeleted = new AtomicInteger(0);

        cacheMap = new TestCacheMap(false, 1, 1, (Object key, Object value) -> {
            keyAdded.getAndIncrement();
        }, (Object key, Object value, boolean expired) -> {
            Assert.assertFalse(expired);
            keyDeleted.getAndIncrement();
        });

        // --- INSERIMENTO ---

        String pinnedKey = "pinnedKey";
        String pinnedValue1 = "pinnedValue1";
        String pinnedValue2 = "pinnedValue2";

        cacheMap.pin(pinnedKey);
        Assert.assertNull(cacheMap.put(pinnedKey, pinnedValue1));
        Assert.assertSame(pinnedValue1, cacheMap.put(pinnedKey, pinnedValue2));

        // Verifico il numero di chiavi aggiunte e rimosse
        Assert.assertEquals(2, keyAdded.get());
        Assert.assertEquals(1, keyDeleted.get());

        String softKey = "softKey";
        String softValue1 = "softValue1";
        String softValue2 = "softValue2";

        Assert.assertNull(cacheMap.put(softKey, softValue1));
        Assert.assertSame(softValue1, cacheMap.put(softKey, softValue2));

        // Verifico il numero di chiavi aggiunte e rimosse
        Assert.assertEquals(4, keyAdded.get());
        Assert.assertEquals(2, keyDeleted.get());

        String hardKey = "hardKey";
        String hardValue1 = "hardValue1";
        String hardValue2 = "hardValue2";

        Assert.assertNull(cacheMap.put(hardKey, hardValue1));
        Assert.assertSame(hardValue1, cacheMap.put(hardKey, hardValue2));

        // Verifico il numero di chiavi aggiunte e rimosse
        Assert.assertEquals(6, keyAdded.get());
        Assert.assertEquals(3, keyDeleted.get());

        // Forzo la chiave in soft map a tornare in hard map
        Assert.assertSame(softValue2, cacheMap.put(softKey, softValue1));

        // Verifico il numero di chiavi aggiunte e rimosse
        Assert.assertEquals(7, keyAdded.get());
        Assert.assertEquals(4, keyDeleted.get());

        // --- RIMOZIONE ---

        // Rimuovo la chiave pinned
        Assert.assertSame(pinnedValue2, cacheMap.remove(pinnedKey));

        // Verifico il numero di chiavi aggiunte e rimosse
        Assert.assertEquals(5, keyDeleted.get());

        // Rimuovo la chiave soft (che adesso però é in hard map)
        Assert.assertSame(softValue1, cacheMap.remove(softKey));
        Assert.assertEquals(6, keyDeleted.get());

        // --- CLEAR ---

        cacheMap.clear();
        Assert.assertEquals(0, cacheMap.size());
        Assert.assertEquals(7, keyDeleted.get());

        cacheMap.pin(pinnedKey);
        cacheMap.put(pinnedKey, pinnedValue1);
        cacheMap.put(softKey, softValue1);
        cacheMap.put(hardKey, hardValue1);
        Assert.assertEquals(3, cacheMap.size());

        cacheMap.clear();
        Assert.assertEquals(0, cacheMap.size());
        Assert.assertEquals(10, keyDeleted.get());

    }

    // mutation line 176
    /*@Test
    public void testCacheMap_mutationCoverage_06() throws Exception {

        int softMapKey = 1_000_000;

        AtomicBoolean valueExpired = new AtomicBoolean(false);

        // Crea una CacheMap con maxSize 10
        cacheMap = new TestCacheMap(false, 10, null, (Object key, Object value, boolean expired) -> {
            if(key != null && value == null && expired) valueExpired.set(true);
        });

        ConcurrentReferenceHashMap softMap = (ConcurrentReferenceHashMap) cacheMap.softMap;

        // Aggiungo un numero di chiavi che superi il limite
        // che finiranno nella soft map
        for(int i = 0; i < softMapKey; i++) {
            softMap.put("key" + i, new byte[10_000]);
            if(i % 100_000 == 0) {
                System.gc(); // Forzo la garbage collection per rimuovere eventuali riferimenti deboli
                Thread.sleep(50); // Attendo un po' per garantire che la GC abbia effetto
            }
        }

        Assert.assertTrue(valueExpired.get());
    }*/

    // mutation line 159
    @Test
    public void testCacheMap_mutationCoverage_07() throws Exception {

        AtomicInteger entryRemoved = new AtomicInteger(0);

        // Forza una chiave specifica a essere rimossa
        // per evitare problemi di randomizzazione
        // La chiave è
        Field randomsField = ConcurrentHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        double[] randoms = (double[]) randomsField.get(null);
        Arrays.fill(randoms, 0.5); // Imposta un valore specifico per la randomizzazione

        // Crea una CacheMap con maxSize 10
        cacheMap = new TestCacheMap(false, 5, null, (Object key, Object value, boolean expired) -> {
            Assert.assertEquals("key9", key);
            entryRemoved.getAndIncrement();
        });

        cacheMap.setSoftReferenceSize(5);

        // Aggiungo un numero di chiavi che superi il limite
        for(int i = 0; i < 11; i++) {
            cacheMap.put("key" + i, "value" + i);
        }

        Assert.assertEquals(1, entryRemoved.get());
    }

    private class TestCacheMap extends CacheMap {
        private final EntryRemovedHandler removedHandler;
        private final EntryAddedHandler addedHandler;

        public TestCacheMap(boolean lru, int max, int size, EntryAddedHandler addedHandler, EntryRemovedHandler removedHandler) {
            super(lru, max, size, 0.75f);
            this.addedHandler = addedHandler;
            this.removedHandler = removedHandler;
        }

        public TestCacheMap(boolean lru, int max, EntryAddedHandler addedHandler, EntryRemovedHandler removedHandler) {
            super(lru, max);
            this.addedHandler = addedHandler;
            this.removedHandler = removedHandler;
        }

        @Override
        protected void entryRemoved(Object key, Object value, boolean expired) {
            if (removedHandler != null) {
                removedHandler.onEntryRemoved(key, value, expired);
            }
        }

        @Override
        protected void entryAdded(Object key, Object value) {
            if (addedHandler != null) {
                addedHandler.onEntryAdded(key, value);
            }
        }
    }

    @FunctionalInterface
    public interface EntryAddedHandler {
        void onEntryAdded(Object key, Object value);
    }

    @FunctionalInterface
    public interface EntryRemovedHandler {
        void onEntryRemoved(Object key, Object value, boolean expired);
    }

}
