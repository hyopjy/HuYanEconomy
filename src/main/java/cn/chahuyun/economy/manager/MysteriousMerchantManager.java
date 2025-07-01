package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.merchant.ExchangeRecordsLog;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantGoods;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantShop;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import jakarta.persistence.criteria.*;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

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

    public static final String SHOP_GOOD_KEY = "shop:good:redis:key:";

    public static final String SHOP_GOOD_USER_KEY = "shop:good:user:redis:key:";

    public static final Integer MAX_RETRY_COUNT = 2;

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
            Log.info("MysteriousMerchantManager-->setting is null");
            return null;
        }
        if(!setting.getStatus()){
            Log.info("MysteriousMerchantManager-->setting status is false");
            return setting;
        }
        setting.setStatus(true);
        setting.saveOrUpdate();

        closeTask(setting);
        runTask(setting);
        Log.info("MysteriousMerchantManager-->神秘商人开启成功");
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
        }else {
            MysteriousMerchantManager.closeTask(config);
        }
        config.setHourStr(String.join(Constant.SPILT, hourList));
        config.setPassMinute(passMinute);
        config.setProbability(probability);
        config.setGoodCodeStr(String.join(Constant.MM_SPILT, goodCodeList));
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
        runTask(setting);
    }
    public static int getStartMinutes() {
        return 30;
    }

    public static int getEndMinutes(MysteriousMerchantSetting setting) {
        return getStartMinutes() + setting.getPassMinute();
    }

    public static String getStartKey(Long settingId, Integer hour, Integer minutes) {
        return getKey("mmm:start", settingId, hour, minutes);
    }

    public static String getEndKey(Long settingId, Integer hour, Integer minutes) {
        return getKey("mmm:end", settingId, hour, minutes);
    }

    public static String getKey(String prefix, Long settingId, Integer hour, Integer minutes) {
        return "mm:" + prefix + Constant.SPILT + settingId + Constant.SPILT + hour + Constant.SPILT + minutes;
    }
    public static void closeTask(MysteriousMerchantSetting config) {
        if(Objects.isNull(config)){
            return;
        }
        Long settingId = config.getSettingId();
        List<Integer> hourList = Arrays.stream(config.getHourStr().split(Constant.SPILT))
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
        List<Integer> hourList = Arrays.stream(setting.getHourStr().split(Constant.SPILT))
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


            CronUtil.remove(startCronKey);
            CronUtil.remove(endCronKey);

            CronUtil.schedule(startCronKey, startCron, openTask);
            CronUtil.schedule(endCronKey, endCron, endTask);
            Log.info("MysteriousMerchantManager-runTask-startCron:" + startCron );
            Log.info("MysteriousMerchantManager-runTask-endCron:" + endCron );

        }

//        String startCron = "0/10 * * * * ?";
//        String endCron = "0/25 * * * * ?";
//        MysteriousMerchantOpenTask openTask = new MysteriousMerchantOpenTask(setting, 23);
//        MysteriousMerchantEndTask endTask = new MysteriousMerchantEndTask(setting, 23);
//        CronUtil.remove("222222");
//        CronUtil.remove("222222111");
//
//        CronUtil.schedule("222222", startCron, openTask);
//        CronUtil.schedule("222222111", endCron, endTask);
    }

    /**
     * 定时任务表达式生成
     *
     * @param hour
     * @param minutes
     * @return
     */
    public static String generateDailyCronExpression(int hour, int minutes) {
        return  String.format("0 %d %d * * ?", minutes, hour);
    }

    /**
     * 神秘商品导入
     */
    public static void importShopInfo(){
        // 删除神秘商品
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaDelete<MysteriousMerchantShop> delete = builder.createCriteriaDelete(MysteriousMerchantShop.class);
                Root<MysteriousMerchantShop> e = delete.from(MysteriousMerchantShop.class);
                return session.createQuery(delete).executeUpdate();
            });

            deleteGoodBySettingId(SETTING_ID, null);
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
            // 或者记录日志
            // logger.error("删除神秘商品时出错：" + e.getMessage(), e);
        }

        // 读取excel
        List<MysteriousMerchantShop> excelData = getExcelData();
        List<MysteriousMerchantShop> saveShop = excelData.stream().map(data -> {
            if (StringUtils.isNotEmpty(data.getProp2Code())) {
                data.setChangeType(CHANGE_TYPE_PROP);
            } else if (Objects.nonNull(data.getBbCount())) {
                data.setChangeType(CHANGE_TYPE_BB);
            } else if (Objects.nonNull(data.getSeasonMoney())) {
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
        ExcelReader reader = ExcelUtil.getReader(Constant.EXCEL_URL, 2);
        Map<String, String> map = new HashMap<>();
        map.put("编号", "goodCode");
        map.put("道具编号", "prop1Code");
        map.put("道具数量", "prop1Count");

        map.put("其他道具", "prop2Code");
        map.put("其他道具数量", "prop2Count");

        map.put("币币", "bbCount");
        map.put("赛季币", "seasonMoney");

        map.put("是否常驻","permanent");
        map.put("常驻数量", "permanentCount");
        List<MysteriousMerchantShop> list = reader.setHeaderAlias(map).readAll(MysteriousMerchantShop.class);
        reader.close();
        return list;
    }

    public synchronized static void exchange(MessageEvent event){
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        Group group = null;
        if(subject instanceof Group){
            group = (Group) subject;
        }
        if(Objects.isNull(group)){
            return;
        }
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        if(Objects.isNull(userInfo)){
            return;
        }

        String[] s = event.getMessage().serializeToMiraiCode().split(" ");
        if (s.length != 2) {
            Log.error("[抢购 格式不正确]");
            return;
        }
        String goodCode = s[1];
        if(!goodCode.startsWith(Constant.MM_PROP_START)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入完整号码SS-xxx"));;
//            goodCode = Constant.MM_PROP_START + goodCode;
            return;
        }
        MysteriousMerchantShop shop = MysteriousMerchantManager.getShopGoodCode(goodCode);
        if(Objects.isNull(shop) || Objects.isNull(shop.getChangeType())){
            return;
        }

        // 查询商品是否在售
        MysteriousMerchantGoods goods = MysteriousMerchantManager.getShopGoodByGoodCode(goodCode, group.getId());
        if(Objects.isNull(goods)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "商品暂未上架"));
            return;
        }

        String shopGoodRedisKey = getShopGoodRedisKey(goods);
        int stored = (Integer) Optional.ofNullable(RedisUtils.getKeyObject(shopGoodRedisKey)).orElse(0);
        if (stored <= 0) {
            RedisUtils.setKeyObject(shopGoodRedisKey, 0);
            subject.sendMessage(MessageUtil.formatMessageChain(message, "已售罄"));
            return;
        }
        // 查询是否已经购买
        Long senderId = event.getSender().getId();
        String shopGoodUserRedisKey = getShopGoodUserRedisKey(goods, senderId);
        if(Objects.nonNull(RedisUtils.getKeyObject(shopGoodUserRedisKey))){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "限购喽"));
            return;
        }
        // redis增加库存
        RedisUtils.setKeyObject(shopGoodRedisKey, (int)RedisUtils.getKeyObject(shopGoodRedisKey) - 1);
        RedisUtils.setKeyObject(shopGoodUserRedisKey, 1);



        // 判断抢购规则
        if(CHANGE_TYPE_PROP.equals(shop.getChangeType())){
            // 查询用户背包
           List<UserBackpack> prop2List =  Optional.ofNullable(userInfo.getBackpacks()).orElse(Lists.newArrayList())
                    .stream().filter(back-> back.getPropsCode().equals(shop.getProp2Code()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(prop2List) || prop2List.size() < shop.getProp2Count()) {
                RedisUtils.setKeyObject(shopGoodRedisKey, (int)RedisUtils.getKeyObject(shopGoodRedisKey) + 1);
                RedisUtils.deleteKeyString(shopGoodUserRedisKey);
                return;
            }
            // 用户减去兑换的道具
            for (int i = 0; i < shop.getProp2Count(); i++) {
                PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(shop.getProp2Code());
                UserInfo currentUserInfo = UserManager.getUserInfo(event.getSender());
                PluginManager.getPropsManager().deleteProp(currentUserInfo, propsBase);
            }

        }

        if(CHANGE_TYPE_BB.equals(shop.getChangeType())){
            double userMoney  = EconomyUtil.getMoneyByUser(event.getSender());
            if(userMoney < shop.getBbCount()){
                RedisUtils.setKeyObject(shopGoodRedisKey, (int)RedisUtils.getKeyObject(shopGoodRedisKey) + 1);
                RedisUtils.deleteKeyString(shopGoodUserRedisKey);
                return;
            }
            if (!EconomyUtil.minusMoneyToUser(event.getSender(), shop.getBbCount())) {
                Log.warning("道具系统:减少余额失败!");
                subject.sendMessage("系统出错，请联系主人!");
                return;
            }
        }

        if(CHANGE_TYPE_SEASON.equals(shop.getChangeType())){
            double userBankMoney  = EconomyUtil.getMoneyByBank(event.getSender());
            if(userBankMoney < shop.getSeasonMoney()){
                RedisUtils.setKeyObject(shopGoodRedisKey, (int)RedisUtils.getKeyObject(shopGoodRedisKey) + 1);
                RedisUtils.deleteKeyString(shopGoodUserRedisKey);
                return;
            }
            if (!EconomyUtil.minusMoneyToBank(event.getSender(), shop.getSeasonMoney())) {
                Log.warning("道具系统:减少余额失败!");
                subject.sendMessage("系统出错，请联系主人!");
                return;
            }
        }
        // 更新库存
        if(!updateShopGoodsStore(goods)){
            RedisUtils.setKeyObject(shopGoodRedisKey, (int)RedisUtils.getKeyObject(shopGoodRedisKey) + 1);
            RedisUtils.deleteKeyString(shopGoodUserRedisKey);
            return;
        }
        // 增加用户抢购记录
        addExchangeRecordsLog(SETTING_ID, goodCode, group.getId(), senderId);
        // 用户新增道具
        PropsBase prop1Code = PropsCardFactory.INSTANCE.getPropsBase(shop.getProp1Code());

        //  增加道具
        //  PluginManager.getPropsManager().addProp(userInfo, prop1Code);
        int prop1Count = shop.getProp1Count();
        for (int i = 0; i < prop1Count; i++) {
            UserInfo newUserInfo = UserManager.getUserInfo(event.getSender());
            PluginManager.getPropsManager().addProp(newUserInfo, prop1Code);
        }

        // 发送消息
        String content = String.format("\uD83C\uDF89恭喜%s抢购到了%s", group.get(event.getSender().getId()).getNameCard(),
                prop1Code.getName() + " x " + prop1Count);
        subject.sendMessage(MessageUtil.formatMessageChain(message, content));
    }

    private static void addExchangeRecordsLog(Long settingId, String goodCode, long groupId, Long senderId) {
        ExchangeRecordsLog log = new ExchangeRecordsLog();
        log.setSettingId(settingId);
        log.setGroupId(groupId);
        log.setUserId(senderId);
        log.setGoodCode(goodCode);
        log.saveOrUpdate();
    }

    private static Boolean updateShopGoodsStore(MysteriousMerchantGoods goods) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                return HibernateUtil.factory.fromTransaction(session -> {
                    MysteriousMerchantGoods storedGoods = session.get(MysteriousMerchantGoods.class, goods.getId());
                    if (storedGoods != null) {
                        if (storedGoods.getGoodStored() > 0) {
                            // 库存减1
                            storedGoods.setGoodStored(storedGoods.getGoodStored() - 1);
                            storedGoods.setSold(storedGoods.getSold() + 1);
                            session.update(storedGoods);
                            return true;
                        } else {
                            Log.error("库存不足");
                        }
                    } else {
                        Log.error("商品不存在");
                    }
                    return false;
                });
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= MAX_RETRY_COUNT) {
                    Log.error("更新库存失败，达到最大重试次数");
                    return false;
                }
            }
        }
        return false;
    }


    private static MysteriousMerchantGoods getShopGoodByGoodCode(String goodCode, long groupId) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantGoods> query = builder.createQuery(MysteriousMerchantGoods.class);
            JpaRoot<MysteriousMerchantGoods> goods = query.from(MysteriousMerchantGoods.class);
            query.select(goods).where(
                    builder.equal(goods.get("goodCode"), goodCode),
                    builder.equal(goods.get("groupId"), groupId)
            );
            return session.createQuery(query).getSingleResultOrNull();
        });
    }

    // 商品根据settingId删除
    public static void deleteGoodBySettingId(Long settingId , Long groupId) {
        List<MysteriousMerchantGoods> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantGoods> query = builder.createQuery(MysteriousMerchantGoods.class);
            JpaRoot<MysteriousMerchantGoods> from = query.from(MysteriousMerchantGoods.class);
            query.select(from);
            if (Objects.nonNull(groupId)) {
                query.where(builder.equal(from.get("settingId"), settingId), builder.equal(from.get("groupId"), groupId));
            }else{
                query.where(builder.equal(from.get("settingId"), settingId));
            }
            return session.createQuery(query).list();
        });
        Log.info("deleteGoodBySettingId - 上次剩余数量：" + list.size());
        if(CollectionUtils.isNotEmpty(list)){
            Log.info("delete");
            list.forEach(MysteriousMerchantGoods::remove);
        }
        // 兑换记录一并删除
        deleteExchangeRecordsLogBySettingId(settingId, groupId);

        // 删除缓存
        List<String> shopGoodKeys  = RedisUtils.getLikeRedisKeys(getShopGoodRedisKeyPrefix(settingId, groupId));
        shopGoodKeys.forEach(RedisUtils::deleteKeyString);

        List<String> shopGoodUserKeys  = RedisUtils.getLikeRedisKeys(getShopGoodUserRedisKeyPrefix(settingId, groupId));
        shopGoodUserKeys.forEach(RedisUtils::deleteKeyString);
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
        List<ExchangeRecordsLog> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<ExchangeRecordsLog> query = builder.createQuery(ExchangeRecordsLog.class);
            JpaRoot<ExchangeRecordsLog> from = query.from(ExchangeRecordsLog.class);
            query.select(from);
            if (Objects.nonNull(groupId)) {
                query.where(builder.equal(from.get("settingId"), settingId), builder.equal(from.get("groupId"), groupId));
            }else{
                query.where(builder.equal(from.get("settingId"), settingId));
            }
            return session.createQuery(query).list();
        });

        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(ExchangeRecordsLog::remove);
        }

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
            String redisKey = getShopGoodRedisKey(good);
            RedisUtils.setKeyObject(redisKey, good.getGoodStored());
        }
    }

    public static String getShopGoodRedisKey(MysteriousMerchantGoods goods){
        return getShopGoodRedisKeyPrefix(goods.getSettingId(), goods.getGroupId()) +  goods.getGoodCode();
    }
    public static String getShopGoodRedisKeyPrefix(Long settingId, Long groupId){
        return SHOP_GOOD_KEY + settingId + groupId;
    }

    public static String getShopGoodUserRedisKey(MysteriousMerchantGoods goods, Long userId){
        return getShopGoodUserRedisKeyPrefix(goods.getSettingId(), goods.getGroupId()) + goods.getGoodCode() + userId;
    }

    public static String getShopGoodUserRedisKeyPrefix(Long settingId, Long groupId){
        return SHOP_GOOD_USER_KEY + settingId + groupId;
    }

    public static void saveShopGoodList(List<MysteriousMerchantShop> shopList) {
        shopList.stream().forEach(MysteriousMerchantShop::saveOrUpdate);
    }

    public static List<MysteriousMerchantShop> getPermanentGoodCodeList() {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantShop> query = builder.createQuery(MysteriousMerchantShop.class);
            JpaRoot<MysteriousMerchantShop> shop = query.from(MysteriousMerchantShop.class);
            query.select(shop).where(builder.equal(shop.get("permanent"), true));
            query.orderBy(builder.asc(shop.get("goodCode"))); // 按照 goodCode 正序排序
            return session.createQuery(query).list();
        });
    }
}
