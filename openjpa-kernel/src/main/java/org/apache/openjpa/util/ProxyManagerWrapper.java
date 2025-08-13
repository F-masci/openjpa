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

import java.util.*;

public class ProxyManagerWrapper {

    private final ProxyManagerImpl proxyManager = new ProxyManagerImpl();

    // copy methods
    public Object testCopyArray() {
        String[] array = {"a", "b", "c"};
        return proxyManager.copyArray(array);
    }

    public Collection<?> testCopyCollection() {
        List<String> list = Arrays.asList("one", "two", "three");
        return proxyManager.copyCollection(list);
    }

    public Map<?, ?> testCopyMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        return proxyManager.copyMap(map);
    }

    public Date testCopyDate() {
        return proxyManager.copyDate(new Date());
    }

    public Calendar testCopyCalendar() {
        return proxyManager.copyCalendar(Calendar.getInstance());
    }

    public Object testCopyCustom() {
        return proxyManager.copyCustom("customString");
    }

    // newProxy methods
    public Object testNewDateProxy() {
        return proxyManager.newDateProxy(Date.class);
    }

    public Object testNewCalendarProxy() {
        return proxyManager.newCalendarProxy(GregorianCalendar.class, TimeZone.getDefault());
    }

    public Object testNewMapProxy() {
        return proxyManager.newMapProxy(Map.class, String.class, Integer.class, null, false);
    }

    public Object testNewCollectionProxy() {
        return proxyManager.newCollectionProxy(Collection.class, String.class, null, true);
    }

    public Object testNewCustomProxy() {
        return proxyManager.newCustomProxy(new String("proxyableString"), true);
    }

    // Getters and Setters
    public boolean testTrackChanges() {
        proxyManager.setTrackChanges(true);
        return proxyManager.getTrackChanges();
    }

    public boolean testAssertAllowedType() {
        proxyManager.setAssertAllowedType(true);
        return proxyManager.getAssertAllowedType();
    }

    public boolean testDelayCollectionLoading() {
        proxyManager.setDelayCollectionLoading(true);
        return proxyManager.getDelayCollectionLoading();
    }

    public void testSetUnproxyable() {
        proxyManager.setUnproxyable("com.example.NonProxyable");
    }

    public Collection<?> testGetUnproxyable() {
        return proxyManager.getUnproxyable();
    }
}
