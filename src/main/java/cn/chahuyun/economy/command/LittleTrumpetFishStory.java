package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Pattern;

/**
 * 《小喇叭鱼的故事》
 */
public class LittleTrumpetFishStory extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        // 消耗小喇叭鱼和流言蜚鱼获得，使用后让bobo为大家讲述一个故事，且使用者获得13140币币
        User sender = event.getSender();

        // 自己获得
        EconomyUtil.plusMoneyToUser(sender, 131400);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append("成功获得131400币币")
                .append("----------\r\n")
                .append("开始讲述《小喇叭鱼的故事》的故事 \r\n")
                .append(propsCard.getContent())
                .build());

    }
}
