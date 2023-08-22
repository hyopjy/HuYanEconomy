package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.economy.entity.LotteryInfo;

import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.entity.fish.FishRanking;
import cn.chahuyun.economy.manager.BackpackManager;
import cn.chahuyun.economy.manager.GamesManager;
import cn.chahuyun.economy.plugin.FishManager;
import cn.chahuyun.economy.plugin.FishPondManager;
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
import net.mamoe.mirai.message.data.PlainText;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;


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
            int money = RandomUtil.randomInt(100, 800);
            EconomyUtil.plusMoneyToUser(sender, money);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "恭喜你获得" +money +"WDIT 币币"));
        }

        if(message.equals("余额")){
            double money = EconomyUtil.getMoneyByUser(sender);
            double bankMoney = EconomyUtil.getMoneyByBank(sender);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "当前WDIT 币币余额%s;当前银行存款%s", money, bankMoney));
        }
        if(message.equals("查看签签")){
            List<LotteryInfo> list = HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<LotteryInfo> query = builder.createQuery(LotteryInfo.class);
                JpaRoot<LotteryInfo> from = query.from(LotteryInfo.class);
                query.select(from);
                query.where(builder.equal(from.get("qq"), sender.getId()),builder.equal(from.get("group"), sender.getGroup().getId()));
                return session.createQuery(query).list();
            });
            if(CollectionUtils.isEmpty(list)){
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "暂时没有签"));
            }else {
                Message m = new PlainText("");
                // 强制透
                List<LotteryInfo> grandLotto = list.stream().filter(lotteryInfo -> lotteryInfo.getType() == 1).collect(Collectors.toList());
                // 缺德球
                List<LotteryInfo> union = list.stream().filter(lotteryInfo -> lotteryInfo.getType() == 2).collect(Collectors.toList());

                if(!CollectionUtils.isEmpty(grandLotto)){
                    m = m.plus("强制透签：").plus("\r\n");
                   for(int i = 0 ; i < grandLotto.size(); i++ ) {
                       m = m.plus("号码：" ).plus(grandLotto.get(i).getNumber()).plus(" 币币：" + grandLotto.get(i).getMoney()).plus("\r\n");
                   }
                }

                if(!CollectionUtils.isEmpty(union)){
                    m = m.plus("缺德球签：").plus("\r\n");
                    for(int i = 0 ; i < union.size(); i++ ) {
                        m = m.plus("号码：" ).plus(union.get(i).getNumber()).plus(" 币币：" + union.get(i).getMoney()).plus("\r\n");
                    }
                }
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), m.contentToString()));
            }

        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public synchronized ListeningStatus onUserMessage(UserMessageEvent event) {
        User sender = event.getSender();
        Contact subject = event.getSubject();
        String message = event.getMessage().serializeToMiraiCode();

        if (message.equals("重置鱼塘") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
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

            FishManager.init();
            GamesManager.playerCooling.clear();
            FishManager.fishMap.clear();
            GamesManager.refresh(event);
            FishPondManager.refresh(event);
            Log.info("重新加载完成！");
            subject.sendMessage(MessageUtil.formatMessageChain("重新加载完成"));
        }

//        if (message.equals("刷新道具") &&
//                EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
//            PluginManager.refreshPropsFishCard();
//            subject.sendMessage(MessageUtil.formatMessageChain("刷新道具完成"));
//        }

        // 清理用户背包自动钓鱼机
        if (message.equals("自动钓鱼机") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
            BackpackManager.clearFishMachine(event);
        }


        return ListeningStatus.LISTENING;
    }
}
