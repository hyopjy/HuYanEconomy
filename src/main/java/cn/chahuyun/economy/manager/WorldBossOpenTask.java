package cn.chahuyun.economy.manager;


import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldPropConfig;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class WorldBossOpenTask implements Task {
    @Override
    public void execute() {
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            Log.info("WorldBossOpenTask-end. boss战开关未打开");
            return;
        }
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("WorldBossOpenTask-end. bot为空");
            return;
        }

        WorldBossConfig worldBossFishSizeConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.FISH_SIZE);
        if(Objects.isNull(worldBossFishSizeConfig)){
            Log.info("WorldBossGoalTask-end. FISH_SIZE 设定为空");
            return;
        }
        int fishSize = Integer.parseInt(worldBossFishSizeConfig.getConfigInfo());

        double bb;
        WorldBossConfig worldBossWditBBConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB);
        if(Objects.nonNull(worldBossWditBBConfig)){
            bb = Double.parseDouble(worldBossWditBBConfig.getConfigInfo());
        } else {
            bb = 0.0;
        }

        List<WorldPropConfig> worldPropConfigList = WorldBossConfigManager.getWorldPropConfigList();
        String propMessage = "";
        if (CollectionUtils.isEmpty(worldPropConfigList)) {
            propMessage = "显示奖励开启\uD83C\uDFB5";
        } else if (worldPropConfigList.size() == 1) {
            String propName = getPropName(worldPropConfigList.get(0).getPropCode());
            propMessage = "钓鱼佬，你掉的是" + propName + "？";
        } else {
            String propName1 = getPropName(worldPropConfigList.get(0).getPropCode());
            String propName2 = getPropName(worldPropConfigList.get(1).getPropCode());
            propMessage = "钓鱼佬，你掉的是这个" + propName1 + "，还是这个" + propName2 + "？";
        }
        List<Long> groupIdList = new ArrayList<>();
        groupIdList.add(758085692L);
        groupIdList.add(835186488L);
        String finalPropMessage = propMessage;
        groupIdList.forEach(groupId->{
            bot.getGroup(groupId);
            Message message = new PlainText(finalPropMessage +"上钩" + fishSize + "斤鱼再告诉你！\r\n" +
                    "\r\n" +
                    "\uD83D\uDCE2BOSS战\uD83E\uDD96开启！今日击败世界boss即可获取" + bb + "WDIT币币\uD83E\uDE99\r\n" +
                    "\r\n" +
                    "[什么等级的鱼竿也想钓我？]\r\n" +
                    "　　/\r\n" +
                    "(ˇωˇ ﾐэ)Э三三三三　乚").plus("\r\n");
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(message);
        });

    }

    private String getPropName(String propCode) {
        if(Constant.FISH_CODE_BB.equals(propCode)){
            return Constant.FISH_NAME_BB_LIST.get(0);
        }else {
            PropsBase propsInfo = PropsType.getPropsInfo(propCode);
            if(Objects.nonNull(propsInfo)){
                return propsInfo.getName();
            }
        }
        return "";
    }
}
