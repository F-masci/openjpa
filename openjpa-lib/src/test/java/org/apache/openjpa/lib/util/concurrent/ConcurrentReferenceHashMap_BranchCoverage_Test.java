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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

public class ConcurrentReferenceHashMap_BranchCoverage_Test {

    // Chiavi di esempio per i test
    // Hanno lo stesso hashCode ma sono diversi
    String key1 = "FB"; // hashCode = 2236 => %11 = 3
    String key2 = "Ea"; // hashCode = 2236 => %11 = 3

    String key3 = "KL"; // hashCode = 2401 => %11 = 3

    ConcurrentReferenceHashMap concurrentReferenceHashMap;

    private static double[] originalRandoms;

    private void addNullEntry(ConcurrentReferenceHashMap map, int nEntries) throws Exception {
        Field tableField = ConcurrentReferenceHashMap.class.getDeclaredField("table");
        tableField.setAccessible(true);
        Object[] table = (Object[]) tableField.get(map);

        Field countField = ConcurrentReferenceHashMap.class.getDeclaredField("count");
        countField.setAccessible(true);
        countField.set(map, nEntries);

        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
    }

    /**
     * Modifica i valori random della mappa.
     * @param map la mappa da cui prendere l'indice di partenza
     * @param offset l'offset da aggiungere all'indice attuale
     * @param value il valore da impostare per il prossimo indice random
     * @throws Exception
     */
    private void setRandomValue(ConcurrentReferenceHashMap map, int offset, double value) throws Exception {
        // Modifico i prossimi valori random
        Field randomEntryField = ConcurrentReferenceHashMap.class.getDeclaredField("randomEntry");
        randomEntryField.setAccessible(true);
        int randomEntry = (int) randomEntryField.get(map);

        Field randomsField = ConcurrentReferenceHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        double[] randomsNumber = (double[]) randomsField.get(null);

        randomsNumber[randomEntry + offset % map.capacity()] = value;
    }

    @BeforeClass
    public static void saveRandoms() throws Exception {
        // Salva i valori random iniziali
        Field randomsField = ConcurrentReferenceHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        originalRandoms = Arrays.copyOf((double[]) randomsField.get(null), ((double[]) randomsField.get(null)).length);
    }

    @Before
    public void setup() {
        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
            AbstractReferenceMap.ReferenceStrength.HARD,
            AbstractReferenceMap.ReferenceStrength.HARD
        );
    }

    @After
    public void tearDown() throws Exception {
        // Ripristina i valori random
        Field randomsField = ConcurrentReferenceHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        double[] randoms = (double[]) randomsField.get(null);
        randoms = Arrays.copyOf(originalRandoms, originalRandoms.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConcurrentReferenceHashMap_Constructor_01() {
        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD,
                -1,
                -1f
        );
    }

    @Test
    public void testConcurrentReferenceHashMap_keySet_values_entrySet_01() {

        // Verifica i keySet
        Set keySet1 = concurrentReferenceHashMap.keySet();
        Assert.assertNotNull(keySet1);
        Set keySet2 = concurrentReferenceHashMap.keySet();
        Assert.assertNotNull(keySet2);

        // Mi aspetto che keySet1 e keySet2 siano la stessa istanza
        // poiché viene costruito solo la prima volta
        Assert.assertSame(keySet1, keySet2);

        // Verifica i values
        Collection values1 = concurrentReferenceHashMap.values();
        Assert.assertNotNull(values1);
        Collection values2 = concurrentReferenceHashMap.values();
        Assert.assertNotNull(values2);

        // Mi aspetto che values1 e values2 siano la stessa istanza
        // poiché viene costruito solo la prima volta
        Assert.assertSame(values1, values2);

        // Verifica gli entrySet
        Set entrySet1 = concurrentReferenceHashMap.entrySet();
        Assert.assertNotNull(entrySet1);
        Set entrySet2 = concurrentReferenceHashMap.entrySet();
        Assert.assertNotNull(entrySet2);

        // Mi aspetto che entrySet1 e entrySet2 siano la stessa istanza
        // poiché viene costruito solo la prima volta
        Assert.assertSame(entrySet1, entrySet2);

        // Verifico che i dati siano coerenti
        concurrentReferenceHashMap.put(key1, "value1");
        concurrentReferenceHashMap.put(key2, "value2");
        concurrentReferenceHashMap.put(key3, "value3");

        // Verifica che le chiavi siano presenti
        Assert.assertTrue(keySet1.contains(key1));
        Assert.assertTrue(keySet1.contains(key2));
        Assert.assertTrue(keySet1.contains(key3));

        // Verifica che i valori siano presenti
        Assert.assertTrue(values1.contains("value1"));
        Assert.assertTrue(values1.contains("value2"));
        Assert.assertTrue(values1.contains("value3"));

        // Verifica che le entry siano presenti
        Assert.assertTrue(entrySet1.contains(new AbstractReferenceMap.SimpleEntry<>(key1, "value1")));
        Assert.assertTrue(entrySet1.contains(new AbstractReferenceMap.SimpleEntry<>(key2, "value2")));
        Assert.assertTrue(entrySet1.contains(new AbstractReferenceMap.SimpleEntry<>(key3, "value3")));
    }

    // Stesso hashCode ma chiavi diverse
    // Stessp index ma hashCode diversi
    @Test
    public void testConcurrentReferenceHashMap_sameIndex_01() {

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
            AbstractReferenceMap.ReferenceStrength.HARD,
            AbstractReferenceMap.ReferenceStrength.HARD,
            11, // Dimensione della tabella (necessaria per avere stessi indici)
            0.75f // Load factor
        );

        // Verifica che le due chiavi abbiano lo stesso hashCode
        Assert.assertEquals(key1.hashCode(), key2.hashCode());

        Object value1 = new Object();
        concurrentReferenceHashMap.put(key1, value1);
        Object value2 = new Object();
        concurrentReferenceHashMap.put(key2, value2);
        Object value3 = new Object();
        concurrentReferenceHashMap.put(key3, value3);

        // Verifica che entrambi i valori siano presenti
        Assert.assertTrue(concurrentReferenceHashMap.containsKey(key1));
        Assert.assertTrue(concurrentReferenceHashMap.containsValue(value1));
        Assert.assertTrue(concurrentReferenceHashMap.containsKey(key2));
        Assert.assertTrue(concurrentReferenceHashMap.containsValue(value2));
        Assert.assertTrue(concurrentReferenceHashMap.containsKey(key3));
        Assert.assertTrue(concurrentReferenceHashMap.containsValue(value3));

        // Verifica che i valori siano corretti
        Assert.assertSame(value1, concurrentReferenceHashMap.get(key1));
        Assert.assertSame(value2, concurrentReferenceHashMap.get(key2));
        Assert.assertSame(value3, concurrentReferenceHashMap.get(key3));

        // Rimuovo la terza chiave e verifica che non sia più presente
        concurrentReferenceHashMap.remove(key3);
        Assert.assertFalse(concurrentReferenceHashMap.containsKey(key3));
        Assert.assertFalse(concurrentReferenceHashMap.containsValue(value3));

        // Rimuove la prima chiave e verifica che non sia più presente
        concurrentReferenceHashMap.remove(key1);
        Assert.assertFalse(concurrentReferenceHashMap.containsKey(key1));
        Assert.assertFalse(concurrentReferenceHashMap.containsValue(value1));

        // Rimuove la seconda chiave e verifica che non sia più presente
        concurrentReferenceHashMap.remove(key2);
        Assert.assertFalse(concurrentReferenceHashMap.containsKey(key2));
        Assert.assertFalse(concurrentReferenceHashMap.containsValue(value2));
    }

    @Test
    public void testConcurrentReferenceHashMap_eq_01() {
        Object a = new String("test");
        Object b = new String("test");
        Object c = new String("other");
        Object d = null;

        // 1. x == y (entrambi null)
        Assert.assertTrue(concurrentReferenceHashMap.eq(null, null));

        // 2. x == y (stesso oggetto non null)
        Assert.assertTrue(concurrentReferenceHashMap.eq(a, a));

        // 3. x != y, x == null
        Assert.assertFalse(concurrentReferenceHashMap.eq(null, a));

        // 4. x != y, y == null
        Assert.assertFalse(concurrentReferenceHashMap.eq(a, null));

        // 5. x != y, x != null, x.equals(y) == true
        Assert.assertTrue(concurrentReferenceHashMap.eq(a, b)); // "test".equals("test") == true, ma oggetti diversi

        // 6. x != y, x != null, x.equals(y) == false
        Assert.assertFalse(concurrentReferenceHashMap.eq(a, c)); // "test".equals("other") == false
    }

    @Test
    public void testConcurrentReferenceHashMap_containsValue_01() {
        // Branch true: tab.length > 0
        concurrentReferenceHashMap.put("a", "value");
        Assert.assertFalse(concurrentReferenceHashMap.containsValue("valueNotPresent"));

        // Branch false: tab.length == 0 (forzando la tabella vuota)
        // Simula tabella vuota
        concurrentReferenceHashMap.clear();
        Assert.assertFalse(concurrentReferenceHashMap.containsValue("valueNotPresent"));
    }

    @Test
    public void testConcurrentReferenceHashMap_loadFactor_01() {
        ConcurrentReferenceHashMap map = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD,
                16,
                0.75f
        );
        Assert.assertEquals("Expected load factor to be 0.75", 0.75f, map.loadFactor(), 0f);
    }

    @Test
    public void testConcurrentReferenceHashMap_removeRandom_randomEntryIndex_findEntry_01() {

        // Controllo che la generazione dei numeri random
        // funzioni anche con numeri alti
        int entryNum = 100_000;
        for (int i = 0; i < entryNum; i++) {
            concurrentReferenceHashMap.put("key" + i, "value" + i);
        }
        for(int i = 0; i < entryNum / 10; i++) {
            concurrentReferenceHashMap.removeRandom();
        }
        Assert.assertFalse("Map should not be empty after removing random entries", concurrentReferenceHashMap.isEmpty());
        Assert.assertEquals("Map should contain at least some entries", entryNum - (entryNum / 10), concurrentReferenceHashMap.size());
        concurrentReferenceHashMap.clear();

    }

    @Test
    public void testConcurrentReferenceHashMap_removeRandom_randomEntryIndex_findEntry_02() throws Exception {

        int fixedTableSize = 11; // Dimensione della tabella (dispari)

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
            AbstractReferenceMap.ReferenceStrength.HARD,
            AbstractReferenceMap.ReferenceStrength.HARD,
            fixedTableSize, // Dimensione della tabella (necessaria per avere stessi indici)
            0.75f // Load factor
        );

        // Simulo la presenza di entry null
        this.addNullEntry(concurrentReferenceHashMap, 1);

        // Forzo i seguenti valori random
        // 0/11 => il prossimo indice sarà 0
        setRandomValue(concurrentReferenceHashMap, 0, 0.0);
        // 1/11 => il prossimo indice sarà 1
        setRandomValue(concurrentReferenceHashMap, 1, 1.0 / fixedTableSize);
        // 10/11 => il prossimo indice sarà 10
        setRandomValue(concurrentReferenceHashMap, 2, 9.0 / fixedTableSize);
        // 9/11 => il prossimo indice sarà 9
        setRandomValue(concurrentReferenceHashMap, 3, 8.0 / fixedTableSize);

        Assert.assertNull(concurrentReferenceHashMap.removeRandom());
        Assert.assertNull(concurrentReferenceHashMap.removeRandom());
        Assert.assertNull(concurrentReferenceHashMap.removeRandom());
        Assert.assertNull(concurrentReferenceHashMap.removeRandom());

    }

    @Test
    public void testConcurrentReferenceHashMap_removeRandom_randomEntryIndex_findEntry_03() throws Exception {

        int fixedTableSize = 10; // Dimensione della tabella (pari)

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
            AbstractReferenceMap.ReferenceStrength.HARD,
            AbstractReferenceMap.ReferenceStrength.HARD,
            fixedTableSize, // Dimensione della tabella (necessaria per avere stessi indici)
            0.75f // Load factor
        );

        // Simulo la presenza di entry null
        this.addNullEntry(concurrentReferenceHashMap, 1);

        // Forzo i seguenti valori random
        // 0/11 => il prossimo indice sarà 0
        setRandomValue(concurrentReferenceHashMap, 0, 0.0);
        // 1/11 => il prossimo indice sarà 1
        setRandomValue(concurrentReferenceHashMap, 1, 1.0 / fixedTableSize);
        // 10/11 => il prossimo indice sarà 10
        setRandomValue(concurrentReferenceHashMap, 2, 9.0 / fixedTableSize);
        // 9/11 => il prossimo indice sarà 9
        setRandomValue(concurrentReferenceHashMap, 3, 8.0 / fixedTableSize);

        Assert.assertNull(concurrentReferenceHashMap.removeRandom());
        Assert.assertNull(concurrentReferenceHashMap.removeRandom());
        Assert.assertNull(concurrentReferenceHashMap.removeRandom());
        Assert.assertNull(concurrentReferenceHashMap.removeRandom());

    }

    @Test
    public void testConcurrentReferenceHashMap_rehash_01() throws Exception {

        int initialSize = 2; // Dimensione della tabella
        float loadFactor = 0.05f; // Fattore di carico

        int validKey = 1_000;

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
            AbstractReferenceMap.ReferenceStrength.HARD,
            AbstractReferenceMap.ReferenceStrength.SOFT,
            initialSize,
            loadFactor
        );

        // Aggiungo una chiave null
        concurrentReferenceHashMap.put(null, "nullValue");

        // Modifico il tipo di riferimento per le chiavi
        Field keyTypeField = ConcurrentReferenceHashMap.class.getDeclaredField("keyType");
        keyTypeField.setAccessible(true);
        keyTypeField.set(concurrentReferenceHashMap, AbstractReferenceMap.ReferenceStrength.SOFT);

        // Aggiungo un numero di chiavi che superi il limite
        // Il rehash dovrebbe essere effettuato ed eliminare la chiave null
        for(int i = 0; i < validKey; i++) {
            concurrentReferenceHashMap.put("key" + i, "value" + i);
        }

        // Verifico che la mappa contenga le chiavi valide
        for(int i = 0; i < validKey; i++) {
            Assert.assertTrue("Map should contain key: key" + i, concurrentReferenceHashMap.containsKey("key" + i));
            Assert.assertTrue("Map should contain value: value" + i, concurrentReferenceHashMap.containsValue("value" + i));
            Assert.assertEquals("value" + i, concurrentReferenceHashMap.get("key" + i));
        }

        // Verifico che la mappa contenga la chiave null
        Assert.assertFalse("Map should not contain null key", concurrentReferenceHashMap.containsKey(null));
        Assert.assertFalse("Map should not contain null value", concurrentReferenceHashMap.containsValue("nullValue"));
        Assert.assertNull(concurrentReferenceHashMap.get(null));

        // Verifico che la mappa abbia effettuato il rehash
        Assert.assertTrue("Table should be resized after rehash", concurrentReferenceHashMap.capacity() > initialSize);
        Assert.assertEquals("Table should contain entries after rehash", validKey, concurrentReferenceHashMap.size());

    }

    @Test
    public void testConcurrentReferenceHashMap_rehash_02() throws Exception {

        int initialSize = 2; // Dimensione della tabella
        float loadFactor = 0.05f; // Fattore di carico

        int validKey = 1_000;

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.SOFT,
                AbstractReferenceMap.ReferenceStrength.HARD,
                initialSize,
                loadFactor
        );

        // Aggiungo un valore null
        concurrentReferenceHashMap.put("nullKey", null);

        // Modifico il tipo di riferimento per i valori
        Field valueTypeField = ConcurrentReferenceHashMap.class.getDeclaredField("valueType");
        valueTypeField.setAccessible(true);
        valueTypeField.set(concurrentReferenceHashMap, AbstractReferenceMap.ReferenceStrength.WEAK);

        // Aggiungo un numero di chiavi che superi il limite
        // Il rehash dovrebbe essere effettuato ed eliminare il valore null
        for(int i = 0; i < validKey; i++) {
            concurrentReferenceHashMap.put("key" + i, "value" + i);
        }

        // Verifico che la mappa contenga le chiavi valide
        for(int i = 0; i < validKey; i++) {
            Assert.assertTrue("Map should contain key: key" + i, concurrentReferenceHashMap.containsKey("key" + i));
            Assert.assertTrue("Map should contain value: value" + i, concurrentReferenceHashMap.containsValue("value" + i));
            Assert.assertEquals("value" + i, concurrentReferenceHashMap.get("key" + i));
        }

        // Verifico che la mappa contenga la chiave null
        Assert.assertFalse("Map should not contain null key", concurrentReferenceHashMap.containsKey("nullKey"));
        Assert.assertFalse("Map should not contain null value", concurrentReferenceHashMap.containsValue(null));
        Assert.assertNull(concurrentReferenceHashMap.get("nullKey"));

        // Verifico che la mappa abbia effettuato il rehash
        Assert.assertTrue("Table should be resized after rehash", concurrentReferenceHashMap.capacity() > initialSize);
        Assert.assertEquals("Table should contain entries after rehash", validKey, concurrentReferenceHashMap.size());

    }

}
