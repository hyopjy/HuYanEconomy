package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.entity.PropTimeRange;
import cn.chahuyun.economy.entity.TimeRange;
import cn.chahuyun.economy.manager.PropTimeRangeManager;
import cn.chahuyun.economy.manager.TimeRangeManager;
import org.apache.commons.lang3.RandomUtils;;

import java.time.LocalDateTime;
import java.util.*;

public class DateUtil {

    public static Boolean checkDate() {
        try {
            LocalDateTime now = LocalDateTime.now();
            TimeRange timeRange = TimeRangeManager.getByWeekDay(now.getDayOfWeek().getValue());
            if (Objects.isNull(timeRange)) {
                return true;
            }
            String hourRange = timeRange.getTime();
            // 1,2,3,4,5 0-5,10-23
            // 6,7 9-15,20-23
            String[] hourArr = hourRange.split(",");
            for (String hour : hourArr) {
                String[] hourArrTime = hour.split("-");
                int hourOpen = Integer.parseInt(hourArrTime[0]);
                int hourEnd = Integer.parseInt(hourArrTime[1]);
                if (now.getHour() >= hourOpen && now.getHour() < hourEnd) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.error("date util error");
            Log.error(e);
            return true;
        }
        return false;
    }

    public static Boolean checkPropDate(String code) {
        String dd = "岛岛全自动钓鱼机";
        String ddCode = "27";
        String mask = "面罩";
        String maskCode = "34";

        if (code.contains(dd) || code.contains(ddCode) || code.contains(mask) || code.contains(maskCode)) {
            return true;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            PropTimeRange propTimeRange = PropTimeRangeManager.getByWeekDay(now.getDayOfWeek().getValue());
            if (Objects.isNull(propTimeRange)) {
                return true;
            }
            String hourRange = propTimeRange.getTime();
            // 1,2,3,4,5 0-5,10-23
            // 6,7 9-15,20-23
            String[] hourArr = hourRange.split(",");
            for (String hour : hourArr) {
                String[] hourArrTime = hour.split("-");
                int hourOpen = Integer.parseInt(hourArrTime[0]);
                int hourEnd = Integer.parseInt(hourArrTime[1]);
                if (now.getHour() >= hourOpen && now.getHour() < hourEnd) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.error("date util error");
            Log.error(e);
            return true;
        }
        return false;
    }
    public static String getCron(LocalDateTime end){

        LocalDateTime end5Minutes = end.minusMinutes(5L);
        // LocalDateTime end5Minutes = end.minusMinutes(2L);
        String sp = " ";
        // [minute] [hour] [day of month] [month] [day of week]
        // [minte] 表示分钟。取值范围 0 到 59
        // [hour] 表示小时。取值范围 0 到 23
        // [day of month] 表示几号。取值范围 1 到 23
        // [month] 表示几月。取值范围 1 到 12，也可以是用名称简写（从 Jan 到 Dec）
        // [day of week] 表示周几。取值范围 0 到 6，也可以是用名称简写（从 Sun 到 Sat）
        Integer second = end5Minutes.getSecond();
        Integer minute = end5Minutes.getMinute();
        Integer hour = end5Minutes.getHour();
        Integer day = end5Minutes.getDayOfMonth();
        Integer month = end5Minutes.getMonthValue();
        return  second + sp + minute + sp + hour + sp + day + sp + month + sp + "?";
    }


//    public static void main(String[] args) {
//        Set<Long> awardUserIds = new HashSet<>();
//        List<Long> userIdList = new ArrayList<>();
//        userIdList.add(4L);
//        userIdList.add(5L);
//        userIdList.add(6L);
//        userIdList.add(9L);
//        userIdList.add(7L);
//
//        for (int i = 0; i < 5; i++) {
//            addUserIds(awardUserIds, userIdList);
//        }
//        System.out.println(awardUserIds);
//    }
//
//    public static void addUserIds(Set<Long> awardUserIds,List<Long> userIdList) {
//        int rand = RandomUtils.nextInt(0, userIdList.size());
//        Long userId = userIdList.get(rand);
//        if(!awardUserIds.contains(userId)){
//            awardUserIds.add(userId);
//        }else {
//            addUserIds(awardUserIds,userIdList);
//        }
//    }
}
