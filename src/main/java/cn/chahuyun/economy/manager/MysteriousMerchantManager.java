package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.merchant.MysteriousMerchantConfig;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.utils.HibernateUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.Objects;

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
    public static MysteriousMerchantConfig open(){
    //    开启神秘商人
        MysteriousMerchantConfig config = getMysteriousMerchantConfigByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantConfig();
            config.setBuyCount(1);
        }
        config.setStatus(true);
        config.saveOrUpdate();
        return config;
    }

    /**
     * 关闭神秘商人
     *

     */
    public static MysteriousMerchantConfig close(){
    //    关闭神秘商人
        MysteriousMerchantConfig config = getMysteriousMerchantConfigByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantConfig();
            config.setBuyCount(1);
        }
        config.setStatus(false);
        config.saveOrUpdate();
        return config;
    }

    /**
     * 设置限购次数
     * @return
     */
    public static MysteriousMerchantConfig setBuyCount(Integer buyCount){
        //    关闭神秘商人
        MysteriousMerchantConfig config = getMysteriousMerchantConfigByKey(1L);
        if(Objects.isNull(config)){
            config = new MysteriousMerchantConfig();
            config.setStatus(false);
        }
        config.setBuyCount(buyCount);
        config.saveOrUpdate();
        return config;
    }

    public static MysteriousMerchantConfig getMysteriousMerchantConfigByKey(Long configId) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<MysteriousMerchantConfig> query = builder.createQuery(MysteriousMerchantConfig.class);
            JpaRoot<MysteriousMerchantConfig> from = query.from(MysteriousMerchantConfig.class);
            query.where(builder.equal(from.get("configId"), configId));
            query.select(from);
            return session.createQuery(query).getSingleResultOrNull();
        });
    }

    /**
     * 设置神秘商人base
     *
     * @param event
     */
    public static MysteriousMerchantSetting setting(List<String> hourList,
                                                    Integer passMinute,
                                                    Integer probability,
                                                    List<String> goodCodeList, Integer randomCount, Integer minStored, Integer maxStored) {
        //    设置神秘商人 14,17,21 10(几分钟消失) 15%   83-92(商品编码范围) 2(几种道具)  1-3(随机道具库存)
        MysteriousMerchantSetting config = new MysteriousMerchantSetting();
        config.setHourStr(String.join(",", hourList));
        config.setPassMinute(passMinute);
        config.setProbability(probability);
        config.setGoodCodeStr(String.join(",", goodCodeList));
        config.setRandomGoodCount(randomCount);
//        config.setMinStored(minStored);
//        config.setMaxStored(maxStored);
//        config.save();
        return config;

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
