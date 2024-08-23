package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.entity.rodeo.RodeoRecord;
import cn.chahuyun.economy.strategy.RodeoFactory;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

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
        List<RodeoRecord> records = RodeoRecordManager.getRecordsByRodeoId(id);
        if(CollectionUtils.isEmpty(records)){
            return false;
        }

        // 从记录中提取局数并找出最大局数
        Integer maxTurns = records.stream()
                .map(RodeoRecord::getTurns)
                .max(Comparator.naturalOrder()).orElse(0);
        // 决斗存入赢+输的场次
        if (maxTurns == rodeo.getRound()) {
            return true;
        }

        // 如果打5局 ， 每一局都会有赢家和胜利者
        // 只要记录胜利次数为 5 / 2 + 1   = 2 说明已经胜利

        // 查询最大场次
        //  1 0     1
        //  1 0     2
        //  0 1     3
        //  1 0     4
        //  0 0     5

        // 1 1
        // 1 2
        // 2 2
        // 3 2
        // 3 0
        // 3 1
        List<String> winnerPlayers = new ArrayList<String>();
        List<String> losePlayers = new ArrayList<String>();
        // 局数
        Map<Integer, List<RodeoRecord>> recordsByTurns = records.stream()
                .collect(Collectors.groupingBy(RodeoRecord::getTurns));
        recordsByTurns.forEach((turns, recordList) -> {
            Optional<RodeoRecord> winnerOptional = recordList.stream().filter(r-> Objects.isNull(r.getForbiddenSpeech()) || r.getForbiddenSpeech().equals(0)).findAny();
            winnerOptional.ifPresent(rodeoRecord -> winnerPlayers.add(rodeoRecord.getPlayer()));

            Optional<RodeoRecord> loseOptional = recordList.stream().filter(r->  r.getForbiddenSpeech() > 0).findAny();
            loseOptional.ifPresent(rodeoRecord -> losePlayers.add(rodeoRecord.getPlayer()));
        });
        // 3 0
        // 记录的是赢的次数
        int roundWinCount = (rodeo.getRound() / 2) + 1 ;
        return winnerPlayers.size() == roundWinCount;
    }

    //  判断存在的数据 时间是否有交叉
    public static boolean checkDateAndTime(String day, String startTime, String endTime) {
        List<Rodeo> exitsRodeo = getRodeoByDay(day);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime newStart = LocalDateTime.parse(day + " " + startTime, formatter);
        LocalDateTime newEnd = LocalDateTime.parse(day + " " + endTime, formatter);

        for (Rodeo rodeo : exitsRodeo) {
            LocalDateTime existingStart = LocalDateTime.parse(rodeo.getStartTime(), formatter);
            LocalDateTime existingEnd = LocalDateTime.parse(rodeo.getEndTime(), formatter);
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return false; // 时间段有交叉
            }
        }
        return true; // 时间段无交叉
    }

    public void init(){
        // 删除今天时间之前的数据

        // 启动有效的任务

        // CURRENT_SPORTS.put
    }


    public static void runTask(Rodeo rodeo) {
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


    /**
     * 根据日期查赛场
     * @param day
     * @return
     */
    public static List<Rodeo> getRodeoByDay(String day) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Rodeo> query = builder.createQuery(Rodeo.class);
            JpaRoot<Rodeo> from = query.from(Rodeo.class);
            query.where(builder.equal(from.get("day"), day));
            query.select(from);
            return session.createQuery(query).list();
        });
    }
}
