package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.Getter;
import lombok.Setter;

import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Pattern;

@Getter
@Setter
public class Glassbead extends AbstractPropUsage {

    public Glassbead() {
    }

    @Override
    public boolean checkOrder() {

        String no = PropsType.getNo(propsCard.getCode());

        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "@指定对象]"));
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

    @Override
    public void excute() {
        User sender = event.getSender();
        // 消耗品，对指定目标使用，获得目标的币币（随机20-100）
        int money = RandomUtil.randomInt(40, 200);
        NormalMember member = group.get(target);
        double moneyByUser = EconomyUtil.getMoneyByUser(member);
        if (moneyByUser - money < 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "目标用户的币币不够" + money));
            return;
        }
        // 减去目标用户
        EconomyUtil.minusMoneyToUser(member, money);
        // 自己获得
        EconomyUtil.plusMoneyToUser(sender, money);
        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append("成功获得").append(new At(target).getDisplay(group))
                .append("的" + money + "币币")
                .build());
    }


}
