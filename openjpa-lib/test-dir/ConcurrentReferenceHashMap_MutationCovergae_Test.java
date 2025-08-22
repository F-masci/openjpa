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
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mockStatic;

public class ConcurrentReferenceHashMap_MutationCovergae_Test {

    ConcurrentReferenceHashMap concurrentReferenceHashMap;

    private static double[] originalRandoms;

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
    public void resetRandoms() throws Exception {
        // Ripristina i valori random
        Field randomsField = ConcurrentReferenceHashMap.class.getDeclaredField("RANDOMS");
        randomsField.setAccessible(true);
        double[] randoms = (double[]) randomsField.get(null);
        randoms = Arrays.copyOf(originalRandoms, originalRandoms.length);
    }

    // line 433
    @Test
    public void testConcurrentReferenceHashMap_mutation_01() throws Exception {

        int validKey = 1_000;

        AtomicInteger keyDeleted = new AtomicInteger(0);

        concurrentReferenceHashMap = new TestConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD,
                (key, value) -> {
                    keyDeleted.getAndIncrement();
                }
        );

        for(int i = 0; i < validKey; i++) {
            concurrentReferenceHashMap.put("key" + i, "value" + i);
        }

        concurrentReferenceHashMap.setMaxSize(100);
        Assert.assertEquals(100, concurrentReferenceHashMap.getMaxSize());
        Assert.assertEquals(100, concurrentReferenceHashMap.size());

        for(int i = 0; i < 10; i++) {
            concurrentReferenceHashMap.put("newKey" + i, "value" + i);
        }

        Assert.assertEquals(100, concurrentReferenceHashMap.size());

        // Verifica che il numero di chiavi eliminate corrisponde al numero di chiavi che sono state rimosse
        // per far rientrare la mappa nel limite di dimensione.
        Assert.assertEquals(910, keyDeleted.get());

    }

    // line 493
    // line 495
    // line 506
    @Test
    public void testConcurrentReferenceHashMap_mutation_02a() throws Exception {

        int tableSize = 10;

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD,
                tableSize,
                0.99f
        );

        setRandomValue(concurrentReferenceHashMap, 0, 0.8); // => mi aspetto come randomIndex 8
        setRandomValue(concurrentReferenceHashMap, 1, 0.8); // => mi aspetto come randomIndex 8
        setRandomValue(concurrentReferenceHashMap, 2, 0.9); // => mi aspetto come randomIndex 9
        setRandomValue(concurrentReferenceHashMap, 3, 0.3); // => mi aspetto come randomIndex 3
        setRandomValue(concurrentReferenceHashMap, 4, 0.3); // => mi aspetto come randomIndex 3
        setRandomValue(concurrentReferenceHashMap, 5, 0.9); // => mi aspetto come randomIndex 9

        Field randomEntryField = ConcurrentReferenceHashMap.class.getDeclaredField("randomEntry");
        randomEntryField.setAccessible(true);
        int randomEntryOld = (int) randomEntryField.get(concurrentReferenceHashMap);

        // Index 8
        concurrentReferenceHashMap.put("key1", "value1");
        // Index 9
        concurrentReferenceHashMap.put("key2", "value2");
        // Index 0
        concurrentReferenceHashMap.put("key3", "value3");
        // Index 1
        concurrentReferenceHashMap.put("key4", "value4");
        // Index 2
        concurrentReferenceHashMap.put("key5", "value5");
        // Index 3
        concurrentReferenceHashMap.put("key6", "value6");
        // Index 4
        concurrentReferenceHashMap.put("key7", "value7");
        // Index 5
        concurrentReferenceHashMap.put("key8", "value8");
        // Index 6
        concurrentReferenceHashMap.put("key9", "value9");

        // Mi aspetto che venga eliminato l'elemento in posizione 8 (key1, value1)
        Map.Entry e1 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 8, ma
        // poiché questo non esiste la mappa prenderà il primo valore che troverà
        // con indice superiore, ovvero quello con indice 9 (key2, value2)
        Map.Entry e2 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 9, ma
        // poiché questo non esiste la mappa prenderà il primo valore che troverà
        // con indice inferiore, ovvero quello con indice 6 (key9, value9)
        Map.Entry e9 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 3 (key6, value6)
        Map.Entry e6 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 3, ma
        // poiché questo non esiste la mappa prenderà il primo valore che troverà
        // con indice inferiore, ovvero quello con indice 2 (key5, value5)
        Map.Entry e5 = concurrentReferenceHashMap.removeRandom();

        // Verifica che gli elementi rimossi siano quelli attesi
        Assert.assertEquals("key1", e1.getKey());
        Assert.assertEquals("value1", e1.getValue());

        Assert.assertEquals("key2", e2.getKey());
        Assert.assertEquals("value2", e2.getValue());

        Assert.assertEquals("key5", e5.getKey());
        Assert.assertEquals("value5", e5.getValue());

        Assert.assertEquals("key6", e6.getKey());
        Assert.assertEquals("value6", e6.getValue());

        Assert.assertEquals("key9", e9.getKey());
        Assert.assertEquals("value9", e9.getValue());

        int randomEntryNew = (int) randomEntryField.get(concurrentReferenceHashMap);

        // Verifica che il campo randomEntry sia stato aggiornato
        Assert.assertEquals(5, randomEntryNew - randomEntryOld);

        // Verifica la dimensione della mappa
        Assert.assertEquals(4, concurrentReferenceHashMap.size());
    }

    // line 493
    // line 495
    // line 506
    @Test
    public void testConcurrentReferenceHashMap_mutation_02b() throws Exception {

        int tableSize = 11;

        concurrentReferenceHashMap = new ConcurrentReferenceHashMap(
                AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.HARD,
                tableSize,
                0.99f
        );

        setRandomValue(concurrentReferenceHashMap, 0, 0.99); // => mi aspetto come randomIndex 10
        setRandomValue(concurrentReferenceHashMap, 1, 0.99); // => mi aspetto come randomIndex 10
        setRandomValue(concurrentReferenceHashMap, 2, 0.1); // => mi aspetto come randomIndex 1
        setRandomValue(concurrentReferenceHashMap, 3, 0.1); // => mi aspetto come randomIndex 1
        setRandomValue(concurrentReferenceHashMap, 4, 0.1); // => mi aspetto come randomIndex 1

        Field randomEntryField = ConcurrentReferenceHashMap.class.getDeclaredField("randomEntry");
        randomEntryField.setAccessible(true);
        int randomEntryOld = (int) randomEntryField.get(concurrentReferenceHashMap);


        // Index 4
        concurrentReferenceHashMap.put("key1", "value1");
        // Index 5
        concurrentReferenceHashMap.put("key2", "value2");
        // Index 6
        concurrentReferenceHashMap.put("key3", "value3");
        // Index 7
        concurrentReferenceHashMap.put("key4", "value4");
        // Index 8
        concurrentReferenceHashMap.put("key5", "value5");
        // Index 9
        concurrentReferenceHashMap.put("key6", "value6");
        // Index 10
        concurrentReferenceHashMap.put("key7", "value7");
        // Index 0
        concurrentReferenceHashMap.put("key8", "value8");
        // Index 1
        concurrentReferenceHashMap.put("key9", "value9");

        // Mi aspetto che venga eliminato l'elemento in posizione 10 (key7, value7)
        Map.Entry e7 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 10, ma
        // poiché questo non esiste la mappa prenderà il primo valore che troverà
        // con indice superiore. Poiché non ci sono tali valori,
        // cercherà tra quelli inferiori, ovvero quello con indice 9 (key6, value6)
        Map.Entry e6 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 1 (key9, value9)
        Map.Entry e9 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 1, ma
        // poiché questo non esiste la mappa prenderà il primo valore che troverà
        // con indice inferiore, ovvero quello con indice 0 (key8, key8)
        Map.Entry e8 = concurrentReferenceHashMap.removeRandom();

        // Mi aspetto che venga eliminato l'elemento in posizione 1, ma
        // poiché questo non esiste la mappa prenderà il primo valore che troverà
        // con indice inferiore. Poiché non ci sono tali valori,
        // cercherà tra quelli superiori, ovvero quello con indice 4 (key1, value1)
        Map.Entry e1 = concurrentReferenceHashMap.removeRandom();

        // Verifica che gli elementi rimossi siano quelli attesi
        Assert.assertEquals("key1", e1.getKey());
        Assert.assertEquals("value1", e1.getValue());

        Assert.assertEquals("key6", e6.getKey());
        Assert.assertEquals("value6", e6.getValue());

        Assert.assertEquals("key7", e7.getKey());
        Assert.assertEquals("value7", e7.getValue());

        Assert.assertEquals("key8", e8.getKey());
        Assert.assertEquals("value8", e8.getValue());

        Assert.assertEquals("key9", e9.getKey());
        Assert.assertEquals("value9", e9.getValue());


        // Verifica che la mappa non abbia rimosso l'elemento con chiave "key3"
        Assert.assertTrue(concurrentReferenceHashMap.containsKey("key3"));
        Assert.assertTrue(concurrentReferenceHashMap.containsValue("value3"));
        Assert.assertEquals("value3", concurrentReferenceHashMap.get("key3"));

        int randomEntryNew = (int) randomEntryField.get(concurrentReferenceHashMap);

        // Verifica che il campo randomEntry sia stato aggiornato
        Assert.assertEquals(5, randomEntryNew - randomEntryOld);

        // Verifica che la dimensione della mappa sia corretta
        Assert.assertEquals(4, concurrentReferenceHashMap.size());

    }

    private class TestConcurrentReferenceHashMap extends ConcurrentReferenceHashMap {

        private final EntryRemovedHandler removedHandler;

        public TestConcurrentReferenceHashMap(AbstractReferenceMap.ReferenceStrength keyType,
                                              AbstractReferenceMap.ReferenceStrength valueType,
                                              EntryRemovedHandler removedHandler) {
            super(keyType, valueType);
            this.removedHandler = removedHandler;
        }

        @Override
        public void overflowRemoved(Object key, Object value) {
            if (removedHandler != null) {
                removedHandler.onEntryRemoved(key, value);
            }
        }
    }

    @FunctionalInterface
    public interface EntryRemovedHandler {
        void onEntryRemoved(Object key, Object value);
    }

}
