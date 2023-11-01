package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

import java.util.*;
import java.util.stream.Collectors;

public class WorldBossProcessTask implements Task {
    @Override
    public void execute() {
        Log.info("WorldBossProcessTask-open");
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            Log.info("WorldBossProcessTask-end. boss战开关未打开");
            return;
        }
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("WorldBossProcessTask-end. bot 为空");
            return;
        }
        // 定时播报
        // 获取目标值
        WorldBossConfig worldBossConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.FISH_SIZE);
        if(Objects.isNull(worldBossConfig)){
            Log.info("WorldBossProcessTask-end. FISH_SIZE 设定为空");
            return;
        }
        double otherFish = 0.0;
        // 获取额外鱼尺寸
        WorldBossConfig otherFishSize =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.OTHER_FISH_SIZE);
        if(Objects.nonNull(otherFishSize)){
            otherFish = Double.parseDouble(otherFishSize.getConfigInfo());
        }
        Integer fishSize = Integer.parseInt(worldBossConfig.getConfigInfo());
        Log.info("WorldBossProcessTask-fishSize设定值：" + fishSize);
        // 获取当前钓鱼所有尺寸
        List<WorldBossUserLog> userLogs = WorldBossConfigManager.getWorldBossUserLog();
        Log.info("WorldBossProcessTask-获取目前钓鱼记录数列表：" + userLogs.size());
        Map<Long, List<WorldBossUserLog>> userLogMap = userLogs.stream().collect(Collectors.groupingBy(WorldBossUserLog::getGroupId));
        for (Map.Entry<Long, List<WorldBossUserLog>> m : userLogMap.entrySet()) {
            Long groupId = m.getKey();
            List<WorldBossUserLog> groupWorldBossUserLog = m.getValue();
            Log.info("WorldBossProcessTask-群id：" + groupId + "WorldBossProcessTask-群记录数：" + groupWorldBossUserLog.size());
            double userFishSize = groupWorldBossUserLog.stream().mapToDouble(WorldBossUserLog::getSize).sum() + otherFish;
            Message message = new PlainText(String.format("\uD83E\uDD96世界Boss当前进度：\r\n\uD83D\uDC33 %s/%s", userFishSize, fishSize) + "\r\n");
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(message);
        }
        Log.info("WorldBossProcessTask-end");
    }
}
