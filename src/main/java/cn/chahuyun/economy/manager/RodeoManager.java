package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.entity.rodeo.RodeoRecord;
import cn.chahuyun.economy.strategy.RodeoFactory;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class RodeoManager {
//
//
//    决斗
//1.分配决斗[双方][规定时间段]内的比赛[局数]（按局数给权限）
//            【
//    东风吹，战鼓擂，决斗场上怕过谁！
//    新的🏟[比赛场次名]已确定于[14:00-17:00]开战！
//            [@A ]与[@B ]正式展开决斗的巅峰对决！⚔[N]局比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
//            】
//            2.该场比赛结束后，统计双方的得分和总被禁言时长
//【
//        [比赛场次名]结束，恭喜胜者@B以[3:1]把对手@A鸡哔！🔫
//    @B共被禁言[秒]
//    @A共被禁言[秒]
//    菜！就！多！练！
//            】
//
//    轮盘
//1.分配轮盘[多方][时间段]（10分钟左右，手动配置）内的比赛（按时间段给权限）
//            【
//    东风吹，战鼓擂，轮盘赛上怕过谁！
//    新的🏟[比赛场次名]正式开战！比赛时长[10分钟]，参赛选手有：@A@B@C@D
//    轮盘比赛正式打响！🔫[10分钟]的比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
//            】
//            2.该场比赛结束后，统计多方的得分和总被禁言时长
//【
//        [比赛场次名]结束，得分表如下：
//    B-3
//    C-2
//    D-1
//    A-0
//    @A共被禁言[秒]
//    @B共被禁言[秒]
//    @C共被禁言[秒]
//    @D共被禁言[秒]
//            】
//
//    大乱斗（多人决斗，逻辑同轮盘）
//            1.分配决斗[多方][时间段]（10分钟左右，手动配置）内的比赛（按时间段给权限）
//            【
//    东风吹，战鼓擂，决斗场上怕过谁！
//    新的🏟[比赛场次名]正式开战！比赛时长[10分钟]，参赛选手有：@A@B@C@D
//    大乱斗比赛正式打响！🔫[10分钟]的比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
//            】
//            2.该场比赛结束后，统计多方的得分和总被禁言时长
//【
//        [比赛场次名]结束，得分表如下：
//    B-3
//    C-2
//    D-1
//    A-0
//    @A共被禁言[秒]
//    @B共被禁言[秒]
//    @C共被禁言[秒]
//    @D共被禁言[秒]】
    /**
     * 正在进行的比赛
     */
    public static Map<String, Rodeo> CURRENT_SPORTS = new ConcurrentHashMap<>();


    /**
     * 判断是否可以比赛
     * @param
     */
    public static void checkCanSport(String messageType) {
    }

    public static boolean checkUserInRodeo(long groupId, long userId) {
//        String taskKey = rodeo.getGroupId() +
//                Constant.SPILT2 + rodeo.getDay() +
//                Constant.SPILT2 + rodeo.getStartTime() +
//                Constant.SPILT2 + rodeo.getEndTime() +
//                Constant.SPILT2 + rodeo.getPlayers();

        Set<String> keys = CURRENT_SPORTS.keySet();
        for (String key : keys) {
            if(key.startsWith(groupId+"")){
                String[] taskKeyArr = key.split(Constant.SPILT2);
                if(taskKeyArr.length != 5){
                    return false;
                }
                String[] playersArr = taskKeyArr[4].split(Constant.MM_SPILT);
                for(String p1: playersArr){
                    if(p1.equals(userId+"")){
                        // 判断决斗胜负是否已经分出
                        if (!RodeoManager.isDuelOver(CURRENT_SPORTS.get(key))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断决斗的胜负是否已经分出
     *
     * @param rodeo
     * @return
     */
    private static boolean isDuelOver(Rodeo rodeo) {
        if(!RodeoFactory.DUEL.equals(rodeo.getPlayingMethod())){
            return false;
        }
        Long id = rodeo.getId();
        List<RodeoRecord> records =RodeoRecordManager.getRecordsByRodeoId(id);
        if(CollectionUtils.isEmpty(records)){
            return false;
        }

        if(records.size() ==  rodeo.getRound()){
            return true;
        }

        // 如果打7局 ， 每一局都会有失败者
        // 只要记录失败次数为 7 / 2  = 3 说明已经失败
        // 记录的是输的次数
        int roundLoseCount = rodeo.getRound()/ 2 ;
        Map<String, Long> userRecordCount = records.stream()
                .collect(Collectors.groupingBy(RodeoRecord::getPlayer, Collectors.counting()));

        Map<String, Long> filteredUserRecordCount = userRecordCount.entrySet().stream()
                .filter(entry -> entry.getValue() >= roundLoseCount)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // 查询出来有输家
        return MapUtils.isNotEmpty(filteredUserRecordCount);
    }

    public void init(){
        // 删除今天时间之前的数据

        // 启动有效的任务

        // CURRENT_SPORTS.put
    }

    //[groupId] 决斗 场次名称 2024-08-23 15:18-14:38 934415751,952746839 5
    public void setSport(Long groupId, String venue, String date, String startTime, String endTime, String players, int round, String playingMethod){
        // todo check 是否存在 时间是否有交叉
        if(checkDateAndTime(date, startTime, endTime)){
            return;
        }

        // todo 用户时间是否冲突
//        if(checkUserAndTime(date, startTime, endTime)){
//            return;
//        }

        Rodeo rodeo = new Rodeo(groupId, venue,  date, startTime, endTime, players, round, playingMethod);
        rodeo.saveOrUpdate();
        // 启动定时任务
        runTask(rodeo);
    }



    private boolean checkDateAndTime(String date, String startTime, String endTime) {
        return true;
    }

    private static void runTask(Rodeo rodeo) {
        if(Objects.isNull(rodeo)){
            return;
        }

        // String date = "2024-08-06";
        // String startTime = "12:00:00";
        // String endTime = "13:00:00";
        String startCronExpression = getCronByDateAndTime(rodeo.getDay(), rodeo.getStartTime());
        String endCronExpression = getCronByDateAndTime(rodeo.getDay(), rodeo.getEndTime());

        // 开始任务
        String startCronKey = rodeo.getDay() + Constant.SPILT + rodeo.getStartTime();
        CronUtil.remove(startCronKey);

        // groupID|2024-08-23|15:18:00|14:38:00|934415751,952746839
        String taskKey = rodeo.getGroupId() + Constant.SPILT2 + rodeo.getDay() + Constant.SPILT2 + rodeo.getStartTime() + Constant.SPILT2 + rodeo.getEndTime() + Constant.SPILT2 + rodeo.getPlayers();
        RodeoOpenTask startTask = new RodeoOpenTask(taskKey, rodeo);
        CronUtil.schedule(startCronKey, startCronExpression, startTask);

        // 结束任务
        String endCronKey = rodeo.getDay() +Constant.SPILT + rodeo.getEndTime();
        CronUtil.remove(endCronKey);
        RodeoEndTask endTask = new RodeoEndTask(taskKey, rodeo);
        CronUtil.schedule(endCronKey, endCronExpression, endTask);

    }

    public static String getCronByDateAndTime(String date, String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);

        int seconds = dateTime.getSecond();
        int minutes = dateTime.getMinute();
        int hour = dateTime.getHour();
        int dayOfMonth = dateTime.getDayOfMonth();
        int month = dateTime.getMonthValue();

        Set<Integer> hourList = new TreeSet<>();
        Set<Integer> dayList = new TreeSet<>();
        Set<Integer> monthList = new TreeSet<>();

        hourList.add(hour);
        dayList.add(dayOfMonth);
        monthList.add(month);

        String hourStr = CollUtil.join(hourList, ",");
        String dayStr = CollUtil.join(dayList, ",");
        String monthStr = CollUtil.join(monthList, ",");

        // [秒] [分] [时] [日] [月] [周] [年]
        return seconds + " " + minutes + " " + hourStr + " " + dayStr + " " + monthStr + " ?";
    }

}
