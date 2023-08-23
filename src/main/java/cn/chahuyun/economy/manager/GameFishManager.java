package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.excu.GameFishThread;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Date;
import java.util.Optional;

public class GameFishManager {

    private static boolean isThreadAlive(String threadName) {
        // 获取所有活动线程
        ThreadGroup group = ThreadUtil.currentThreadGroup();
        int activeCount = group.activeCount();
        Thread[] threads = new Thread[activeCount];
        group.enumerate(threads);
        // 判断线程是否存活
        for (Thread thread : threads) {
            if (thread != null && thread.isAlive() && thread.getName().equals(threadName)) {
                return true;
            }
        }
        return false;
    }

    public static void fishing(MessageEvent event) {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();
        Contact subject = event.getSubject();
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();
        //能否钓鱼
        if (!userFishInfo.isFishRod()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "没有鱼竿，bobo也帮不了你🥹"));
            return;
        }
        //是否已经在钓鱼
        if (userFishInfo.getStatus()) {
            Double constMoney = GamesManager.userPay.get(user.getId());
            Boolean checkUser = GamesManager.checkUserPay(user);
            if (checkUser) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,还你%s💰", Optional.ofNullable(constMoney).orElse(0.0)));
            } else {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
            }
            return;
        }
        //钓鱼冷却
        if (GamesManager.playerCooling.containsKey(userInfo.getQq())) {
            Date date = GamesManager.playerCooling.get(userInfo.getQq());
            long between = DateUtil.between(date, new Date(), DateUnit.MINUTE, true);
            if (between < 5) {
                Double constMoney = GamesManager.userPay.get(user.getId());
                Boolean checkUser = GamesManager.checkUserPay(user);
                if (checkUser) {
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,你还差%s分钟来抛第二杆!,还你%s币币", 5 - between, Optional.ofNullable(constMoney).orElse(0.0)));
                } else {
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
                }
                return;
            } else {
                GamesManager.playerCooling.remove(userInfo.getQq());
            }
        } else {
            GamesManager.playerCooling.put(userInfo.getQq(), new Date());
        }

        //是否已经在钓鱼
        if (userFishInfo.isStatus()) {
            Double constMoney = GamesManager.userPay.get(user.getId());
            Boolean checkUser = GamesManager.checkUserPay(user);
            if (checkUser) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,还你%s💰", Optional.ofNullable(constMoney).orElse(0.0)));
            } else {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
            }
            return;
        }

        String threadName = "userId-groupId-"+user.getId()+"-"+group.getId();
        if(isThreadAlive(threadName)){
            // 卡bug归还
            GamesManager.returnMoney(user,5.00);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,还你%s💰", 5.00));
            return;
        }
        Thread thread = new GameFishThread(event);
        thread.setName(threadName);
        ThreadUtil.execute(thread);
    }
}
