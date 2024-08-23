package cn.chahuyun.economy.strategy;

import cn.chahuyun.economy.entity.rodeo.Rodeo;

public interface RodeoStrategy {

    /**
     * 开始
     */
    public void startGame(Rodeo rodeo);

    /**
     * 记录
     */
    public void record(Rodeo rodeo);

    /**
     * 结算
     */
    public void endGame(Rodeo rodeo);

}
