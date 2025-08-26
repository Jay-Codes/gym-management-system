package com.jerrycode.gym_services.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObjectFactory {
    public static Map<String, List<String>> newMapping() {
        return new HashMap<>();
    }

    public static void addNewMapping(Map<String, List<String>> map, String field, String message) {
        map.computeIfAbsent(field, k -> new java.util.ArrayList<>()).add(message);
    }

    public static Map<String, String> newOrderedStringMap() {
        return new LinkedHashMap<>();
    }

    public static Map<String, Long> newOrderedLongMap() {
        return new LinkedHashMap<>();
    }
}
