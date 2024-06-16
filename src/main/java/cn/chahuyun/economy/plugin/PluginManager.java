package cn.chahuyun.economy.plugin;

import cn.chahuyun.config.ConfigData;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.manager.PropsManager;
import cn.chahuyun.economy.manager.PropsManagerImpl;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.CronUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 插件管理<p>
 *
 * @author Moyuyanli
 * @date 2022/11/15 16:03
 */
public class PluginManager {

    /**
     * 是否加载壶言会话插件
     */
    public static boolean isHuYanSessionPlugin;

    /**
     * 插件的道具管理
     */
    private static PropsManager propsManager = new PropsManagerImpl();

    private PluginManager() {
    }

    /**
     * 初始化插件道具系统
     *
     * @author Moyuyanli
     * @date 2022/11/23 10:50
     */
    public static void init() {
        //插件加载的时候启动调度器
        CronUtil.start();
        //加载道具
//        PropsCard propsCard = new PropsCard(Constant.SIGN_DOUBLE_SINGLE_CARD, "签到双倍币币卡", 99, true, "张", "不要999，不要599，只要199币币，你的下一次签到将翻倍！", false, null, null, false, null);
//        propsManager.registerProps(propsCard);
        // todo 重置
        initPropsFishCard();
        try {
            //壶言会话
            HuYanEconomy.INSTANCE.config.setOwner(ConfigData.INSTANCE.getOwner());
            Log.info("检测到壶言会话,已同步主人!");
            isHuYanSessionPlugin = true;
        } catch (NoClassDefFoundError e) {
            isHuYanSessionPlugin = false;
        }

    }

    private static void initPropsFishCard() {
        // 缓存
        propsManager.clearProps();

        List<PropsFishCard> PropsFishCardList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PropsFishCard> query = builder.createQuery(PropsFishCard.class);
            query.select(query.from(PropsFishCard.class));
            return session.createQuery(query).list();
        });

        if (CollectionUtils.isEmpty(PropsFishCardList)) {
            reloadPropsFishCard();
            return;
        }

        PropsFishCardList.stream().forEach(propsFishConfig -> {
            propsManager.registerProps(propsFishConfig);
        });

    }

    private static void reloadPropsFishCard() {
        List<PropsFishCard> propsFishConfigList = getExcelData();
        propsFishConfigList.stream().forEach(propsFishConfig -> {
            PropsFishCard propsFishCard = new PropsFishCard(propsFishConfig.getCode(),
                    propsFishConfig.getName(), propsFishConfig.getCost(),
                    propsFishConfig.getDescription(),
                    propsFishConfig.getFishDesc(),
                    propsFishConfig.getContent(),
                    propsFishConfig.getBuy(),
                    propsFishConfig.getPriceDesc(),
                    propsFishConfig.getExchange(),
                    propsFishConfig.getDelete(),
                    propsFishConfig.getTradable(),
                    propsFishConfig.getOffShelf()
            );
            PropsFishCard finalPropsFishCard = propsFishCard.save();
            propsManager.registerProps(finalPropsFishCard);
        });
    }

    /**
     * 获取道具管理实现
     *
     * @return 道具管理实现
     */
    public static PropsManager getPropsManager() {
        return propsManager;
    }

    /**
     * 设置道具管理的实现
     *
     * @param propsManager 道具管理类
     */
    public static void setPropsManager(PropsManager propsManager) {
        PluginManager.propsManager = propsManager;
    }

    /**
     * 更新鱼塘-- 有就 更新 没有就创建
     */
    public static void refreshPropsFishCard() {
        // 缓存
        propsManager.clearProps();

        /**
         * 查出数据库有的道具
         */
        List<PropsFishCard> PropsFishCardList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PropsFishCard> query = builder.createQuery(PropsFishCard.class);
            query.select(query.from(PropsFishCard.class));
            return session.createQuery(query).list();
        });
        Map<String, PropsFishCard> maps = PropsFishCardList.stream().collect(Collectors.toMap(
                PropsFishCard::getCode, Function.identity()));


        List<PropsFishCard> propsFishConfigList = getExcelData();

        propsFishConfigList.forEach(propsFishConfig -> {
            PropsFishCard propsFishCard = null;
            if(Objects.nonNull(maps.get(propsFishConfig.getCode()))){
                propsFishCard = new PropsFishCard(maps.get(propsFishConfig.getCode()).getId()
                        ,propsFishConfig.getCode(),
                        propsFishConfig.getName(), propsFishConfig.getCost(),
                        propsFishConfig.getDescription(),
                        propsFishConfig.getFishDesc(),
                        propsFishConfig.getContent(),
                        propsFishConfig.getBuy(),
                        propsFishConfig.getPriceDesc(),
                        propsFishConfig.getExchange(),
                        propsFishConfig.getDelete(),
                        propsFishConfig.getTradable(),
                        propsFishConfig.getOffShelf()
                );
            }else {
                propsFishCard = new PropsFishCard(propsFishConfig.getCode(),
                        propsFishConfig.getName(), propsFishConfig.getCost(),
                        propsFishConfig.getDescription(),
                        propsFishConfig.getFishDesc(),
                        propsFishConfig.getContent(),
                        propsFishConfig.getBuy(),
                        propsFishConfig.getPriceDesc(),
                        propsFishConfig.getExchange(),
                        propsFishConfig.getDelete(),
                        propsFishConfig.getTradable(),
                        propsFishConfig.getOffShelf()
                );
            }
            PropsFishCard finalPropsFishCard = propsFishCard.save();
            propsManager.registerProps(finalPropsFishCard);
        });

    }

    private static List<PropsFishCard> getExcelData() {
        HuYanEconomy instance = HuYanEconomy.INSTANCE;
        ExcelReader reader = ExcelUtil.getReader(instance.getResourceAsStream("fish_2406.xlsx"), 1);
        Map<String, String> map = new HashMap<>();
        map.put("编号", "code");
        map.put("道具", "name");
        map.put("钓鱼描述", "fishDesc");
        map.put("道具描述", "description");
        map.put("价格", "cost");
        map.put("备注", "content");
        map.put("是否可以购买", "buy");
        map.put("价格描述","priceDesc");
        map.put("是否兑换","exchange");
        // 无使用效果 直接兑换
        map.put("是否直接兑换","delete");
        map.put("是否可交易","tradable");
        map.put("是否下架","offShelf");
        List<PropsFishCard> list = reader.setHeaderAlias(map).readAll(PropsFishCard.class);
        reader.close();
        return list;
    }
}
