package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.strategy.RodeoFactory;
import cn.chahuyun.economy.strategy.RodeoStrategy;
import cn.hutool.cron.task.Task;

import java.util.Objects;

public class RodeoOpenTask implements Task {

    private String cronKey;

    private Rodeo rodeo;

    public RodeoOpenTask(String cronKey, Rodeo rodeo) {
        this.cronKey = cronKey;
        this.rodeo = rodeo;
    }
    @Override
    public void execute() {
        RodeoManager.CURRENT_SPORTS.put(cronKey, rodeo);
        RodeoStrategy strategy =  RodeoFactory.createRodeoDuelStrategy(rodeo.getPlayingMethod());
        if(Objects.isNull(strategy)){
            return;
        }
        strategy.startGame(rodeo);
    }
}
