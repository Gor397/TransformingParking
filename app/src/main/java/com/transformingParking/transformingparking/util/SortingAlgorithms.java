package com.transformingParking.transformingparking.util;

import com.transformingParking.transformingparking.ParkingActivities.RatingAdapter.RatingReviewItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SortingAlgorithms {
    public static class MapListWrapper {
        private List<Map<String, Object>> list;

        public MapListWrapper(List<Map<String, Object>> list) {
            this.list = list;
        }

        public List<Map<String, Object>> getList() {
            return list;
        }
    }

    public static class RatingReviewItemListWrapper {
        private List<RatingReviewItem> list;

        public RatingReviewItemListWrapper(List<RatingReviewItem> list) {
            this.list = list;
        }

        public List<RatingReviewItem> getList() {
            return list;
        }
    }

    public static void sortListBasedOnTimestamp(MapListWrapper wrapper) {
        List<Map<String, Object>> list = wrapper.getList();
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime timestamp1 = LocalDateTime.parse((String) m1.get("timestamp"), formatter);
                LocalDateTime timestamp2 = LocalDateTime.parse((String) m2.get("timestamp"), formatter);
                return timestamp2.compareTo(timestamp1);
            }
        });
    }

    public static void sortListBasedOnTimestamp(RatingReviewItemListWrapper wrapper) {
        List<RatingReviewItem> list = wrapper.getList();
        Collections.sort(list, new Comparator<RatingReviewItem>() {
            @Override
            public int compare(RatingReviewItem o1, RatingReviewItem o2) {
                LocalDateTime timestamp1 = o1.getTimestamp();
                LocalDateTime timestamp2 = o2.getTimestamp();
                return timestamp2.compareTo(timestamp1);
            }
        });
    }
}
