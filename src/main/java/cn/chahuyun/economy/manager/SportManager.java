//package cn.chahuyun.economy.manager;
//
//import cn.chahuyun.economy.constant.WorldBossEnum;
//import cn.chahuyun.economy.entity.boss.WorldBossConfig;
//import cn.chahuyun.economy.entity.sport.Sport;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.cron.CronUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.compress.utils.Lists;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//public class SportManager {
//
//    /**
//     * 就是你只需要给我一个能够配置日期、时间段、选手、局数、玩法（决斗或轮盘）就可以了。
//     * 然后根据配置的结果，在指定时间内指定的选手发送的指令是有效的，然后根据规则展示结果就行
//     *
//     *
//     * ：在［配置的时间段］内，［配置的两个用户］获得了［配置局数］的游戏次数。
//     * 只要在这个配置时间段内，这两个用户就可以开N场决斗/轮盘
//     *
//     东风吹，战鼓擂，决斗场上怕过谁！
//     新的🏟️[比赛场次名]已确定于[14:00-17:00]开战！
//     [@A ]与[@B ]正式展开[决斗/轮盘]的巅峰对决！⚔️[N]局比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
//     [比赛场次名]结束，@A与对手@B的比分为[1:1]🤺
//     这个是当设置的局数为2时，结果的展示（2局比赛是小组赛的特殊情况，只得积分，不分输赢），替代上面那个[比赛场次名]结束，恭喜胜者
//     *
//     */
//    /**
//     * 正在进行的比赛
//     */
//    public static Map<String, List<Sport>> CURRENT_SPORTS = new ConcurrentHashMap<>();
//
//
//    /**
//     * 判断是否可以比赛
//     * @param messageType2
//     */
//    public static void checkCanSport(String messageType) {
//    }
//
//    public void init(){
//        // 删除今天时间之前的数据
//
//        // 启动有效的任务
//
//        // CURRENT_SPORTS.put
//    }
//    public void setSport(String date, String startTime, String endTime, String players, String number, String playingMethod){
//        // todo check 是否存在
//        if(checkDateAndTime(date, startTime, endTime)){
//            return;
//        }
//
//        // todo 用户时间是否冲突
////        if(checkUserAndTime(date, startTime, endTime)){
////            return;
////        }
//
//        Sport sport = new Sport(date, startTime, endTime, players, number, playingMethod);
//        sport.saveOrUpdate();
//        // 启动定时任务
//        runTask(sport);
//    }
//
//
//
//    private boolean checkDateAndTime(String date, String startTime, String endTime) {
//        return true;
//    }
//
//    private static void runTask(Sport sport) {
//        if(Objects.isNull(sport)){
//            return;
//        }
//        String date = "2024-08-06";
//        String startTime = "12:00:00";
//        String endTime = "13:00:00";
//        String startCronExpression = getCronByDateAndTime(date, startTime);
//        String endCronExpression = getCronByDateAndTime(date, endTime);
//
//        // 开始任务
//        String startCronKey = sport.getDate() + "-" + sport.getStartTime();
//        CronUtil.remove(startCronKey);
//        SportOpenTask startTask = new SportOpenTask();
//        CronUtil.schedule(startCronKey, startCronExpression, startTask);
//
//        // 开始任务
//        String endCronKey = sport.getDate() + "-" + sport.getEndTime();
//        CronUtil.remove(endCronKey);
//        SportEndTask endTask = new SportEndTask();
//        CronUtil.schedule(endCronKey, endCronExpression, endTask);
//
//    }
//
//    public static String getCronByDateAndTime(String date, String time) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);
//
//        int seconds = dateTime.getSecond();
//        int minutes = dateTime.getMinute();
//        int hour = dateTime.getHour();
//        int dayOfMonth = dateTime.getDayOfMonth();
//        int month = dateTime.getMonthValue();
//
//        Set<Integer> hourList = new TreeSet<>();
//        Set<Integer> dayList = new TreeSet<>();
//        Set<Integer> monthList = new TreeSet<>();
//
//        hourList.add(hour);
//        dayList.add(dayOfMonth);
//        monthList.add(month);
//
//        String hourStr = CollUtil.join(hourList, ",");
//        String dayStr = CollUtil.join(dayList, ",");
//        String monthStr = CollUtil.join(monthList, ",");
//
//        // [秒] [分] [时] [日] [月] [周] [年]
//        return seconds + " " + minutes + " " + hourStr + " " + dayStr + " " + monthStr + " ?";
//    }
//
//}
