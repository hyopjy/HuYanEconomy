package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.strategy.RodeoFactory;
import cn.chahuyun.economy.strategy.RodeoStrategy;
import cn.hutool.cron.task.Task;

import java.util.Objects;

public class RodeoEndTask implements Task {

    private String cronKey;

    private Rodeo rodeo;

    public RodeoEndTask(String cronKey, Rodeo rodeo) {
        this.cronKey = cronKey;
        this.rodeo = rodeo;
    }

    @Override
    public void execute() {
        // 获取比赛结果
        RodeoManager.CURRENT_SPORTS.remove(cronKey, rodeo);
        RodeoStrategy strategy =  RodeoFactory.createRodeoDuelStrategy(rodeo);
        if(Objects.isNull(strategy)){
            return;
        }
        strategy.endGame(rodeo);
    }
}
