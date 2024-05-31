package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.merchant.ExchangeRecordsLog;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantGoods;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantShop;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.CronUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final Long SETTING_ID = 1L;

    public static final Integer CHANGE_TYPE_PROP = 0;
    public static final Integer CHANGE_TYPE_BB = 1;

    public static final Integer CHANGE_TYPE_SEASON = 2;

    public static final Map<String, MysteriousMerchantShop> SHOP_GOODS_MAP = new ConcurrentHashMap<>();

    public static void init(){
        // 加载商品信息
        reloadShopListMap();
        // 开启定时任务
        open();
    }

    /**
     * 开启神秘商人
     *
     */
    public static MysteriousMerchantSetting open(){
    //    开启神秘商人
        MysteriousMerchantSetting setting = geMysteriousMerchantSettingByKey(SETTING_ID);
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
        MysteriousMerchantSetting setting = geMysteriousMerchantSettingByKey(SETTING_ID);
        if(Objects.isNull(setting)){
            return null;
        }
        if(!setting.getStatus()){
            return setting;
        }

        setting.setStatus(false);
        setting.saveOrUpdate();
        deleteGoodBySettingId(setting.getSettingId(), null);
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
        MysteriousMerchantSetting config = geMysteriousMerchantSettingByKey(SETTING_ID);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantSetting();
            config.setSettingId(SETTING_ID);
            config.setStatus(true);
        }
        config.setHourStr(String.join(Constant.SPILT, hourList));
        config.setPassMinute(passMinute);
        config.setProbability(probability);
        config.setGoodCodeStr(String.join(Constant.SPILT, goodCodeList));
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
     * 神秘商品导入
     */
    public static void importShopInfo(){
        // 删除神秘商品
        HibernateUtil.factory.fromSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<MysteriousMerchantShop> deleteQuery = builder.createCriteriaDelete(MysteriousMerchantShop.class);
            Root<MysteriousMerchantShop> root = deleteQuery.from(MysteriousMerchantShop.class);
            // 执行删除操作
            session.createQuery(deleteQuery).executeUpdate();
            return null;
        });
        deleteGoodBySettingId(SETTING_ID, null);
        // 读取excel
        List<MysteriousMerchantShop> excelData = getExcelData();
        List<MysteriousMerchantShop> saveShop = excelData.stream().map(data -> {
            if (StringUtils.isNotEmpty(data.getProp2Code())) {
                data.setChangeType(CHANGE_TYPE_PROP);
            } else if (StringUtils.isNotEmpty(data.getProp2Code())) {
                data.setChangeType(CHANGE_TYPE_BB);
            } else if (StringUtils.isNotEmpty(data.getProp2Code())) {
                data.setChangeType(CHANGE_TYPE_SEASON);
            } else {
                return null;
            }
            return data;
        }).collect(Collectors.toList()).stream().filter(Objects::nonNull).collect(Collectors.toList());

        // 保存
        saveShopGoodList(saveShop);

        reloadShopListMap();
    }

    public static void reloadShopListMap() {
        /**
         * 查出数据库有的神秘商品
         */
        SHOP_GOODS_MAP.clear();

        List<MysteriousMerchantShop> shopGoodList =  HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantShop> query = builder.createQuery(MysteriousMerchantShop.class);
            JpaRoot<MysteriousMerchantShop> shop = query.from(MysteriousMerchantShop.class);
            query.orderBy(builder.asc(shop.get("goodCode"))); // 按照 goodCode 正序排序
            return session.createQuery(query).list();
        });

        shopGoodList.forEach(shopGood->{
            SHOP_GOODS_MAP.put(shopGood.getGoodCode(),shopGood);
        });
    }

    private static List<MysteriousMerchantShop> getExcelData() {
        HuYanEconomy instance = HuYanEconomy.INSTANCE;
        ExcelReader reader = ExcelUtil.getReader(instance.getResourceAsStream("fish.xlsx"), 2);
        Map<String, String> map = new HashMap<>();
        map.put("编号", "goodCode");
        map.put("道具编号", "prop1Code");
        map.put("其他道具", "prop2Code");
        map.put("其他道具数量", "prop2Count");
        map.put("币币", "bbCount");
        map.put("赛季币", "seasonMoney");
        List<MysteriousMerchantShop> list = reader.setHeaderAlias(map).readAll(MysteriousMerchantShop.class);
        reader.close();
        return list;
    }

    public synchronized static void exchange(MessageEvent event){
        Contact subject = event.getSubject();
        Group group = null;
        if(subject instanceof Group){
            group = (Group) subject;
        }
        if(Objects.isNull(group)){
            return;
        }
        String[] s = event.getMessage().serializeToMiraiCode().split(" ");
        if (s.length != 2) {
            Log.error("[抢购 格式不正确]");
            return;
        }
        String goodCode = s[1];
        MysteriousMerchantShop shop = MysteriousMerchantManager.getShopGoodCode(goodCode);
        if(Objects.isNull(shop)){
            return;
        }
        // 库存数量先放入redis中
        // redis进行-1
        // 判断当前商品是否在出售中
        // 判断剩余量（redis）


        // 获取userId
        Long senderId = event.getSender().getId();

        // 查询兑换编码
        // 兑换放入redis缓存  根据小时数

        MessageChain message = event.getMessage();
    }

    // 商品根据settingId删除
    public static void deleteGoodBySettingId(Long settingId , Long groupId) {
        HibernateUtil.factory.fromSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<MysteriousMerchantGoods> deleteQuery = builder.createCriteriaDelete(MysteriousMerchantGoods.class);
            Root<MysteriousMerchantGoods> root = deleteQuery.from(MysteriousMerchantGoods.class);

            // 添加 settingId 的约束条件
            deleteQuery.where(builder.equal(root.get("settingId"), settingId));

            // 如果 groupId 不为 null，则同时添加 groupId 的约束条件
            if (Objects.nonNull(groupId)) {
                deleteQuery.where(builder.equal(root.get("groupId"), groupId));
            }
            // 执行删除操作
            session.createQuery(deleteQuery).executeUpdate();
            return null;
        });

        // 兑换记录一并删除
        deleteExchangeRecordsLogBySettingId(settingId, groupId);
    }

    public static List<MysteriousMerchantGoods> getGoodBySettingId(Long settingId, Long groupId) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantGoods> query = builder.createQuery(MysteriousMerchantGoods.class);
            JpaRoot<MysteriousMerchantGoods> goods = query.from(MysteriousMerchantGoods.class);
            query.select(goods).where(
                    builder.equal(goods.get("settingId"), settingId),
                    builder.equal(goods.get("groupId"), groupId)
            );
            return session.createQuery(query).list();
        });
    }
    /**
     * 删除用户兑换记录
     * @param settingId
     * @return
     */
    public static void deleteExchangeRecordsLogBySettingId(Long settingId, Long groupId) {
        HibernateUtil.factory.fromSession(session -> {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<ExchangeRecordsLog> deleteQuery = builder.createCriteriaDelete(ExchangeRecordsLog.class);
            Root<ExchangeRecordsLog> root = deleteQuery.from(ExchangeRecordsLog.class);

            // 添加 settingId 的约束条件
            deleteQuery.where(builder.equal(root.get("settingId"), settingId));

            // 如果 groupId 不为 null，则同时添加 groupId 的约束条件
            if (Objects.nonNull(groupId)) {
                deleteQuery.where(builder.equal(root.get("groupId"), groupId));
            }

            // 执行删除操作
            session.createQuery(deleteQuery).executeUpdate();
            return null;
        });
    }

    /**
     * 查询指定的商品列表
     *
     * @param goodCodeList
     * @return
     */
    public static List<MysteriousMerchantShop> getMysteriousMerchantShopByGoodCodeList(List<String> goodCodeList) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantShop> query = builder.createQuery(MysteriousMerchantShop.class);
            JpaRoot<MysteriousMerchantShop> shop = query.from(MysteriousMerchantShop.class);
            query.select(shop).where(builder.in(shop.get("goodCode")).value(goodCodeList));
            query.orderBy(builder.asc(shop.get("goodCode"))); // 按照 goodCode 正序排序
            return session.createQuery(query).list();
        });
    }

    public static MysteriousMerchantShop getShopGoodCode(String codeStr) {
        return SHOP_GOODS_MAP.get(codeStr);
    }

    /**
     * 保存当前生成的商品信息
     *
     * @param goodUpList
     */
    public static void saveGoodUpList(List<MysteriousMerchantGoods> goodUpList) {
        for(MysteriousMerchantGoods good : goodUpList){
            good.saveOrUpdate();
            //
        }
    }
    public static void saveShopGoodList(List<MysteriousMerchantShop> shopList) {
        shopList.stream().forEach(MysteriousMerchantShop::saveOrUpdate);
    }

}
