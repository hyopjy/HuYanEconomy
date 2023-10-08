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
import cn.chahuyun.economy.utils.RandomHelperUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;

public class WorldBossGoalTask implements Task {
    @Override
    public void execute() {
        Log.info("WorldBossGoalTask-open");
        WorldBossConfig worldBossStatusConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
        if(!Boolean.parseBoolean(worldBossStatusConfig.getConfigInfo())){
            Log.info("WorldBossGoalTask-end. boss战开关未打开");
            return;
        }
        // 达成成就播报
        // 定时播报
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("WorldBossGoalTask-end. bot为空");
            return;
        }
        // 获取目标值
        WorldBossConfig worldBossFishSizeConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.FISH_SIZE);
        if(Objects.isNull(worldBossFishSizeConfig)){
            Log.info("WorldBossGoalTask-end. FISH_SIZE 设定为空");
            return;
        }
        int fishSize = Integer.parseInt(worldBossFishSizeConfig.getConfigInfo());
        Log.info("WorldBossGoalTask-fishSize设定值：" + fishSize);
        double bb;
        WorldBossConfig worldBossWditBBConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB);
        if(Objects.nonNull(worldBossWditBBConfig)){
            bb = Double.parseDouble(worldBossWditBBConfig.getConfigInfo());
        } else {
            bb = 0.0;
        }
        Log.info("WorldBossGoalTask-bb数量：" + bb);

        List<WorldPropConfig> worldPropConfigList = WorldBossConfigManager.getWorldPropConfigList();
        Log.info("WorldBossGoalTask-获取道具列表长度：" + worldPropConfigList.size());

        // 获取当前钓鱼所有尺寸
        List<WorldBossUserLog> userLogs = WorldBossConfigManager.getWorldBossUserLog();
        Log.info("WorldBossGoalTask-获取钓鱼记录：" + userLogs.size());
        Map<Long, List<WorldBossUserLog>> userLogMap = userLogs.stream().collect(Collectors.groupingBy(WorldBossUserLog::getGroupId));
        for (Map.Entry<Long, List<WorldBossUserLog>> m : userLogMap.entrySet()) {
            Long groupId = m.getKey();
            Group group = bot.getGroup(groupId);
            List<WorldBossUserLog> groupWorldBossUserLog = m.getValue();
            Log.info("----------------");
            Log.info("WorldBossGoalTask-群id：" + groupId);
            Log.info("WorldBossGoalTask-群记录数：" + groupWorldBossUserLog.size());

            double userFishSize = NumberUtil.round(groupWorldBossUserLog.stream().mapToDouble(WorldBossUserLog::getSize).sum(), 2).doubleValue();
            if (userFishSize < fishSize) {
                Log.info("WorldBossGoalTask-end：目标尺寸:" + fishSize + "当前尺寸：" + userFishSize);
                Log.info("----------------");
                continue;
            }

            Set<Long> userIdList = groupWorldBossUserLog.stream().map(WorldBossUserLog::getUserId).collect(Collectors.toSet());
            Log.info("WorldBossGoalTask-参与人数：" + userIdList.size());
            // 转账wditBB
            StringBuilder sb = new StringBuilder();
            if (bb > 0) {
                sb.append("-------").append("\r\n");
                sb.append("获取wdit bb Boss奖金" + bb + "如下：").append("\r\n");
                userIdList.forEach(userId -> {
                    NormalMember member = group.get(userId);
                    if(Objects.isNull(member)){
                        Log.error("WorldBossGoalTask-发放奖金用户为空：" + userId);
                        return;
                    }
                    if (!EconomyUtil.plusMoneyToUser(member, bb)) {
                        member.sendMessage("奖金添加失败，请联系管理员!");
                        Log.error("WorldBossGoalTask-发放奖金失败：" + userId);
                    }else {
                        sb.append(new At(userId).getDisplay(group)).append(" ").append("\r\n");
                    }
                });
                sb.append("-------").append("\r\n");
            }

            sb.append("-------").append("\r\n");
            sb.append("获取道具如下：").append("\r\n");
            // 概率开奖列表
            List<WorldPropConfig> propProbabilityList = worldPropConfigList.stream().filter(worldPropConfig ->
                    Constant.BOSS_PROP_PROBABILITY_TYPE.equals(worldPropConfig.getType())).collect(Collectors.toList());
            Log.info("WorldBossGoalTask：概率道具开奖，概率道具数量：" + propProbabilityList.size());
            // 查询概率奖金
            WorldBossConfig worldWditBbProp =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB_PROP);
            double propBB = 0;
            if(Objects.nonNull(worldWditBbProp)){
                propBB = Double.parseDouble(worldWditBbProp.getConfigInfo());
            }
            double finalPropBB = propBB;
            propProbabilityList.forEach(probabilityBo -> {
                // 获取中奖人列表
                Set<Long> getUserList = getAwardUserId(userIdList, probabilityBo, Constant.BOSS_PROP_PROBABILITY_TYPE);
                Log.info("----------------");
                Log.info("WorldBossGoalTask-概率-道具code：" + probabilityBo.getPropCode() + "道具概率：" + probabilityBo.getConfigInfo());
                Log.info("WorldBossGoalTask-概率-参与人数：" + userIdList.size() + "道具获得人数：" + getUserList.size());
                Log.info("----------------");
                getWorldPropInfo(group, getUserList, sb, probabilityBo.getPropCode(), finalPropBB);
            });

            // 数量开奖列表
            // 查询数量奖金
            WorldBossConfig worldWditBbCount =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB_COUNT);
            double countBB = 0;
            if(Objects.nonNull(worldWditBbCount)){
                countBB = Double.parseDouble(worldWditBbCount.getConfigInfo());
            }
            double finalCountBB = countBB;
            List<WorldPropConfig> propCountList = worldPropConfigList.stream().filter(worldPropConfig ->
                    Constant.BOSS_PROP_COUNT_TYPE.equals(worldPropConfig.getType())).collect(Collectors.toList());
            Log.info("WorldBossGoalTask：数量道具开奖，数量道具个数：" + propCountList.size());
            propCountList.forEach(probabilityBo -> {
                // 获取数量
                Set<Long> getUserList = getAwardUserId(userIdList, probabilityBo, Constant.BOSS_PROP_COUNT_TYPE);
                Log.info("----------------");
                Log.info("WorldBossGoalTask-数量-道具code：" + probabilityBo.getPropCode() + "道具个数：" + probabilityBo.getConfigInfo());
                Log.info("WorldBossGoalTask-数量-参与人数：" + userIdList.size() + "数量获得人数：" + getUserList.size());
                Log.info("----------------");
                getWorldPropInfo(group, getUserList, sb, probabilityBo.getPropCode(), finalCountBB);
            });

            sb.append("-------").append("\r\n");
           // WorldBossConfigManager.deleteAllWorldBossUserLog(groupId);
            // 删除日志
            groupWorldBossUserLog.forEach(WorldBossUserLog::remove);
            Log.info("----------------");

            Message messgae = new PlainText("Boss战结束，战况如下：\r\n");
            messgae = messgae.plus(sb.toString());
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(messgae);
        }
    }

    private Set<Long> getAwardUserId(Set<Long> userIdSet, WorldPropConfig probabilityBo, String type) {
        Set<Long> awardUserIds = new HashSet<>();
        List<Long> userIdList = new ArrayList<>(userIdSet);
        if(Constant.BOSS_PROP_PROBABILITY_TYPE.equals(type)){
            // 获取概率
            Integer prop = probabilityBo.getConfigInfo();
            userIdList.forEach(userId->{
                if(RandomHelperUtil.checkRandomByProp(prop)){
                    awardUserIds.add(userId);
                }
            });
        }

        if(Constant.BOSS_PROP_COUNT_TYPE.equals(type)){
            // 数量
            int num = probabilityBo.getConfigInfo();
            if(userIdList.size() < num){
                num = userIdList.size();
            }
            for (int i = 0; i < num; i++) {
                addUserIds(awardUserIds, userIdList);
            }
        }
        return awardUserIds;
    }

    public void addUserIds(Set<Long> awardUserIds,List<Long> userIdList) {
        int rand = RandomUtils.nextInt(0, userIdList.size());
        Long userId = userIdList.get(rand);
        if(!awardUserIds.contains(userId)){
            awardUserIds.add(userId);
        }else {
            addUserIds(awardUserIds,userIdList);
        }
    }

    private void getWorldPropInfo(Group group, Set<Long> userIdList, StringBuilder sb, String propCode, double bb) {
        Log.info("WorldBossGoalTask-getWorldPropInfo：中奖人：" + userIdList.size());

        if(Constant.FISH_CODE_BB.equals(propCode)){
            userIdList.forEach(userId -> {
                NormalMember member = group.get(userId);
                if(Objects.isNull(member)){
                    Log.error("WorldBossGoalTask-概率/数量发放币币-用户为空：" + userId);
                    return;
                }
                if (!EconomyUtil.plusMoneyToUser(member, bb)) {
                    member.sendMessage("奖金添加失败，请联系管理员!");
                    Log.error("WorldBossGoalTask-概率/数量发放币币-发放奖金失败：" + userId);
                }else {
                    sb.append(new At(userId).getDisplay(group)).append(" ").append(bb).append("币币").append("\r\n");
                }
            });
        }else {
            PropsBase propsInfo = PropsType.getPropsInfo(propCode);
            if(Objects.isNull(propsInfo)){
                return;
            }
            userIdList.forEach(userId -> {
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

        Log.info("WorldBossGoalTask-end");

    }


}
