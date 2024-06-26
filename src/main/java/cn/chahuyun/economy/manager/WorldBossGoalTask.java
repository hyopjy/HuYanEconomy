package cn.chahuyun.economy.manager;


import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.dto.BossTeamUserSize;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.chahuyun.economy.entity.boss.WorldPropConfig;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.plugin.PluginManager;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

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

        double otherFish = 0.0;
        // 获取额外鱼尺寸
        WorldBossConfig otherFishSize =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.OTHER_FISH_SIZE);
        if(Objects.nonNull(otherFishSize)){
            otherFish = Double.parseDouble(otherFishSize.getConfigInfo());
        }
        Log.info("WorldBossGoalTask-otherFish：" + otherFish);

        double lastShotBB = 0.0;
        // 最后一杆奖励bb
        WorldBossConfig lastShotBbConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.LAST_SHOT_BB);
        if(Objects.nonNull(lastShotBbConfig)){
            lastShotBB = Double.parseDouble(lastShotBbConfig.getConfigInfo());
        }
        // 最后一杆奖励道具
        List<String>  lastShotPropCodeList = new ArrayList<>();
        WorldBossConfig lastShotPropCodeConfig =  WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.LAST_SHOT_PROP);
        if(Objects.nonNull(lastShotPropCodeConfig) && StringUtils.isNotBlank(lastShotPropCodeConfig.getConfigInfo())){
            lastShotPropCodeList = Arrays.asList(lastShotPropCodeConfig.getConfigInfo().split(Constant.SPILT));
        }

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
            // 查询最后一杆用户
            Long lastShotUserId = null;
            Optional<WorldBossUserLog> optionalWorldBossUserLog =
                    groupWorldBossUserLog.stream()
                            .max(Comparator.comparing(WorldBossUserLog::getDateTime));
            if (optionalWorldBossUserLog.isPresent()) {
                WorldBossUserLog worldBossUserLog = optionalWorldBossUserLog.get();
                lastShotUserId = worldBossUserLog.getUserId();
            }
            Log.info("WorldBossGoalTask-群id：" + groupId + "WorldBossGoalTask-群记录数：" + groupWorldBossUserLog.size() + "最后一名玩家：" + lastShotUserId);
            double userFishSize = NumberUtil.round(groupWorldBossUserLog.stream().mapToDouble(WorldBossUserLog::getSize).sum(), 2).doubleValue();
            if ((userFishSize + otherFish) < fishSize) {
                Log.info("WorldBossGoalTask-end：目标尺寸:" + fishSize + "当前尺寸：" + (userFishSize + otherFish));
                continue;
            }
            Set<Long> userIdList = groupWorldBossUserLog.stream().map(WorldBossUserLog::getUserId).collect(Collectors.toSet());
            Log.info("WorldBossGoalTask-参与人数：" + userIdList.size());
            // 转账wditBB
            StringBuilder sb = new StringBuilder();
            if (bb > 0) {
                sb.append("-------").append("\r\n");
                sb.append("获取WDIT BB Boss奖金").append("如下：").append("\r\n");
                List<BossTeamUserSize> bossUserSortList = WorldBossConfigManager.getTeamUserBbList(groupWorldBossUserLog,  groupId);
                // 拼提示信息
                Map<Long, Double> bbUserMap = WorldBossConfigManager.getUserBbMapByTeamUser(bb, bossUserSortList);
                bossUserSortList.stream().forEach(teamUserSize->{
                    if(teamUserSize.getType().equals(1)){
                        double owner = Optional.ofNullable(bbUserMap.get(teamUserSize.getTeamOwner())).orElse(0.0);
                        double member = Optional.ofNullable(bbUserMap.get(teamUserSize.getTeamMember())).orElse(0.0);
                        sb.append("[").append(teamUserSize.getTeamName()).append("]").append("-").append("[").append(teamUserSize.getFishSize()).append("/").append(owner).append("➕").append(member).append("]").append("\r\n");
                    }
                    if(teamUserSize.getType().equals(0)){
                        double mm = bbUserMap.get(teamUserSize.getId());
                        sb.append(new At(teamUserSize.getId()).getDisplay(group)).append(" ").append("[" + teamUserSize.getFishSize() + "/" + mm + "]").append("\r\n");
                    }
                });
                // 发放奖金
                for (Map.Entry<Long, Double> entry : bbUserMap.entrySet()) {
                    Long userId = entry.getKey();
                    Double moneyBB = entry.getValue();
                    NormalMember member = group.get(userId);
                    if (Objects.isNull(member)) {
                        Log.error("WorldBossGoalTask-发放奖金用户为空：" + userId);
                        return;
                    }
                    if (!EconomyUtil.plusMoneyToUser(member, moneyBB)) {
                        member.sendMessage("奖金添加失败，请联系管理员!");
                        Log.error("WorldBossGoalTask-发放奖金失败：" + userId + "奖金：" + moneyBB);
                    }
                }
                sb.append("-------").append("\r\n");
            }

            // 最后一个钓鱼用户
            if(Objects.nonNull(lastShotUserId)){
                sb.append("-------").append("\r\n");
                sb.append("恭喜最后一杆 \uD83D\uDC51\uD83D\uDC51")
                        .append(new At(lastShotUserId).getDisplay(group))
                        .append("获得：").append("\r\n");
                NormalMember member = group.get(lastShotUserId);
                if (Objects.isNull(member)) {
                    Log.error("WorldBossGoalTask-发放奖金用户为空：" + lastShotUserId);
                    return;
                }
                // bb
                if(lastShotBB > 0){
                    if (!EconomyUtil.plusMoneyToUser(member, lastShotBB)) {
                        member.sendMessage("奖金添加失败，请联系管理员!");
                        Log.error("WorldBossGoalTask-发放奖金失败：" + lastShotUserId + "奖金：" + lastShotBB);
                        return;
                    }
                    sb.append("bb:" + lastShotBB).append("\r\n");
                } else if(CollectionUtils.isNotEmpty(lastShotPropCodeList)){
                    // 道具
                    List<String> propNameList = new ArrayList<>();
                    lastShotPropCodeList.forEach(propCode->{
                        if(StringUtils.isBlank(propCode)){
                            return;
                        }
                        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
                        if(Objects.isNull(propsInfo)){
                            return;
                        }
                        UserInfo userInfo = UserManager.getUserInfo(member);
                        PluginManager.getPropsManager().addProp(userInfo, propsInfo);
                    });
                    sb.append("道具:" + String.join(Constant.SPILT, propNameList)).append("\r\n");
                } else {
                    sb.append("什么也没有呢\uD83D\uDE09\uD83D\uDE09").append("\r\n");
                }
                sb.append("-------").append("\r\n");
            }

            sb.append("钓鱼佬，你掉的道具如下：").append("\r\n");
            sb.append("-------").append("\r\n");
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
                Log.info("WorldBossGoalTask-概率-道具code：" + probabilityBo.getPropCode() + "道具概率：" + probabilityBo.getConfigInfo()
                + "WorldBossGoalTask-概率-参与人数：" + userIdList.size() + "道具获得人数：" + getUserList.size());
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
                Log.info("WorldBossGoalTask-数量-道具code：" + probabilityBo.getPropCode() + "道具个数：" + probabilityBo.getConfigInfo()
                + "WorldBossGoalTask-数量-参与人数：" + userIdList.size() + "数量获得人数：" + getUserList.size());
                getWorldPropInfo(group, getUserList, sb, probabilityBo.getPropCode(), finalCountBB);
            });

            sb.append("-------").append("\r\n");

            Message messgae = new PlainText("\uD83E\uDD96Boss战结束，钓鱼佬狂砍" + (userFishSize + otherFish) + "斤，战况如下：：\r\n");
            messgae = messgae.plus(sb.toString());
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(messgae);
        }
        // 删除日志
        userLogs.forEach(WorldBossUserLog::remove);
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
            sb.append("["+ bb + "币币]").append("\r\n");
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
                    sb.append(new At(userId).getDisplay(group)).append("\r\n");
                }
            });
            sb.append("-------").append("\r\n");
        }else {
            PropsBase propsInfo = PropsType.getPropsInfo(propCode);
            if(Objects.isNull(propsInfo)){
                return;
            }
            sb.append("["+ propsInfo.getName() + "]").append("\r\n");
            userIdList.forEach(userId -> {
                NormalMember member = group.get(userId);
                UserInfo userInfo = UserManager.getUserInfo(member);
                UserBackpack userBackpack = new UserBackpack(userInfo, propsInfo);
                if (!userInfo.addPropToBackpack(userBackpack)) {
                    Log.warning("道具系统:添加道具到用户背包失败!");
                    member.sendMessage("系统出错，请联系主人!");
                }else {
                    sb.append(new At(userId).getDisplay(group)).append("\r\n");
                }
            });
            sb.append("-------").append("\r\n");
        }

        Log.info("WorldBossGoalTask-end");

    }


}
