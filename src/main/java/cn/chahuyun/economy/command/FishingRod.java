package cn.chahuyun.economy.command;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.UserManager;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;


public class FishingRod extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        return checkOrderDefault();
    }

    @Override
    public void excute() {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();
        userFishInfo.upFishRod();

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append("升级成功!你的鱼竿更强了!\n%s->%s",  userFishInfo.getRodLevel() - 1, userFishInfo.getRodLevel())
                .build());
    }
}
