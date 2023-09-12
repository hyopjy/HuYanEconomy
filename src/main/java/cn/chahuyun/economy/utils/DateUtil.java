package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.entity.TimeRange;
import cn.chahuyun.economy.manager.TimeRangeManager;

import java.time.LocalDateTime;
import java.util.Objects;

public class DateUtil {

    public static Boolean checkDate() {
        try {
            LocalDateTime now = LocalDateTime.now();
            TimeRange timeRange = TimeRangeManager.getByWeekDay(now.getDayOfWeek().getValue());
            if (Objects.nonNull(timeRange)) {
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
            }
        } catch (Exception e) {
            Log.error("date util error");
            Log.error(e);
            return true;
        }
        return false;
    }
}
