package com.example.transformingparking.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SortingAlgorithms {
    public static void sortListBasedOnTimestamp(List<Map<String, Object>> list) {
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                // Define the formatter
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Assuming the timestamp is stored as a String
                LocalDateTime timestamp1 = LocalDateTime.parse((String) m1.get("timestamp"), formatter);
                LocalDateTime timestamp2 = LocalDateTime.parse((String) m2.get("timestamp"), formatter);

                return timestamp2.compareTo(timestamp1);
            }
        });
    }
}
