package cn.chahuyun.economy.strategy;

import cn.chahuyun.economy.dto.RodeoRecordGameInfoDto;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import net.mamoe.mirai.event.events.UserMessageEvent;

public interface RodeoStrategy {

    /**
     * 开始
     */
    public void startGame(Rodeo rodeo);

    /**
     * 记录
     */
    public void record(Rodeo rodeo, RodeoRecordGameInfoDto dto);

    /**
     * 结算
     */
    public void endGame(Rodeo rodeo);

    Rodeo checkOrderAndGetRodeo(UserMessageEvent event, String[] messageArr);

    /**
     * 解析消息
     *
     * @param message
     * @return
     */
    RodeoRecordGameInfoDto analyzeMessage(String message);
}
