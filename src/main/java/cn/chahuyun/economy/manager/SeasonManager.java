package cn.chahuyun.economy.manager;


import cn.chahuyun.economy.constant.DailyPropCode;
import cn.chahuyun.economy.constant.FishSignConstant;
import cn.chahuyun.economy.dto.BadgeFishInfoDto;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.entity.fish.FishRanking;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.FishManager;
import cn.chahuyun.economy.plugin.FishPondManager;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import jakarta.persistence.criteria.Root;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.redisson.api.RBloomFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 赛季管理
 */
public class SeasonManager {

    /**
     * 重置鱼塘
     *

     */
    public static void reloadFishPod(UserMessageEvent event){
        List<FishRanking> fishRankList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<FishRanking> query = builder.createQuery(FishRanking.class);
            query.select(query.from(FishRanking.class));
            return session.createQuery(query).list();
        });

        fishRankList.stream().forEach(fishRanking -> {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(fishRanking);
                return null;
            });
        });

        List<Fish> fishList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Fish> query = builder.createQuery(Fish.class);
            query.select(query.from(Fish.class));
            return session.createQuery(query).list();
        });
        fishList.stream().forEach(fish -> {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(fish);
                return null;
            });
        });

        FishManager.fishMap.clear();
        FishManager.init();
        GamesManager.playerCooling.clear();
        GamesManager.refresh(event);
        FishPondManager.refresh(event);
        Log.info("重新加载完成！");

    }

    /**
     * 刷新道具
     */
    public static void reloadPropsFishCard(){
        PluginManager.refreshPropsFishCard();
        Log.info("刷新道具完成");
    }

    public static void clearSeasonMoney() {
        CompetitionSeasonManager.clearUserBankMoney();
    }

    /**
     * 清理用户包内道具信息-下架道具
     */
    public static void clearUserPackOffline() {
        BackpackManager.clearOffLineProp();
        Log.info("清理用户包内道具信息");
    }

    /**
     * 点亮鱼竿成就

     */
    public static void lightUpFishRod() {
        List<BadgeFishInfoDto> badgeFishInfoDtos = FishInfoManager.getBadgeFishInfoDto();
        badgeFishInfoDtos.stream().forEach(badgeFishInfoDto -> {
            BadgeInfoManager.updateOrInsertBadgeInfo(badgeFishInfoDto.getGroupId(), badgeFishInfoDto.getQq(),
                    FishSignConstant.FISH_ROD_LEVEL, null, null);
        });
        Log.info("点亮鱼竿成就");
    }

    /**
     * 清理排行榜

     */
    public static void clearFishRank() {
        FishRankingManger.clearFishRanking();
        Log.info("清理排行榜");

    }

    public static void resetWditBB() {
        CompetitionSeasonManager.resetWditBB();
        Log.info("重置wditbb");
    }

    public static void importShopInfo() {
        MysteriousMerchantManager.importShopInfo();
        Log.info("神秘商品导入");
    }


    public static void checkUserDailyWork(MessageEvent event, Contact subject) {
        Long userId = event.getSender().getId();
        Long groupId = subject.getId();
        RBloomFilter<Long> rBloomFilter =  RedisUtils.dailyWorkBloomFilterInit(groupId);
        if(rBloomFilter.contains(userId)){
            return;
        }
        // 面罩34三次
        Boolean maskCount = CacheUtils.checkMaskCountKey(groupId, userId);
        // 购买币币51
        Boolean by51Count = RedisUtils.getWditBBCount(groupId, userId) > 0;
        //  签到
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        Boolean sign = false;
        if (Objects.nonNull(userInfo) && userInfo.isSign()) {
            sign = true;
        }
        if(maskCount && by51Count && sign ){
            rBloomFilter.add(userId);
            PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(DailyPropCode.FISH_101);
            PluginManager.getPropsManager().addProp(userInfo, propsBase);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "恭喜你获得" + propsBase.getName() + "x1"));
        }
    }

    public static Integer clearPropCode(String propCode) {
        List<String> propCodeList = new ArrayList<String>();
        propCodeList.add(propCode);
        int p =  HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaDelete<UserBackpack> delete = builder.createCriteriaDelete(UserBackpack.class);
            Root<UserBackpack> e = delete.from(UserBackpack.class);
            delete.where(builder.in(e.get("propsCode")).value(propCodeList));
            return session.createQuery(delete).executeUpdate();
        });
        Log.info("清理"+ propCode + ":"+ p);
        return p;
    }
}
