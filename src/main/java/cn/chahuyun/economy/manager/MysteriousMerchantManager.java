package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.cron.CronUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 神秘商人管理
 *
 */
public class MysteriousMerchantManager {

    //1.每天14点、17点、21点都有15%概率刷新神秘商人
    //2.神秘商人会在商品83~92中（暂定十个）随机上架2~4种
    //3.神秘商人上架的每种商品各有随机1~3个库存
    //4.神秘商人会在出现后的10分钟消失
    //5.每个人在神秘商人处的限购数为每次每种商品数1
    //6.神秘商人的商品会有兑换品，
    // 即：
    // 可能有币币购买、
    // 可能有赛季币购买、
    // 可能有道具兑换
    // 神秘商人的商品会有打包商品，如：100币币作为一个打包品，每次可以使用100000赛季币购买一份
    //7.具体的商品列表、回复文案请等我！！！

    /**
     * 开启神秘商人
     *
     */
    public static MysteriousMerchantSetting open(){
    //    开启神秘商人
        MysteriousMerchantSetting config = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantSetting();
            config.setSettingId(1L);
            config.setBuyCount(1);
        }else{
            closeTask(config);
        }
        config.setStatus(true);
        config.saveOrUpdate();
        runTask(config);
        return config;
    }

    /**
     * 关闭神秘商人
     *

     */
    public static MysteriousMerchantSetting close(){
    //    关闭神秘商人
        MysteriousMerchantSetting config = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantSetting();
            config.setSettingId(1L);
            config.setBuyCount(1);
        }
        config.setStatus(false);
        config.saveOrUpdate();
        closeTask(config);
        return config;
    }

    /**
     * 设置限购次数
     * @return
     */
    public static MysteriousMerchantSetting setBuyCount(Integer buyCount){
        //    关闭神秘商人
        MysteriousMerchantSetting config = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantSetting();
            config.setSettingId(1L);
            config.setStatus(false);
        }
        config.setBuyCount(buyCount);
        config.saveOrUpdate();
        return config;
    }


    /**
     * 设置神秘商人base
     *
     */
    public static MysteriousMerchantSetting setting(List<String> hourList,
                                                    Integer passMinute,
                                                    Integer probability,
                                                    List<String> goodCodeList,
                                                    Integer randomGoodCount,
                                                    Integer minStored,
                                                    Integer maxStored) {
        //    设置神秘商人 14,17,21 10(几分钟消失) 15%   83-92(商品编码范围) 2(几种道具)  1-3(随机道具库存)
        MysteriousMerchantSetting config = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantSetting();
            config.setSettingId(1L);
            config.setStatus(false);
            config.setBuyCount(1);
        }
        config.setHourStr(String.join(",", hourList));
        config.setPassMinute(passMinute);
        config.setProbability(probability);
        config.setGoodCodeStr(String.join(",", goodCodeList));
        config.setRandomGoodCount(randomGoodCount);
        config.setMinStored(minStored);
        config.setMaxStored(maxStored);
        config.saveOrUpdate();
        return config;

    }

    public static MysteriousMerchantSetting geMysteriousMerchantSettingByKey(Long settingId) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantSetting> query = builder.createQuery(MysteriousMerchantSetting.class);
            JpaRoot<MysteriousMerchantSetting> from = query.from(MysteriousMerchantSetting.class);
            query.where(builder.equal(from.get("settingId"), settingId));
            query.select(from);
            return session.createQuery(query).getSingleResultOrNull();
        });
    }

    /**
     * 商品列表创建 及创建task
     *
     * @param setting
     * @return
     */
    public static void configRunTask(MysteriousMerchantSetting setting) {
        if(Objects.isNull(setting)){
            return;
        }
        Long settingId = setting.getSettingId();

        List<Integer> hourList = Arrays.stream(setting.getHourStr().split(","))
                .mapToInt(Integer::parseInt)
                .boxed()
                .collect(Collectors.toList());

        int startMinutes = getStartMinutes();
        int endMinutes = getEndMinutes(setting);

        for (int i = 0; i < hourList.size(); i++) {
            Integer hour = hourList.get(i);
            String startCronKey = getStartKey(settingId, hour, startMinutes);
            String endCronKey = getEndKey(settingId, hour, endMinutes);
            CronUtil.remove(startCronKey);
            CronUtil.remove(endCronKey);
            runTask(startCronKey, endCronKey, settingId, hour, startMinutes, endMinutes);
        }

    }
    public static int getStartMinutes() {
        return 30;
    }

    public static int getEndMinutes(MysteriousMerchantSetting setting) {
        return getStartMinutes() + setting.getPassMinute();
    }

    public static String getStartKey(Long settingId, Integer hour, Integer minutes) {
        return getKey("start", settingId, hour, minutes);
    }

    public static String getEndKey(Long settingId, Integer hour, Integer minutes) {
        return getKey("end", settingId, hour, minutes);
    }

    public static String getKey(String prefix, Long settingId, Integer hour, Integer minutes) {
        return "mm:" + prefix + Constant.SPILT + settingId + Constant.SPILT + hour + Constant.SPILT + minutes;
    }
    public static void closeTask(MysteriousMerchantSetting config) {
        if(Objects.isNull(config)){
            return;
        }
        Long settingId = config.getSettingId();
        List<Integer> hourList = Arrays.stream(config.getHourStr().split(","))
                .mapToInt(Integer::parseInt)
                .boxed()
                .collect(Collectors.toList());

        int startMinutes = getStartMinutes();
        int endMinutes = getEndMinutes(config);

        for (int i = 0; i < hourList.size(); i++) {
            Integer hour = hourList.get(i);
            String startCronKey = getStartKey(settingId, hour, startMinutes);
            String endCronKey = getEndKey(settingId, hour, endMinutes);
            CronUtil.remove(startCronKey);
            CronUtil.remove(endCronKey);
        }

    }

    private static void runTask(String startCronKey, String endCronKey, Long settingId, Integer hour, Integer startMinutes, Integer endMinutes) {
        String startCron = "";
        String endCron = "";
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


    /**
     * 设置神秘商人
     *
     * @param event
     */
    public static void buyShop(MessageEvent event){
        //    神秘商人商店 (excel导入)
    }

    public static void exchangeShop(MessageEvent event){
        //    神秘商人兑换商店 (excel导入)
    }

    public static void buy(MessageEvent event){
        //    购买(想个新命令)
    }

    public static void exchange(MessageEvent event){
        //    兑换(想个新命令)
    }



    // 道具背包 展示神秘商店标志
}
