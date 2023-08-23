package cn.chahuyun.economy.excu;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.manager.UserManager;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class GameFishThread extends Thread {
    private MessageEvent event;


    /**
     * 钓鱼接口
     */
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());

        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();
        Contact subject = event.getSubject();
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();

        //获取鱼塘
        FishPond fishPond = userFishInfo.getFishPond();
        if (fishPond == null) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "默认鱼塘不存在!"));
            return;
        }
        fishPond.setName("日夜颠岛");
        //获取鱼塘限制鱼竿最低等级
        int minLevel = fishPond.getMinLevel();
        if (userFishInfo.getRodLevel() < minLevel) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "鱼竿等级太低，bobo拒绝你在这里钓鱼\uD83D\uDE45\u200D♀️"));
            return;
        }
        String userName = userInfo.getName();
        if (Objects.nonNull(group)) {
            NormalMember member = group.get(userInfo.getQq());
            if (Objects.nonNull(member)) {
                userName = member.getNameCard();
            }
        }

        //开始钓鱼
        String start = String.format("%s开始钓鱼\n鱼塘:%s\n等级:%s\n最低鱼竿等级:%s\n%s", userName, fishPond.getName(), fishPond.getPondLevel(), fishPond.getMinLevel(), fishPond.getDescription());
        subject.sendMessage(start);
        Log.info(String.format("%s开始钓鱼", userInfo.getName()));

        String[] errorMessages = new String[]{"钓鱼失败:哎呀，风吹的……", "钓鱼失败:哎呀，眼花了……", "钓鱼失败:bobo摇头", "钓鱼失败:呀！切线了！", "钓鱼失败:什么都没有钓上来！"};

        //随机睡眠
        try {
            Thread.sleep(RandomUtil.randomInt(5 * 60 * 1000, 60 * 60 * 1000));
//            Thread.sleep(RandomUtil.randomInt(100, 6000));
        } catch (InterruptedException e) {
            Log.debug(e);
        }


    }
}
