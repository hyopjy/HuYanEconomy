package cn.chahuyun.economy.strategy.impl;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.strategy.RodeoStrategy;
import cn.chahuyun.economy.utils.Log;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

import java.util.Objects;

public abstract class RodeoAbstractStrategy implements RodeoStrategy {

    public Group getBotGroup(Long groupId){
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("RodeoAbstractStrategy bot 为空");
            return null ;
        }
        return bot.getGroup(groupId);
    }
}
