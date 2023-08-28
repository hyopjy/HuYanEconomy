package cn.chahuyun.economy.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class DateUtil {

    public static Boolean checkDate(){

        // 周五22.00 周日 23.00
        LocalDateTime now = LocalDateTime.now();
        if(now.getDayOfWeek() == DayOfWeek.FRIDAY){
            if(now.getHour() >= 22){
                return true;
            }
        }
        if(now.getDayOfWeek() == DayOfWeek.SATURDAY){
            return true;
        }
        if(now.getDayOfWeek() == DayOfWeek.SUNDAY){
            if(now.getHour() < 23){
                return true;
            }
        }

        return false;
    }
}
