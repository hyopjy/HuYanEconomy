package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.utils.EconomyUtil;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

public class TheShellTrumpet extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        return this.checkOrderDefault();
    }

    @Override
    public void excute() {
        User sender = event.getSender();

        // 自己获得
        EconomyUtil.plusMoneyToUser(sender, 666);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append("成功获得666币币")
                .build());

        Message m = new PlainText("开始吹响一段贼难听的《小螺号》🎵🎵 \r\n");
        m = m.plus(propsCard.getContent());
        subject.sendMessage(m);
    }
}
