package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantGoods;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.cron.CronUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.text.MessageFormat;
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

    //
//    ◈日常任务
//　      完成当日的日常任务，可以获得额外道具日常币×1
//       包括：签到，面罩34三次，购买狗的姐姐2一次（在完成的时候消息提示：恭喜@用户获得波波日常币×1）
//
//    ◈被狗的姐姐2选中后，背包内增加心选礼盒×1
//         狗的姐姐2文案：[狗的姐姐使用成功]
//    @使用者 搭讪的姐姐选择了幸运数字18
//    @用户 被姐姐成功俘获，ATM姬自愿交出了2646币币，但获得了心选礼盒×1
//
//            ◈上钩保护动物（手钓和钓鱼机）后背包内增加动物保护徽章×1
    /**
     * 开启神秘商人
     *
     */
    public static MysteriousMerchantSetting open(){
    //    开启神秘商人
        MysteriousMerchantSetting setting = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(setting)){
            return null;
        }
        if(setting.getStatus()){
            return setting;
        }
        setting.setStatus(true);
        setting.saveOrUpdate();

        closeTask(setting);
        runTask(setting);
        return setting;
    }

    /**
     * 关闭神秘商人
     *

     */
    public static MysteriousMerchantSetting close(){
    //    关闭神秘商人
        MysteriousMerchantSetting setting = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(setting)){
            return null;
        }
        if(!setting.getStatus()){
            return setting;
        }

        setting.setStatus(false);
        setting.saveOrUpdate();
        deleteGoodBySettingId(setting.getSettingId());
        closeTask(setting);
        return setting;
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
                                                    Integer maxStored,
                                                    Integer buyCount) {
        //    设置神秘商人 14,17,21 10(几分钟消失) 15%   83-92(商品编码范围) 2(几种道具)  1-3(随机道具库存)
        MysteriousMerchantSetting config = geMysteriousMerchantSettingByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantSetting();
            config.setSettingId(1L);
            config.setStatus(true);
        }
        config.setHourStr(String.join(",", hourList));
        config.setPassMinute(passMinute);
        config.setProbability(probability);
        config.setGoodCodeStr(String.join(",", goodCodeList));
        config.setRandomGoodCount(randomGoodCount);
        config.setMinStored(minStored);
        config.setMaxStored(maxStored);
        config.setBuyCount(buyCount);
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
    public static void settingRunTask(MysteriousMerchantSetting setting) {
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
            runTask(setting);
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

    private static void runTask(MysteriousMerchantSetting setting) {
        // 启动任务
        int startMinutes = getStartMinutes();
        int endMinutes = getEndMinutes(setting);
        Long settingId = setting.getSettingId();
        List<Integer> hourList = Arrays.stream(setting.getHourStr().split(","))
                .mapToInt(Integer::parseInt)
                .boxed()
                .collect(Collectors.toList());

        for (int i = 0; i < hourList.size(); i++) {
            Integer hour = hourList.get(i);
            String startCronKey = getStartKey(settingId, hour, startMinutes);
            String endCronKey = getEndKey(settingId, hour, endMinutes);
            // task
            MysteriousMerchantOpenTask openTask = new MysteriousMerchantOpenTask(setting, hour);
            MysteriousMerchantEndTask endTask = new MysteriousMerchantEndTask(setting, hour);
            // cron
            String startCron = generateDailyCronExpression(hour, startMinutes);
            String endCron = generateDailyCronExpression(hour, endMinutes);

            CronUtil.schedule(startCronKey, startCron, openTask);
            CronUtil.schedule(endCronKey, endCron, endTask);
        }

    }

    /**
     * 定时任务表达式生成
     *
     * @param hour
     * @param minutes
     * @return
     */
    public static String generateDailyCronExpression(int hour, int minutes) {
        return MessageFormat.format("0 {0} {1} * * ?", minutes, hour);
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

    // 商品根据settingId删除
    public static void deleteGoodBySettingId(Long settingId) {
        HibernateUtil.factory.fromSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<MysteriousMerchantGoods> deleteQuery = builder.createCriteriaDelete(MysteriousMerchantGoods.class);
            Root<MysteriousMerchantGoods> root = deleteQuery.from(MysteriousMerchantGoods.class);
            deleteQuery.where(builder.equal(root.get("settingId"), settingId));
            session.createQuery(deleteQuery).executeUpdate();
            return null;
        });
    }

}
