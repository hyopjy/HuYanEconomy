package cn.chahuyun.economy.manager;


import cn.chahuyun.economy.constant.FishSignConstant;
import cn.chahuyun.economy.dto.BadgeFishInfoDto;
import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.fish.FishRanking;
import cn.chahuyun.economy.plugin.FishManager;
import cn.chahuyun.economy.plugin.FishPondManager;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.UserMessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;

import java.util.List;

/**
 * 赛季管理
 */
public class SeasonManager {

    /**
     * 重置鱼塘
     *
     * @param event
     */
    public static void reloadFishPod(UserMessageEvent event){
        Contact subject = event.getSubject();
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
        subject.sendMessage(MessageUtil.formatMessageChain("重置鱼塘成功"));
    }

    /**
     * 刷新道具
     */
    public static void reloadPropsFishCard(UserMessageEvent event){
        Contact subject = event.getSubject();
        PluginManager.refreshPropsFishCard();
        subject.sendMessage(MessageUtil.formatMessageChain("刷新道具完成"));
    }

    public static void clearSeasonMoney(UserMessageEvent event) {
        Contact subject = event.getSubject();
        CompetitionSeasonManager.clearUserBankMoney();
        subject.sendMessage(MessageUtil.formatMessageChain("清理赛季币完成"));
    }

    /**
     * 清理用户包内道具信息-下架道具
     * @param event
     */
    public static void clearUserPackOffline(UserMessageEvent event) {
        Contact subject = event.getSubject();
        BackpackManager.clearOffLineProp();
        subject.sendMessage(MessageUtil.formatMessageChain("清理用户包内道具信息完成"));
    }

    /**
     * 点亮鱼竿成就
     * @param event
     */
    public static void lightUpFishRod(UserMessageEvent event) {
        Contact subject = event.getSubject();
        List<BadgeFishInfoDto> badgeFishInfoDtos = FishInfoManager.getBadgeFishInfoDto();
        badgeFishInfoDtos.stream().forEach(badgeFishInfoDto -> {
            BadgeInfoManager.updateOrInsertBadgeInfo(badgeFishInfoDto.getGroupId(), badgeFishInfoDto.getQq(),
                    FishSignConstant.FISH_ROD_LEVEL, null, null);
        });
        subject.sendMessage(MessageUtil.formatMessageChain("点亮鱼竿成就结束"));
    }

    /**
     * 清理排行榜
     * @param event
     */
    public static void clearFishRank(UserMessageEvent event) {
        Contact subject = event.getSubject();
        FishRankingManger.clearFishRanking();
        subject.sendMessage(MessageUtil.formatMessageChain("清理排行榜数据成功"));
    }

    public static void resetWditBB(UserMessageEvent event) {
        Contact subject = event.getSubject();
        CompetitionSeasonManager.resetWditBB();
        subject.sendMessage(MessageUtil.formatMessageChain("重置wdit bb"));
    }
}
