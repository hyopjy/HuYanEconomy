package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.utils.EconomyUtil;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;


public class GAP extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        return checkOrderDefault();
    }

    @Override
    public void excute() {
        User sender = event.getSender();

        EconomyUtil.plusMoneyToUser(sender, 999999);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append("\r\n")
                .append(propsCard.getDescription())
                .append("\r\n")
                .build());
    }
}
