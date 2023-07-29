package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.economy.manager.GamesManager;
import cn.chahuyun.economy.plugin.FishManager;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.Message;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RandomMoneyListener extends SimpleListenerHost {

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        if (exception instanceof EventCancelledException) {
            Log.error("发送消息被取消:", exception);
        } else if (exception instanceof BotIsBeingMutedException) {
            Log.error("你的机器人被禁言:", exception);
        } else if (exception instanceof MessageTooLargeException) {
            Log.error("发送消息过长:", exception);
        } else if (exception instanceof IllegalArgumentException) {
            Log.error("发送消息为空:", exception);
        }

        // 处理事件处理时抛出的异常
        Log.error(exception);
    }

    @EventHandler()
    public synchronized ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Contact subject = event.getSubject();
        String message = event.getMessage().serializeToMiraiCode();
        if (message.equals("WDIT") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
            int money = RandomUtil.randomInt(30, 365);
            EconomyUtil.plusMoneyToUser(sender, money);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "恭喜你获得" +money +"WDIT 币币"));
        }

        if(message.equals("余额")){
            double money = EconomyUtil.getMoneyByUser(sender);
            double bankMoney = EconomyUtil.getMoneyByBank(sender);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "当前WDIT 币币余额%s;当前银行存款%s", money, bankMoney));
        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public synchronized ListeningStatus onUserMessage(UserMessageEvent event) {
        User sender = event.getSender();
        Contact subject = event.getSubject();
        String message = event.getMessage().serializeToMiraiCode();

        if (message.equals("重置鱼塘") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
           int tempDelete =  HibernateUtil.factory.fromSession(session -> {
               Transaction tx = session.beginTransaction();  //创建transaction实例
               int fish = 0;
               int fishRank = 0;
                try {
                    String rankhql = "delete from FishRanking";
                    Query rankQuery = session.createQuery(rankhql);
                    fishRank = rankQuery.executeUpdate();
                    Log.info("清理排行榜数据"+fishRank );

                    String hql = "delete from Fish";
                    Query query = session.createQuery(hql);
                    fish = query.executeUpdate();

                    // 更新钓鱼人状态
                    tx.commit();            //提交事务
                    return fish;
                } catch (Exception e) {
                    e.printStackTrace();
                    tx.rollback();
                }
                return fish;

            });
            GamesManager.playerCooling.clear();
            FishManager.fishMap.clear();
            GamesManager.refresh(event);
            Log.info("清理数据"+tempDelete + "条");
            FishManager.init();
            Log.info("重新加载完成！");
            subject.sendMessage(MessageUtil.formatMessageChain("重新加载完成"));
        }
        return ListeningStatus.LISTENING;
    }
}
