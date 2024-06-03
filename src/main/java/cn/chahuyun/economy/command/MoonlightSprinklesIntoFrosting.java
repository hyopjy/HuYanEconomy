package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.utils.EconomyUtil;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;

public class MoonlightSprinklesIntoFrosting extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        return this.checkOrderDefault();
    }

    @Override
    public void excute() {
        User sender = event.getSender();
        // 自己获得
        EconomyUtil.plusMoneyToUser(sender, 7800000);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功")
                .append("成功获得7800000币币")
                .append("\r\n")
                .append(propsCard.getContent())
                .build());
    }
}
