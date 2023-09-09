package cn.chahuyun.economy.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class DateUtil {

    public static Boolean checkDate() {
        // 就每天的23-次日5
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() >= 23 || now.getHour() < 5) {
            return true;
        }
//        if(now.getDayOfWeek() == DayOfWeek.SUNDAY){
//            if(now.getHour() < 23){
//                return true;
//            }
//        }
        return false;
    }
}
