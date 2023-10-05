package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

import java.util.*;
import java.util.stream.Collectors;

public class WorldBossProcessTask implements Task {
    @Override
    public void execute() {
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            return;
        }
        // 定时播报
        // 获取目标值
        WorldBossConfig worldBossConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.FISH_SIZE);
        if(Objects.isNull(worldBossConfig)){
            return;
        }
        Integer fishSize = Integer.parseInt(worldBossConfig.getConfigInfo());
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();

        // 获取当前钓鱼所有尺寸
        List<WorldBossUserLog> userLogs = WorldBossConfigManager.getWorldBossUserLog();
        Map<Long, List<WorldBossUserLog>> userLogMap = userLogs.stream().collect(Collectors.groupingBy(WorldBossUserLog::getGroupId));
        for (Map.Entry<Long, List<WorldBossUserLog>> m : userLogMap.entrySet()) {
            Long groupId = m.getKey();
            List<WorldBossUserLog> groupWorldBossUserLog =  m.getValue();
            int userFishSize = NumberUtil.round(groupWorldBossUserLog.stream().mapToInt(WorldBossUserLog::getSize).sum(),2).intValue();
            Message messgae = new PlainText(String.format("Boss当前进度：\r\n%s/%s", userFishSize, fishSize) + "\r\n");
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(messgae);
        }
    }
}
