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

    private Integer num;

    public Glassbead() {
    }

    @Override
    public boolean checkOrder() {

        String no = PropsType.getNo(propsCard.getCode());

        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )\\d( )*)";
        String match1 = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";

        String code = event.getMessage().serializeToMiraiCode();
        if(!(Pattern.matches(match, code) ||  Pattern.matches(match1, code))){
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
        String[] s = code.split(" ");
        if(s.length > 2){
            this.num = Integer.parseInt(s[2]);
        }else {
            this.num = 1;
        }
        return true;

    }

    @Override
    public void excute() {
        User sender = event.getSender();
        // 消耗品，对指定目标使用，即可打赏目标1000WDIT币币
        int money = 1000 * num;
        NormalMember senderMember = group.get(sender.getId());
        double moneyByUser = EconomyUtil.getMoneyByUser(senderMember);
        if (moneyByUser - money < 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "当前币币不够" + money));
            return;
        }
        // 目标用户增加
        NormalMember targetMember = group.get(sender.getId());
        EconomyUtil.plusMoneyToUser(targetMember, money);
        // 自己减少
        EconomyUtil.minusMoneyToUser(sender, money);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功 ").append("\r\n")
                .append(new At(sender.getId()).getDisplay(group))
                .append(" 成功打赏") .append(new At(target).getDisplay(group))
                .append( " " + money + "玻璃珠！老板大气！")
                .build());
    }


}
