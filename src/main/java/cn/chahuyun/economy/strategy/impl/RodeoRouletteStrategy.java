package cn.chahuyun.economy.strategy.impl;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.strategy.impl.RodeoAbstractStrategy;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

/**
 * 轮盘
 */
public class RodeoRouletteStrategy extends RodeoAbstractStrategy {
    @Override
    public void startGame(Rodeo rodeo) {
        Group group = getBotGroup(rodeo.getGroupId());
        if(group == null){
            return;
        }

        String messageFormat1= """
            东风吹，战鼓擂，轮盘赛上怕过谁！
            新的🏟[%s]正式开战！比赛时长[%s]，参赛选手有：
        """;

        String messageFormat2= """
            
            轮盘比赛正式打响！🔫[%s]的比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
        """;

        String[] players = rodeo.getPlayers().split(Constant.MM_SPILT);

        long playerTime = DateUtil.between(DateUtil.parse(rodeo.getStartTime(),
                DatePattern.NORM_TIME_PATTERN), DateUtil.parse(rodeo.getEndTime(), DatePattern.NORM_TIME_PATTERN), DateUnit.MINUTE);

        String message1 = String.format(messageFormat1, rodeo.getVenue(), playerTime+"分钟");
        String message2 = String.format(messageFormat2, playerTime+"分钟");

        Message m = new PlainText(message1);
        for(String str : players){
            Long playerId = Long.parseLong(str);
            m = m.plus(new At(playerId).getDisplay(group));
        }
        m = m.plus(message2);

        group.sendMessage(m);

        // todo 开始轮盘权限
    }

    @Override
    public void record(Rodeo rodeo) {

    }

    @Override
    public void endGame(Rodeo rodeo) {

    }
}
