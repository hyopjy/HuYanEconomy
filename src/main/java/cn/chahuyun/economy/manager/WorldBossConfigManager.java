package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.chahuyun.economy.entity.boss.WorldPropConfig;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.cron.CronUtil;
import jakarta.persistence.Id;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WorldBossConfigManager {

    public static void init() {
        List<WorldBossConfig> list = getWorldBossConfigList();
        List<WorldBossEnum> worldBossEnumList = WorldBossEnum.getWorldBossEnumList();
        worldBossEnumList.forEach(worldBossEnum -> {
            Optional<WorldBossConfig> bossStatusConfig = list.stream().filter(keyConfig -> worldBossEnum.getKeyId() == keyConfig.getKeyId()).findFirst();
            if (bossStatusConfig.isEmpty()) {
                WorldBossConfig bossConfig = new WorldBossConfig(worldBossEnum.getKeyId(), worldBossEnum.getKeyString(), worldBossEnum.getValue());
                bossConfig.save();
            }
        });

        // 启动定时任务
        WorldBossConfig worldBossCornDay = getWorldBossConfigByKey(WorldBossEnum.CORN_GOAL);
        runTask(worldBossCornDay);
        WorldBossConfig worldBossCornSpecial = getWorldBossConfigByKey(WorldBossEnum.CORN_PROGRESS);
        runTask(worldBossCornSpecial);


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
                WorldBossGoalTask  task = new WorldBossGoalTask();
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
        return getWorldBossUserLog(null);
    }

    public static void deleteAllWorldBossUserLog(Long groupId) {
        getWorldBossUserLog(groupId).forEach(WorldBossUserLog::remove);
    }

    public static void saveWorldBossUserLog(Long groupId, Long userId, int size) {
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            return;
        }
        // 判断时间
        WorldBossConfig openHourConfig = getWorldBossConfigByKey(WorldBossEnum.OPEN_HOUR);
        WorldBossConfig openHourMinuteConfig = getWorldBossConfigByKey(WorldBossEnum.OPEN_HOUR_MINUTE);
        WorldBossConfig endHourConfig = getWorldBossConfigByKey(WorldBossEnum.END_HOUR);

        LocalDateTime now = LocalDateTime.now();
        Boolean checkOpenTime = now.getHour() >= Integer.parseInt(openHourConfig.getConfigInfo())
                && now.getMinute() >= Integer.parseInt(openHourMinuteConfig.getConfigInfo());
        Boolean checkEndTime = now.getHour() <= Integer.parseInt(endHourConfig.getConfigInfo());

        if (checkOpenTime && checkEndTime) {
            WorldBossUserLog worldBossUserLog = new WorldBossUserLog(IdUtil.getSnowflakeNextId(), groupId, userId,
                    size, now);
            worldBossUserLog.save();
        }
    }

    /**
     * 开奖列表
     */

    public static List<WorldPropConfig> getWorldPropConfigList(){
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<WorldPropConfig> query = builder.createQuery(WorldPropConfig.class);
            JpaRoot<WorldPropConfig> from = query.from(WorldPropConfig.class);
            query.select(from);
            return session.createQuery(query).list();
        });
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
}
