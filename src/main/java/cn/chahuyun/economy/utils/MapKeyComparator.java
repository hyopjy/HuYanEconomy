package cn.chahuyun.economy.utils;

import java.util.Comparator;

public class MapKeyComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }
}