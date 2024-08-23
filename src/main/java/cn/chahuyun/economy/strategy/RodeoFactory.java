package cn.chahuyun.economy.strategy;

import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.strategy.impl.RodeoDuelStrategy;
import cn.chahuyun.economy.strategy.impl.RodeoRouletteStrategy;
import cn.chahuyun.economy.strategy.impl.RodeoSuperSmashBrothersStrategy;

public class RodeoFactory {
    public static final String DUEL = "决斗";
    public static final String ROULETTE = "轮盘";
    public static final String SUPER_SMASH_BROTHERS = "大乱斗";

    public static RodeoStrategy createRodeoDuelStrategy(Rodeo rodeo){
        String playingMethod = rodeo.getPlayingMethod();
        if(DUEL.equals(playingMethod)){
            return new RodeoDuelStrategy();
        }
        if(ROULETTE.equals(playingMethod)) {
            return new RodeoRouletteStrategy();
        }
        if(SUPER_SMASH_BROTHERS.equals(playingMethod)) {
            return new RodeoSuperSmashBrothersStrategy();
        }
        return null;
    }
}
