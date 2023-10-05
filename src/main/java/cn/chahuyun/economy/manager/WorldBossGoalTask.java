package cn.chahuyun.economy.manager;


import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.chahuyun.economy.entity.boss.WorldPropConfig;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

import java.util.*;
import java.util.stream.Collectors;

public class WorldBossGoalTask implements Task {
    @Override
    public void execute() {
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            return;
        }
        // 达成成就播报
        // 定时播报
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        // 获取目标值
        WorldBossConfig worldBossFishSizeConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.FISH_SIZE);
        if(Objects.isNull(worldBossFishSizeConfig)){
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

        // 获取当前钓鱼所有尺寸
        List<WorldBossUserLog> userLogs = WorldBossConfigManager.getWorldBossUserLog();
        Map<Long, List<WorldBossUserLog>> userLogMap = userLogs.stream().collect(Collectors.groupingBy(WorldBossUserLog::getGroupId));
        for (Map.Entry<Long, List<WorldBossUserLog>> m : userLogMap.entrySet()) {
            Message messgae = new PlainText("Boss战结束，战况如下：\r\n");

            Long groupId = m.getKey();
            Group group = bot.getGroup(groupId);
            List<WorldBossUserLog> groupWorldBossUserLog = m.getValue();
            double userFishSize = NumberUtil.round(groupWorldBossUserLog.stream().mapToDouble(WorldBossUserLog::getSize).sum(), 2).doubleValue();
            if (userFishSize < fishSize) {
                continue;
            }

            Set<Long> userIdList = groupWorldBossUserLog.stream().map(WorldBossUserLog::getUserId).collect(Collectors.toSet());
            // 转账wditBB
            StringBuilder sb = new StringBuilder();
            if (bb > 0) {
                sb.append("-------").append("\r\n");
                sb.append("获取wdit bb Boss奖金" + bb + "如下：").append("\r\n");
                userIdList.forEach(userId -> {
                    NormalMember member = group.get(userId);
                    assert member != null;
                    if (!EconomyUtil.plusMoneyToUser(member, bb)) {
                        member.sendMessage("奖金添加失败，请联系管理员!");
                    }else {
                        sb.append(new At(userId).getDisplay(group)).append(" ").append(bb).append("\r\n");
                    }
                });
                sb.append("-------").append("\r\n");
            }

            sb.append("-------").append("\r\n");
            sb.append("获取道具如下：").append("\r\n");
            // 概率开奖列表
            List<WorldPropConfig> propProbabilityList = worldPropConfigList.stream().filter(worldPropConfig ->
                    Constant.BOSS_PROP_PROBABILITY_TYPE.equals(worldPropConfig.getType())).collect(Collectors.toList());
            propProbabilityList.forEach(probabilityBo -> {
                // 计算百分比
                int num;
                if(userIdList.size() <= 10){
                    double value = NumberUtil.round((double) probabilityBo.getConfigInfo() / 10,2).doubleValue();
                    num = (int) (userIdList.size() * value);
                }else {
                    double value = NumberUtil.round((double) probabilityBo.getConfigInfo() / 100,2).doubleValue();
                    num = (int) (userIdList.size() * value);
                }
                getWorldPropInfo(group, userIdList, sb, probabilityBo.getPropCode(), num);
            });
            // 数量开奖列表
            List<WorldPropConfig> propCountList = worldPropConfigList.stream().filter(worldPropConfig ->
                    Constant.BOSS_PROP_COUNT_TYPE.equals(worldPropConfig.getType())).collect(Collectors.toList());
            propCountList.stream().forEach(probabilityBo -> {
                // 获取数量
                int num = probabilityBo.getConfigInfo();
                if(userIdList.size() <= num){
                    num = userIdList.size();
                }
                getWorldPropInfo(group, userIdList, sb, probabilityBo.getPropCode(), num);
            });
            sb.append("-------").append("\r\n");
           // WorldBossConfigManager.deleteAllWorldBossUserLog(groupId);
            // 删除日志
            groupWorldBossUserLog.forEach(WorldBossUserLog::remove);
            messgae = messgae.plus(sb.toString());
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(messgae);
        }
    }

    private void getWorldPropInfo(Group group, Set<Long> userIdList, StringBuilder sb, String propCode, int num) {
        num = Math.min(userIdList.size(), num);
        List<Long> propProbabilityUserId = RandomUtil.randomEles(new ArrayList<>(userIdList), num);
        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        if(Objects.isNull(propsInfo)){
            return;
        }
        propProbabilityUserId.forEach(userId -> {
            NormalMember member = group.get(userId);
            UserInfo userInfo = UserManager.getUserInfo(member);
            UserBackpack userBackpack = new UserBackpack(userInfo, propsInfo);
            if (!userInfo.addPropToBackpack(userBackpack)) {
                Log.warning("道具系统:添加道具到用户背包失败!");
                member.sendMessage("系统出错，请联系主人!");
            }else {
                sb.append(new At(userId).getDisplay(group)).append(" ").append(propsInfo.getName()).append("\r\n");
            }
        });
    }


}
