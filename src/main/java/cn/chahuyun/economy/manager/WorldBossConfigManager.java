package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.dto.BossTeamUserSize;
import cn.chahuyun.economy.dto.BossUserSize;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.chahuyun.economy.entity.boss.WorldPropConfig;
import cn.chahuyun.economy.entity.team.Team;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.cron.CronUtil;
import org.apache.commons.compress.utils.Lists;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class WorldBossConfigManager {

    public static void init() {
     //   List<WorldBossConfig> list = getWorldBossConfigList();
        // 删除固定的
//        List<WorldBossEnum> removeEnumList = WorldBossEnum.getWorldBossNotFixEnumList();
//        removeEnumList.forEach(worldBossEnum -> {
//            Optional<WorldBossConfig> bossStatusConfig = list.stream().filter(keyConfig -> worldBossEnum.getKeyId() == keyConfig.getKeyId()).findFirst();
//            bossStatusConfig.ifPresent(WorldBossConfig::remove);
//        });

        List<WorldBossConfig> list2 = getWorldBossConfigList();
        List<WorldBossEnum> worldBossEnumList = WorldBossEnum.getWorldBossEnumList();
        worldBossEnumList.forEach(worldBossEnum -> {
            Optional<WorldBossConfig> bossStatusConfig = list2.stream().filter(keyConfig -> worldBossEnum.getKeyId() == keyConfig.getKeyId()).findFirst();
            if (bossStatusConfig.isEmpty()) {
                WorldBossConfig bossConfig = new WorldBossConfig(worldBossEnum.getKeyId(), worldBossEnum.getKeyString(), worldBossEnum.getValue());
                bossConfig.save();
            }
        });

        // 启动定时任务
        WorldBossConfig worldBossCornGoal = getWorldBossConfigByKey(WorldBossEnum.CORN_GOAL);
        runTask(worldBossCornGoal);
        WorldBossConfig worldBossCornProgress = getWorldBossConfigByKey(WorldBossEnum.CORN_PROGRESS);
        runTask(worldBossCornProgress);
        WorldBossConfig worldBossCornOpen = getWorldBossConfigByKey(WorldBossEnum.CORN_OPEN);
        runTask(worldBossCornOpen);
    }

    private static void runTask(WorldBossConfig worldBossCornDay) {
        if(Objects.isNull(worldBossCornDay)){
            return;
        }
        List<String> cronList = List.of(worldBossCornDay.getConfigInfo().split("｜"));
        for (int i = 0; i < cronList.size(); i++) {
            String cronKey = worldBossCornDay.getKeyId() + "-" + worldBossCornDay.getKeyString() + "-" + (i + 1);
            CronUtil.remove(cronKey);
            if (WorldBossEnum.CORN_PROGRESS.getKeyId() == worldBossCornDay.getKeyId()) {
                WorldBossProcessTask task = new WorldBossProcessTask();
                CronUtil.schedule(cronKey, cronList.get(i), task);
            }
            if (WorldBossEnum.CORN_GOAL.getKeyId() == worldBossCornDay.getKeyId()) {
                WorldBossGoalTask task = new WorldBossGoalTask();
                CronUtil.schedule(cronKey, cronList.get(i), task);
            }
            if (WorldBossEnum.CORN_OPEN.getKeyId() == worldBossCornDay.getKeyId()) {
                WorldBossOpenTask task = new WorldBossOpenTask();
                CronUtil.schedule(cronKey, cronList.get(i), task);
            }
        }
    }

    public static List<WorldBossConfig> getWorldBossConfigList(){
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<WorldBossConfig> query = builder.createQuery(WorldBossConfig.class);
            JpaRoot<WorldBossConfig> from = query.from(WorldBossConfig.class);
            query.select(from);
            return session.createQuery(query).list();
        });
    }

    public static WorldBossConfig getWorldBossConfigByKey(WorldBossEnum worldBossEnum) {
        return getWorldBossConfigByKey(worldBossEnum.getKeyId(), worldBossEnum.getKeyString());
    }

    public static WorldBossConfig getWorldBossConfigByKey(int keyId,String keyString) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<WorldBossConfig> query = builder.createQuery(WorldBossConfig.class);
            JpaRoot<WorldBossConfig> from = query.from(WorldBossConfig.class);
            query.where(builder.equal(from.get("keyId"), keyId),builder.equal(from.get("keyString"), keyString));
            query.select(from);
            return session.createQuery(query).getSingleResultOrNull();
        });
    }

    /**
     * 用户钓鱼尺寸记录
     * @return
     */
    public static List<WorldBossUserLog> getWorldBossUserLog(Long groupId){
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<WorldBossUserLog> query = builder.createQuery(WorldBossUserLog.class);
            JpaRoot<WorldBossUserLog> from = query.from(WorldBossUserLog.class);
            if(Objects.nonNull(groupId)){
                query.where(builder.equal(from.get("groupId"), groupId));
            }

            query.select(from);
            return session.createQuery(query).list();
        });
    }

    public static List<WorldBossUserLog> getWorldBossUserLog() {
        return Optional.ofNullable(getWorldBossUserLog(null)).orElse(Lists.newArrayList());
    }

    public static void saveWorldBossUserLog(Long groupId, Long userId, int size) {
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            return;
        }
        // 判断时间
        WorldBossConfig openTimeConfig = getWorldBossConfigByKey(WorldBossEnum.OPEN_TIME);
        WorldBossConfig endTimeConfig = getWorldBossConfigByKey(WorldBossEnum.END_TIME);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime openTime = LocalDateTime.parse(openTimeConfig.getConfigInfo(), Constant.FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(endTimeConfig.getConfigInfo(), Constant.FORMATTER);

        Boolean checkTime = now.isAfter(openTime) && now.isBefore(endTime);
        Log.info("判断是否在钓鱼区间: 当前时间" + now.format(Constant.FORMATTER) + "开始时间：" + openTime.format(Constant.FORMATTER) + "结束时间：" + endTime.format(Constant.FORMATTER) + "判断是否在钓鱼区间: 是否在时间范围内" + checkTime);
        if (checkTime) {
            WorldBossUserLog worldBossUserLog = new WorldBossUserLog(IdUtil.getSnowflakeNextId(), groupId, userId,
                    size, now);
            worldBossUserLog.save();
        }
    }

    /**
     * 开奖列表
     */

    public static List<WorldPropConfig> getWorldPropConfigList(){
        return Optional.ofNullable(HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<WorldPropConfig> query = builder.createQuery(WorldPropConfig.class);
            JpaRoot<WorldPropConfig> from = query.from(WorldPropConfig.class);
            query.select(from);
            return session.createQuery(query).list();
        })).orElse(Lists.newArrayList());
    }

    public static WorldPropConfig getWorldPropConfigByTypeAndCode(String type, String propCode) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<WorldPropConfig> query = builder.createQuery(WorldPropConfig.class);
            JpaRoot<WorldPropConfig> from = query.from(WorldPropConfig.class);
            query.where(builder.equal(from.get("type"), type),builder.equal(from.get("propCode"), propCode));
            query.select(from);
            return session.createQuery(query).getSingleResultOrNull();
        });
    }

    public static void refreshCronStr(WorldBossConfig oldConfig, String newCronStr) {
        deleteCronConf(oldConfig);
        oldConfig.setConfigInfo(newCronStr);
        oldConfig.setConfigInfo(newCronStr);
        oldConfig.save();
        runTask(oldConfig);
    }

    private static void deleteCronConf(WorldBossConfig oldConfig) {
        if(Objects.isNull(oldConfig)){
            return;
        }
        List<String> cronList = List.of(oldConfig.getConfigInfo().split("｜"));
        for (int i = 0; i < cronList.size(); i++) {
            String cronKey = oldConfig.getKeyId() + "-" + oldConfig.getKeyString() + "-" + (i + 1);
            CronUtil.remove(cronKey);
            if (WorldBossEnum.CORN_PROGRESS.getKeyId() == oldConfig.getKeyId()) {
                CronUtil.remove(cronKey);
            }
            if (WorldBossEnum.CORN_GOAL.getKeyId() == oldConfig.getKeyId()) {
                CronUtil.remove(cronKey);
            }
            if (WorldBossEnum.CORN_OPEN.getKeyId() == oldConfig.getKeyId()) {
                CronUtil.remove(cronKey);
            }
        }
    }

    /**
     * 获取用户的
     * @param groupWorldBossUserLog
     * @return
     */
    public static List<BossTeamUserSize> getTeamUserBbList(List<WorldBossUserLog> groupWorldBossUserLog, Long groupId) {
        // 用户钓鱼的数量
        Map<Long, Integer> userSizeMap = groupWorldBossUserLog.stream().collect(Collectors.toMap(WorldBossUserLog::getUserId, WorldBossUserLog::getSize, Integer::sum));
        // 没有组队，则排队个人比
        List<Team> teamList = Optional.ofNullable(TeamManager.listTeam(groupId)).orElse(Lists.newArrayList());
//        if (CollectionUtils.isEmpty(teamList)) {
//            List<BossUserSize> bossUserSizeList = new ArrayList<>();
//            for (Map.Entry<Long, Integer> entry : userSizeMap.entrySet()) {
//                BossUserSize userSize = new BossUserSize();
//                userSize.setUserId(entry.getKey());
//                userSize.setFishSize(userSize.getFishSize());
//                bossUserSizeList.add(userSize);
//            }
//            List<BossUserSize> bossUserSortList = bossUserSizeList.stream().sorted(Comparator.comparing(BossUserSize::getFishSize).reversed()).collect(Collectors.toList());
//            return getUserBbMap(bb, bossUserSortList);
//        } else {
            List<BossTeamUserSize> bossTeamUserSizesList = new ArrayList<>();
            // 每个人钓鱼数量 bossUserSizeList
            Set<Long> userIdList = groupWorldBossUserLog.stream().map(WorldBossUserLog::getUserId).collect(Collectors.toSet());
            // 组队
            List<Team> teamInfoList = teamList.stream().filter(team -> userIdList.contains(team.getTeamMember()) || userIdList.contains(team.getTeamOwner())).collect(Collectors.toList());
            // 结算组队的大小
            teamInfoList.forEach(team -> {
                Long teamOwner = team.getTeamOwner();
                Long teamMember = team.getTeamMember();
                Integer size = Optional.ofNullable(userSizeMap.get(teamOwner)).orElse(0) + Optional.ofNullable(userSizeMap.get(teamMember)).orElse(0);
                BossTeamUserSize teamUserSize = new BossTeamUserSize();
                teamUserSize.setId(team.getId());
                teamUserSize.setFishSize(size);
                teamUserSize.setType(1);
                teamUserSize.setTeamOwner(teamOwner);
                teamUserSize.setTeamMember(teamMember);
                teamUserSize.setTeamName(team.getTeamName());
                bossTeamUserSizesList.add(teamUserSize);
            });
            // 个人
            Set<Long> teamUserId = new HashSet<>();
            teamInfoList.forEach(t -> {
                teamUserId.add(t.getTeamMember());
                teamUserId.add(t.getTeamOwner());
            });
            List<Long> expUserId = userIdList.stream().filter(userId -> !teamUserId.contains(userId)).collect(Collectors.toList());
            expUserId.stream().forEach(userId->{
                Integer size = Optional.ofNullable(userSizeMap.get(userId)).orElse(0);
                BossTeamUserSize teamUserSize = new BossTeamUserSize();
                teamUserSize.setId(userId);
                teamUserSize.setFishSize(size);
                teamUserSize.setType(0);
                bossTeamUserSizesList.add(teamUserSize);
            });
            List<BossTeamUserSize> bossUserSortList = bossTeamUserSizesList.stream().sorted(Comparator.comparing(BossTeamUserSize::getFishSize).reversed()).collect(Collectors.toList());
            return bossUserSortList;
          //    return getUserBbMapByTeamUser(bb, bossUserSortList);
//        }
    }




    public static Map<Long, Double> getUserBbMapByTeamUser(double bb, List<BossTeamUserSize> bossTeamUserSizes) {
        Map<Long, Double> map = new HashMap<>();
        for (int i = 0; i < bossTeamUserSizes.size(); i++) {
            BossTeamUserSize teamUser = bossTeamUserSizes.get(i);
            if (i == 0) {
                putBB(teamUser, map, bb * 7, bb * 3.5);
                continue;
            }
            if (i == 1) {
                putBB(teamUser, map, bb * 5, bb * 2.5);
                continue;
            }
            if (i == 2) {
                putBB(teamUser, map, 3 * bb, 1.5 * bb);
                continue;
            }
            putBB(teamUser, map, bb, bb);
        }
        return map;
    }

    private static void putBB(BossTeamUserSize teamUser, Map<Long, Double> map, double bb, double teamBb) {
        // 组队用户
        bb = NumberUtil.round(bb, 2).doubleValue();
        teamBb = NumberUtil.round(teamBb, 2).doubleValue();
        if(teamUser.getType().equals(1)){
            map.put(teamUser.getTeamOwner(), teamBb);
            map.put(teamUser.getTeamMember(), teamBb);
        }
        if(teamUser.getType().equals(0)){
            map.put(teamUser.getId(), bb);
        }
    }


    private static Map<Long, Double> getUserBbMap(double bb, List<BossUserSize> bossUserSortList) {
        bb = NumberUtil.round(bb, 2).doubleValue();
        Map<Long, Double> map = new HashMap<>();
        for (int i = 0; i < bossUserSortList.size(); i++) {
            Long userId = bossUserSortList.get(i).getUserId();
            if (i ==  0) {
                map.put(userId, 7 * bb);
            }
            if (i ==  1) {
                map.put(userId, 5 * bb);
            }
            if (i ==  2) {
                map.put(userId, 3 * bb);
            }
            map.put(userId, bb);
        }
        return map;
    }
}
