package cn.chahuyun.economy.plugin;

import cn.chahuyun.config.ConfigData;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.props.PropsCard;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.manager.PropsManager;
import cn.chahuyun.economy.manager.PropsManagerImpl;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.CronUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;

import java.util.*;

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
        propsManager.clearProps();
        //加载道具
        PropsCard propsCard = new PropsCard(Constant.SIGN_DOUBLE_SINGLE_CARD, "签到双倍币币卡", 99, true, "张", "不要999，不要599，只要199币币，你的下一次签到将翻倍！", false, null, null, false, null);
        propsManager.registerProps(propsCard);
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
        List<PropsFishCard> PropsFishCardList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PropsFishCard> query = builder.createQuery(PropsFishCard.class);
            query.select(query.from(PropsFishCard.class));
            return session.createQuery(query).list();
        });

        if (PropsFishCardList == null || PropsFishCardList.size() == 0) {
            reloadPropsFishCard();
            return;
        }

        PropsFishCardList.stream().forEach(propsFishConfig -> {
            PropsFishCard propsFishCard = new PropsFishCard(propsFishConfig.getCode(),
                    propsFishConfig.getName(), propsFishConfig.getCost(),
                    propsFishConfig.getDescription(),
                    propsFishConfig.getFishDesc(),
                    propsFishConfig.getContent(),
                    propsFishConfig.getBuy());
            propsFishCard.save();
            propsManager.registerProps(propsFishCard);
        });

    }

    private static void reloadPropsFishCard() {
        HuYanEconomy instance = HuYanEconomy.INSTANCE;
        ExcelReader reader = ExcelUtil.getReader(instance.getResourceAsStream("fish.xlsx"), 1);
        Map<String, String> map = new HashMap<>();
        map.put("编号", "code");
        map.put("道具", "name");
        map.put("钓鱼描述", "fishDesc");
        map.put("道具描述", "description");
        map.put("价格", "cost");
        map.put("备注", "content");
        map.put("是否可以购买", "buy");
        List<PropsFishCard> propsFishConfigList = reader.setHeaderAlias(map).readAll(PropsFishCard.class);
        propsFishConfigList.stream().forEach(propsFishConfig -> {
            PropsFishCard propsFishCard = new PropsFishCard(propsFishConfig.getCode(),
                    propsFishConfig.getName(), propsFishConfig.getCost(),
                    propsFishConfig.getDescription(),
                    propsFishConfig.getFishDesc(),
                    propsFishConfig.getContent(),
                    propsFishConfig.getBuy());
            propsFishCard.save();
            propsManager.registerProps(propsFishCard);
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
}
