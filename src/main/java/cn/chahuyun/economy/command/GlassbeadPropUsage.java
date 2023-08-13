package cn.chahuyun.economy.command;


import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.Objects;
import java.util.regex.Pattern;

public class GlassbeadPropUsage extends PropUsage {
    private Long target;

    public GlassbeadPropUsage() {
    }


    public GlassbeadPropUsage(PropsFishCard propsCard,  UserInfo userInfo,MessageEvent event) {
        this.event = event;
        this.userInfo = userInfo;
        this.propsCard = propsCard;
    }
    @Override
    public void execute(int num) {
        Contact subject = event.getSubject();
       Group group =null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        if(Objects.isNull(group)){
            // 删除使用道具卡状态
            doRemove(userInfo.getQq());
            return;
        }
        User sender = event.getSender();
        // 消耗品，对指定目标使用，获得目标的币币（随机20-100）
        for (int i = 0; i < num; i++) {
            int money = RandomUtil.randomInt(20, 100);
            NormalMember member = group.get(target);
            double moneyByUser = EconomyUtil.getMoneyByUser(member);
            if (moneyByUser - money < 0) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "目标用户的币币不够"+money));
                // 删除使用道具卡状态
                doRemove(userInfo.getQq());
                return;
            }
            // 减去目标用户
            EconomyUtil.minusMoneyToUser(member, money);
            // 自己获得
            EconomyUtil.plusMoneyToUser(sender, money);
            subject.sendMessage(new MessageChainBuilder()
                            .append(new QuoteReply(event.getMessage()))
                            .append(propsCard.getName() + " 道具卡使用成功").append("\r\n")
                            .append("成功获得")
                            .append(new At(target).getDisplay(group))
                            .append("的" + money + "币币")
                            .build());
        }
        // 删除使用道具卡状态
        doRemove(userInfo.getQq());
    }

    @Override
    public Boolean checkOrder(String order) {
        String match = "使用 玻璃珠(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 玻璃珠@指定对象]"));
            return false;
        }
        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                this.target = at.getTarget();
            }
        }
        return true;
    }
}
