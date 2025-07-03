package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.constant.FishSignConstant;
import cn.chahuyun.economy.constant.WorldBossEnum;
import cn.chahuyun.economy.entity.LotteryInfo;

import cn.chahuyun.economy.entity.PropTimeRange;
import cn.chahuyun.economy.entity.TimeRange;
import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.entity.boss.WorldPropConfig;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantShop;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.manager.*;

import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.*;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



public class RandomMoneyListener extends SimpleListenerHost {

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        if (exception instanceof EventCancelledException) {
            Log.error("发送消息被取消:", exception);
        } else if (exception instanceof BotIsBeingMutedException) {
            Log.error("你的机器人被禁言:", exception);
        } else if (exception instanceof MessageTooLargeException) {
            Log.error("发送消息过长:", exception);
        } else if (exception instanceof IllegalArgumentException) {
            Log.error("发送消息为空:", exception);
        }

        // 处理事件处理时抛出的异常
        Log.error(exception);
    }

    @EventHandler()
    public synchronized ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Contact subject = event.getSubject();
        String message = event.getMessage().serializeToMiraiCode();
        if (message.equals("WDIT") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
            int money = RandomUtil.randomInt(500, 7001);
            EconomyUtil.plusMoneyToUser(sender, money);
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "恭喜你获得" + money + "WDIT 币币"));
        }

        if (message.startsWith("颁发") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
            GroupAdminManager.giveCup(event);
        }

        if (message.equals("余额")) {
            String bbStr = EconomyUtil.getMoneyByUserStr(sender);
            String seasonStr = EconomyUtil.getMoneyByBankStr(sender);

            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                    SeasonCommonInfoManager.getBBMoneyDesc() + ": %s \r\n"
                    + SeasonCommonInfoManager.getSeasonMoneyDesc()+": %s \r\n", bbStr, seasonStr));
        }
        if (message.equals("查看签签")) {
            List<LotteryInfo> list = HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<LotteryInfo> query = builder.createQuery(LotteryInfo.class);
                JpaRoot<LotteryInfo> from = query.from(LotteryInfo.class);
                query.select(from);
                query.where(builder.equal(from.get("qq"), sender.getId()), builder.equal(from.get("group"), sender.getGroup().getId()));
                return session.createQuery(query).list();
            });
            if (CollectionUtils.isEmpty(list)) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "暂时没有签"));
            } else {
                Message m = new PlainText("");
                // 强制透
                List<LotteryInfo> grandLotto = list.stream().filter(lotteryInfo -> lotteryInfo.getType() == 3).collect(Collectors.toList());
                // 缺德球
                List<LotteryInfo> union = list.stream().filter(lotteryInfo -> lotteryInfo.getType() == 2).collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(grandLotto)) {
                    m = m.plus("强制一签：").plus("\r\n");
                    for (int i = 0; i < grandLotto.size(); i++) {
                        m = m.plus("号码：").plus(grandLotto.get(i).getNumber()).plus(" 币币：" + grandLotto.get(i).getMoney()).plus("\r\n");
                    }
                }

                if (!CollectionUtils.isEmpty(union)) {
                    m = m.plus("缺德球签：").plus("\r\n");
                    for (int i = 0; i < union.size(); i++) {
                        m = m.plus("号码：").plus(union.get(i).getNumber()).plus(" 币币：" + union.get(i).getMoney()).plus("\r\n");
                    }
                }
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), m.contentToString()));
            }

        }

        return ListeningStatus.LISTENING;
    }

    @EventHandler()
    public synchronized ListeningStatus onUserMessage(UserMessageEvent event) {
        User sender = event.getSender();
        Contact subject = event.getSubject();
        String message = event.getMessage().serializeToMiraiCode();
        try {
            if (message.equals("重置鱼塘") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                // SeasonManager.clearFishRank();
                SeasonManager.reloadFishPod(event);
                subject.sendMessage(MessageUtil.formatMessageChain("重置鱼塘成功"));
            }

             if (message.equals("刷新表格")) {
                 // 重置鱼塘
                 subject.sendMessage(MessageUtil.formatMessageChain("开始加载"));
                SeasonManager.reloadFishPod(event);
                subject.sendMessage(MessageUtil.formatMessageChain("重置鱼塘成功"));

                // 刷新道具
                SeasonManager.reloadPropsFishCard();
                subject.sendMessage(MessageUtil.formatMessageChain("刷新道具完成"));

                // 神秘商品
                SeasonManager.importShopInfo();
                subject.sendMessage(MessageUtil.formatMessageChain("神秘商品更新完成"));
            }

            if (message.startsWith("休渔期") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                // 休渔期 1,2,3,4 10-11
                // 1,2,3,4,5 0-5,10-23
                // 6,7 9-15,20-23
                message = message.replace("\\", "");
                String[] arr = message.split(" ");
                String weekDay = arr[1];
                String time = arr[2];
                String[] weekDayArr = weekDay.split(",");
                for (String week : weekDayArr) {
                    int weekNum = 0;
                    try {
                        weekNum = Integer.parseInt(week);
                    } catch (Exception e) {
                        subject.sendMessage(MessageUtil.formatMessageChain("week输入数字"));
                        return ListeningStatus.LISTENING;
                    }
                    TimeRange timeRange = new TimeRange(weekNum, time);
                    timeRange.save();
                }

                Message m = new PlainText("=====配置信息=====").plus("\r\n");
                StringBuffer stringBuffer = new StringBuffer();
                List<TimeRange> list = TimeRangeManager.getTimeRangeList();
                list.forEach(timeRange -> {
                    TimeRangeManager.WEEK_TIME_RANGE_CACHE.put(timeRange.getWeekDay(), timeRange);
                    stringBuffer.append(timeRange.getDesc());
                });
                m = m.plus(stringBuffer);
                subject.sendMessage(MessageUtil.formatMessageChain(m.contentToString()));
            }

            if (message.startsWith("营业时间") && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                // 休渔期 1,2,3,4 10-11
                // 1,2,3,4,5 0-5,10-23
                // 6,7 9-15,20-23
                message = message.replace("\\", "");
                String[] arr = message.split(" ");
                String weekDay = arr[1];
                String time = arr[2];
                String[] weekDayArr = weekDay.split(",");
                for (String week : weekDayArr) {
                    int weekNum = 0;
                    try {
                        weekNum = Integer.parseInt(week);
                    } catch (Exception e) {
                        subject.sendMessage(MessageUtil.formatMessageChain("week输入数字"));
                        return ListeningStatus.LISTENING;
                    }
                    PropTimeRange propTimeRange = new PropTimeRange(weekNum, time);
                    propTimeRange.save();
                }

                Message m = new PlainText("=====配置信息=====").plus("\r\n");
                StringBuffer stringBuffer = new StringBuffer();
                List<PropTimeRange> list = PropTimeRangeManager.getPropTimeRangeList();
                list.forEach(propTimeRange -> {
                    PropTimeRangeManager.PROP_TIME_RANGE_CACHE.put(propTimeRange.getWeekDay(), propTimeRange);
                    stringBuffer.append(propTimeRange.getDesc());
                });
                m = m.plus(stringBuffer);
                subject.sendMessage(MessageUtil.formatMessageChain(m.contentToString()));
            }

            if (message.equals("刷新道具") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                SeasonManager.reloadPropsFishCard();
                subject.sendMessage(MessageUtil.formatMessageChain("刷新道具完成"));
            }

            if (message.equals("导入神秘商品") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                SeasonManager.importShopInfo();
                subject.sendMessage(MessageUtil.formatMessageChain("导入神秘商品"));
            }

            if (message.startsWith("世界boss") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                //  开启
                String code = event.getMessage().serializeToMiraiCode().replaceAll("\\\\,",",");
                String[] codeArr = code.split(" ", 3);
                if ("开启".equals(codeArr[1])) {
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
                    worldBossStatusConfig.setConfigInfo("true");
                    worldBossStatusConfig.save();
                }
                if ("关闭".equals(codeArr[1])) {
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.BOSS_STATUS);
                    worldBossStatusConfig.setConfigInfo("false");
                    worldBossStatusConfig.save();
                }

                if ("目标尺寸".equals(codeArr[1])) {
                    int size = Integer.parseInt(codeArr[2]);
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.FISH_SIZE);
                    worldBossStatusConfig.setConfigInfo(size + "");
                    worldBossStatusConfig.save();
                }
                if ("奖励金额".equals(codeArr[1])) {
                    double bb = Double.parseDouble(codeArr[2]);
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB);
                    worldBossStatusConfig.setConfigInfo(bb + "");
                    worldBossStatusConfig.save();
                }
                if ("币币数量奖励金额".equals(codeArr[1])) {
                    double bb = Double.parseDouble(codeArr[2]);
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB_COUNT);
                    worldBossStatusConfig.setConfigInfo(bb + "");
                    worldBossStatusConfig.save();
                }
                if ("币币概率奖励金额".equals(codeArr[1])) {
                    double bb = Double.parseDouble(codeArr[2]);
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.WDIT_BB_PROP);
                    worldBossStatusConfig.setConfigInfo(bb + "");
                    worldBossStatusConfig.save();
                }
                // 设置达成播报时间
                if ("达成播报".equals(codeArr[1])) {
                    String cornGoalCron = codeArr[2];
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.CORN_GOAL);
                    // 刷新cron
                    WorldBossConfigManager.refreshCronStr(worldBossStatusConfig, cornGoalCron);
                }
                // 设置进度播报时间
                if ("进度播报".equals(codeArr[1])) {
                    String cornGoalCron = codeArr[2];
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.CORN_PROGRESS);
                    // 刷新cron
                    WorldBossConfigManager.refreshCronStr(worldBossStatusConfig, cornGoalCron);
                }

                // 设置开始播报时间
                if ("开始播报".equals(codeArr[1])) {
                    String cornGoalCron = codeArr[2];
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.CORN_OPEN);
                    // 刷新cron
                    WorldBossConfigManager.refreshCronStr(worldBossStatusConfig, cornGoalCron);
                }

                if ("开始时间".equals(codeArr[1])) {
                    String openTime = codeArr[2];
                    String nt = LocalDate.now().getYear() + openTime + "00";
                    try{
                        LocalDateTime otime = LocalDateTime.parse(nt, Constant.FORMATTER_YYMMDDHHMMSS);
                        String time = otime.format(Constant.FORMATTER);
                        WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.OPEN_TIME);
                        worldBossStatusConfig.setConfigInfo(time);
                        worldBossStatusConfig.save();
                    }catch (Exception e){
                        subject.sendMessage("时间格式有误");
                        return ListeningStatus.LISTENING;
                    }
                }

                if ("结束时间".equals(codeArr[1])) {
                    String endtime = codeArr[2];
                    String nt = LocalDate.now().getYear() + endtime + "59";
                    try{
                        LocalDateTime otime = LocalDateTime.parse(nt, Constant.FORMATTER_YYMMDDHHMMSS);
                        String time = otime.format(Constant.FORMATTER);
                        WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.END_TIME);
                        worldBossStatusConfig.setConfigInfo(time);
                        worldBossStatusConfig.save();
                    }catch (Exception e){
                        subject.sendMessage("时间格式有误");
                        return ListeningStatus.LISTENING;
                    }
                }


                if ("额外鱼尺寸".equals(codeArr[1])) {
                    String min = Double.parseDouble(codeArr[2]) + "";
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.OTHER_FISH_SIZE);
                    worldBossStatusConfig.setConfigInfo(min);
                    worldBossStatusConfig.save();
                }

                if ("最后一杆奖励币币".equals(codeArr[1])) {
                    String bb = Double.parseDouble(codeArr[2]) + "";
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.LAST_SHOT_BB);
                    worldBossStatusConfig.setConfigInfo(bb);
                    worldBossStatusConfig.save();
                }

                if ("最后一杆奖励道具".equals(codeArr[1])) {
                    String prop = Objects.isNull(codeArr[2]) ? "" : codeArr[2];
                    List<String> propList = Arrays.asList(prop.split(Constant.SPILT)).stream().distinct().collect(Collectors.toList());
                    List<String> propCodeList = new ArrayList<>(propList.size());
                    List<String> notExitPropList = new ArrayList<>();
                    propList.forEach(no->{
                        String propCode = PropsType.getCode(no);
                        if(Objects.isNull(propCode)){
                            notExitPropList.add(no);
                        }else {
                            propCodeList.add(propCode);
                        }
                    });
                    if(CollectionUtils.isNotEmpty(notExitPropList)){
                        String commaSeparatedString = String.join(Constant.SPILT, notExitPropList);
                        subject.sendMessage("道具不存在：" + commaSeparatedString);
                        return ListeningStatus.LISTENING;
                    }
                    String propCodeValue =  String.join(Constant.SPILT, propCodeList);
                    WorldBossConfig worldBossStatusConfig = WorldBossConfigManager.getWorldBossConfigByKey(WorldBossEnum.LAST_SHOT_PROP);
                    worldBossStatusConfig.setConfigInfo(propCodeValue);
                    worldBossStatusConfig.save();
                }

                StringBuilder sb = new StringBuilder("世界模式配置如下:\r\n");
                List<WorldBossConfig> list = WorldBossConfigManager.getWorldBossConfigList();
                List<WorldBossEnum> worldBossEnumList = WorldBossEnum.getWorldBossEnumList();
                worldBossEnumList.forEach(worldBossEnum -> {
                    Optional<WorldBossConfig> bossStatusConfig = list.stream().filter(keyConfig -> worldBossEnum.getKeyId() == keyConfig.getKeyId()).findFirst();
                    bossStatusConfig.ifPresent(worldBossConfig -> sb.append(worldBossEnum.getKeyDesc()).append(":").append(worldBossConfig.getConfigInfo()).append("\r\n"));
                });
                subject.sendMessage(sb.toString());
                return ListeningStatus.LISTENING;
            }
            if (message.startsWith("boss奖励") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                //  开启
                String code = event.getMessage().serializeToMiraiCode();
                String[] codeArr = code.split(" ");

                // 设置奖励
                // boss奖励 code/name 概率 50
                // boss奖励 code/name 数量 5
                String no = codeArr[1];
                String type = codeArr[2];
                Integer num = Integer.parseInt(codeArr[3]);
                String propCode = "";
                if (Constant.FISH_NAME_BB_LIST.contains(no)) {
                    propCode = Constant.FISH_CODE_BB;
                } else {
                    propCode = PropsType.getCode(no);
                    if (propCode == null) {
                        Log.warning("道具系统:购买道具为空");
                        subject.sendMessage("道具不存在");
                        return ListeningStatus.LISTENING;
                    }
                }
                // PropsBase propsInfo = PropsType.getPropsInfo(propCode);
                String typeCode = "";
                if ("概率".equals(type)) {
                    typeCode = Constant.BOSS_PROP_PROBABILITY_TYPE;
                } else if ("数量".equals(type)) {
                    typeCode = Constant.BOSS_PROP_COUNT_TYPE;
                } else {
                    subject.sendMessage("类型错误");
                    return ListeningStatus.LISTENING;
                }

                WorldPropConfig worldPropConfig = WorldBossConfigManager.getWorldPropConfigByTypeAndCode(typeCode, propCode);
                if (Objects.isNull(worldPropConfig)) {
                    worldPropConfig = new WorldPropConfig(IdUtil.getSnowflakeNextId(), propCode, typeCode, num);
                } else {
                    worldPropConfig.setConfigInfo(num);
                }
                worldPropConfig.save();

                StringBuilder sb = getWorldPropInfoString();
                subject.sendMessage(sb.toString());
            }

            if (message.startsWith("删除奖励") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                //  开启
                String code = event.getMessage().serializeToMiraiCode();
                String[] codeArr = code.split(" ");

                // 设置奖励
                // 删除奖励 概率 code/name
                // 删除奖励 数量 code/name
                String type = codeArr[1];
                String no = codeArr[2];
                String propCode;
                if (Constant.FISH_NAME_BB_LIST.contains(no)) {
                    propCode = Constant.FISH_CODE_BB;
                } else {
                    propCode = PropsType.getCode(no);
                    if (propCode == null) {
                        Log.warning("道具系统:购买道具为空");
                        subject.sendMessage("道具不存在");
                        return ListeningStatus.LISTENING;
                    }
                }
                // PropsBase propsInfo = PropsType.getPropsInfo(propCode);
                String typeCode = "";
                if ("概率".equals(type)) {
                    typeCode = Constant.BOSS_PROP_PROBABILITY_TYPE;
                } else if ("数量".equals(type)) {
                    typeCode = Constant.BOSS_PROP_COUNT_TYPE;
                } else {
                    subject.sendMessage("类型错误");
                    return ListeningStatus.LISTENING;
                }

                WorldPropConfig worldPropConfig = WorldBossConfigManager.getWorldPropConfigByTypeAndCode(typeCode, propCode);
                if (Objects.nonNull(worldPropConfig)) {
                    worldPropConfig.remove();
                }

                StringBuilder sb = getWorldPropInfoString();
                subject.sendMessage(sb.toString());
            }

            if (message.startsWith("重置用户属性") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())){
                UserManager.resetUserRgb();
                subject.sendMessage("重置成功");
            }

            if (message.startsWith("rgb") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())){
                UserManager.userRgbList(event);
            }
            // 设置用户属性 setRgb groupid qq R/G/B
            if (message.startsWith("setRgb") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())){
                String[] arr = message.split(" ");
                Long groupId = Long.parseLong(arr[1]);
                Long userId = Long.parseLong(arr[2]);
                String rgb = arr[3];
                UserManager.setUserRgb(event, groupId,  userId, rgb);
            }
            if (message.startsWith("重置赛季") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())){
                // 1. 重置鱼塘
                SeasonManager.reloadFishPod(event);
                // 2. 更新道具
                SeasonManager.reloadPropsFishCard();
                // 3. 清除旧的赛季币
                SeasonManager.clearSeasonMoney();
                // 4. 清理用户包内道具信息
                SeasonManager.clearUserPackOffline();
                // 6. 点亮鱼竿纪念成就, 鱼竿重置等级
//                SeasonManager.lightUpFishRod();
                // 7. 清理钓鱼排行榜
                 SeasonManager.clearFishRank();
                // 8. 用户widitbb余额 超过88888的 更新为88888
//                SeasonManager.resetWditBB();
                // 9. 神秘商品更新
                SeasonManager.importShopInfo();

                // 10. 删除道具
                // 2025-06/07 版本
                SeasonManager.clearPropCode("FISH-25");
                SeasonManager.clearPropCode("FISH-26");

            }
            if (message.startsWith("xb") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())){
                try {
                    String[] arr = message.split(" ");
                    double money = Double.parseDouble(arr[1]);
                    EconomyUtil.plusMoneyToBank(sender, money);

                }catch (Exception e){
                    Log.info("给我雪币 exception:"+ e.getMessage());
                }
            }
            if (message.startsWith("bb") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())){
                try {
                    String[] arr = message.split(" ");
                    double money = Double.parseDouble(arr[1]);
                    EconomyUtil.plusMoneyToUser(sender, money);

                }catch (Exception e){
                    Log.info("给我币币 exception:"+ e.getMessage());
                }
            }

            // 设置鱼竿等级 groupId qq 44
            if (message.startsWith("fishlevel") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                String[] arr = message.split(" ");
                Long groupId = Long.parseLong(arr[1]);
                Long qq = Long.parseLong(arr[2]);
                Integer rodLevel = Integer.parseInt(arr[3]);
                 FishInfoManager.updateUserRodLevel(event ,groupId, qq, rodLevel);
            }

            // 增加成就 groupId qq 11
            if (message.startsWith("增加成就") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                String[] arr = message.split(" ");
                Long groupId = Long.parseLong(arr[1]);
                Long qq = Long.parseLong(arr[2]);
                String propNumber = arr[3];

                String propCode = PropsType.getCode(propNumber);
                if(!Objects.isNull(propCode)){
                    String signCode = propCode.toUpperCase(Locale.ROOT);
                    // 成就
                    if (FishSignConstant.getSignPropCode().contains(signCode)) {
                        BadgeInfoManager.updateOrInsertBadgeInfo(groupId, qq, signCode, null, null);
                    }
                }

            }

            // 增加道具 add pack (ap groupId qq prop 4 |ap groupId qq prop) 删除道具 remove pack  rp groupid qq prop 4
            if ((message.startsWith("ap") || message.startsWith("rp")) &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                try {
                    String[] arr = message.split(" ");
                    if (!(arr.length == 4 || arr.length == 5)) {
                        subject.sendMessage("请输入： ap groupId qq prop 4 或者 ap groupId qq prop");
                        return ListeningStatus.LISTENING;
                    }
                    String type = arr[0];
                    Long groupId = null;
                    Long qq = null;
                    String prop = "";
                    Integer num = null;
                    if (arr.length == 5) {
                        groupId = Long.parseLong(arr[1]);
                        qq = Long.parseLong(arr[2]);
                        prop = arr[3];
                        num = Integer.parseInt(arr[4]);
                    }
                    if (arr.length == 4) {
                        groupId = Long.parseLong(arr[1]);
                        qq = Long.parseLong(arr[2]);
                        prop = arr[3];
                        num = 1;
                    }
                    // 给某人背包新增道具
                    BackpackManager.updateUserPropForGroup(event, groupId, qq, prop, num, type);
                } catch (Exception e) {
                    Log.info("增加/删除 道具 add pack exception:" + e.getMessage());
                }
            }

            // 扣减 bb groupid qq number
            if ((message.startsWith("扣减") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId()))) {
                try {
                    String[] arr = message.split(" ");
                    if (!(arr.length == 5)) {
                        subject.sendMessage("请输入： 扣减 bb qq number");
                        return ListeningStatus.LISTENING;
                    }
                    Long groupId = Long.parseLong(arr[2]);
                    Long qq = Long.parseLong(arr[3]);
                    Double bbCount = Double.parseDouble(arr[4]);
                    // 给某人背包新增道具
                    TransferManager.subUseMoney(event, groupId, qq, bbCount);
                } catch (Exception e) {
                    Log.info("扣减 bb exception:" + e.getMessage());
                }
            }

            if (message.startsWith("神秘商人") &&
                    EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                //  神秘商人 开启
                String code = event.getMessage().serializeToMiraiCode().replaceAll("\\\\","").toString();
                String[] codeArr = code.split(" ");
                MysteriousMerchantSetting config = null;
                if ("开启".equals(codeArr[1])) {
                    config = MysteriousMerchantManager.open();
                    if(Objects.isNull(config)) {
                        subject.sendMessage("请先设置");
                    }
                }
                // 神秘商人 关闭
                if ("关闭".equals(codeArr[1])) {
                    config = MysteriousMerchantManager.close();
                    if(Objects.isNull(config)) {
                        subject.sendMessage("请先设置");
                    }
                }

                if ("设置规则".equals(codeArr[1])) {
//                  神秘商人 设置规则 14,17,21    10(几分钟消失)  15(概率) 1-52/0(商品编码范围) 2(几种道具)  1-3(随机道具库存)
                    // 小时数量
                    List<String> hourList = Arrays.asList(StringUtils.split(codeArr[2] , Constant.MM_SPILT));
                    // 过多久消失
                    Integer passMinute = Integer.parseInt(codeArr[3]);
                    // 概率
                    Integer probability = Integer.parseInt(codeArr[4]);

                    // 商品
                    // 如果包含0 则是所有商品列表
                    // 如果xx-xx
                    List<String> goodCodeList = getGoodCodeListByCodeString(codeArr[5]);

                    // 随机几种
                    Integer randomGoodCount = Integer.parseInt(codeArr[6]);
                    // 随机库存数量
                    String[] storedPropCode = codeArr[7].split(Constant.SPILT);
                    // 最小库存数
                    Integer minStored = Integer.parseInt(storedPropCode[0]);
                    // 最大库存数
                    Integer maxStored = Integer.parseInt(storedPropCode[1]);

                    // 限制购买次数
                    Integer buyCount = 1;
                    // 配置信息
                    config = MysteriousMerchantManager.setting(hourList, passMinute,
                            probability, goodCodeList, randomGoodCount, minStored, maxStored, buyCount);
                    // 启动config任务
                    MysteriousMerchantManager.settingRunTask(config);
                }

                //  神秘商人 开启
                //  神秘商人 关闭
                //  神秘商人 限购次数 2
                //  神秘商人 设置规则 14,17,21 15% 10(几分钟消失)  83-92(商品编码范围) 2(几种道具)  1-3(随机道具库存)
                if(Objects.isNull(config)){
                    subject.sendMessage("神秘商人信息为空");
                    return ListeningStatus.LISTENING;
                }
                String sb = "神秘商人设置信息\r\n"
                        + "是否开启：" + config.getStatus() + "\r\n"
                        + "限制购买次数：" + config.getBuyCount() + "\r\n"
                        + "开始小时数：" + config.getHourStr() + "\r\n"
                        + "过多久消失：" + config.getPassMinute() + "\r\n"
                        + "概率：" + config.getProbability()+ "\r\n"
                        + "上架商品列表：" + config.getGoodCodeStr() + "\r\n"
                        + "随机几种商品：" + config.getRandomGoodCount() + "\r\n"
                        + "随机几种商品：" + config.getRandomGoodCount() + "\r\n"
                        + "最小随机库存数：" + config.getMinStored()+ "\r\n"
                        + "最大随机库存数：" + config.getMaxStored()+ "\r\n"
                        ;
                subject.sendMessage(sb);
                return ListeningStatus.LISTENING;
            }

        }catch (Exception e){
            e.printStackTrace();
            subject.sendMessage(MessageUtil.formatMessageChain("程序发生异常@@"+ e.getMessage()));
        }
        return ListeningStatus.LISTENING;
    }

    private List<String> getGoodCodeListByCodeString(String codeStr) {
        if("0".equals(codeStr)){
            return new ArrayList<>(Optional.of(MysteriousMerchantManager.SHOP_GOODS_MAP).orElse(new HashMap<>()).keySet());
        }
        String[] codeArr = codeStr.split(Constant.SPILT);
        if(codeArr.length !=2){
            return Lists.newArrayList();
        }

        int startCode = Integer.parseInt(codeArr[0]);
        int endCode = Integer.parseInt(codeArr[0]);

        if(startCode > endCode){
            int temp = startCode;
            startCode = endCode;
            endCode = temp;
        }
        List<String> goodCodeList = new ArrayList<>();
        for(int i = startCode; i <= endCode ; i++ ){
            String shopCodeStr = Constant.MM_PROP_START + i;
            MysteriousMerchantShop shopGood = MysteriousMerchantManager.getShopGoodCode(shopCodeStr);
            if(Objects.nonNull(shopGood)){
                goodCodeList.add(shopGood.getGoodCode());
            }
        }
        return goodCodeList;
    }

    @NotNull
    private static StringBuilder getWorldPropInfoString() {
        List<WorldPropConfig> propConfigList = WorldBossConfigManager.getWorldPropConfigList();
        StringBuilder sb = new StringBuilder("boss奖励如下:\r\n");
        List<WorldPropConfig> propList = propConfigList.stream().filter(prop ->
                Constant.BOSS_PROP_PROBABILITY_TYPE.equals(prop.getType())).collect(Collectors.toList());

        propList.forEach(prop -> {
            getSbString(prop, sb,"概率(%)");
        });
        List<WorldPropConfig> countList = propConfigList.stream().filter(prop ->
                Constant.BOSS_PROP_COUNT_TYPE.equals(prop.getType())).collect(Collectors.toList());
        countList.forEach(count -> {
            getSbString(count, sb,"数量");
        });
        return sb;
    }


    private static void getSbString(WorldPropConfig prop, StringBuilder sb,String desc) {
        if (Constant.FISH_CODE_BB.equals(prop.getPropCode())) {
            sb.append("道具编码：").append(prop.getPropCode().replace("FISH-", "")).append(" 名称：").append(" 币币").append("｜").append(desc).append("：").append(prop.getConfigInfo()).append("\r\n");
        } else {
            PropsBase propsInfo = PropsType.getPropsInfo(prop.getPropCode());
            if (Objects.isNull(propsInfo)) {
                return;
            }
            sb.append("道具编码：").append(prop.getPropCode().replace("FISH-", "")).append(" 名称：").append(propsInfo.getName()).append("｜").append(desc).append("：").append(prop.getConfigInfo()).append("\r\n");
        }
    }
}
