package cn.chahuyun.economy.command;

import cn.chahuyun.economy.dto.AutomaticFish;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.AutomaticFishUser;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.UserManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 岛岛全自动钓鱼机
 */
public class AutomaticFishingMachine extends AbstractPropUsage {

    /**
     * 创建配置文件
     * 启动定时任务
     * init
     *
     * @return
     */
    @Override
    public boolean checkOrder() {
        this.isBuff = true;
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if (!Pattern.matches(match, code)) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                    "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
            return false;
        }

        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();
        //能否钓鱼
        if (!userFishInfo.isFishRod()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "没有鱼竿，bobo也帮不了你🥹"));
            return false;
        }

        if(userFishInfo.getStatus()){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在钓鱼无法使用道具"));
            return false;
        }
        FishPond fishPond = userFishInfo.getFishPond();
        if (fishPond == null) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "默认鱼塘不存在!"));
            return false;
        }
        //获取鱼塘限制鱼竿最低等级
        int minLevel = fishPond.getMinLevel();
        if (userFishInfo.getRodLevel() < minLevel) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "鱼竿等级太低，bobo拒绝你在这里钓鱼\uD83D\uDE45" +
                    "\u200D♀️"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        User user = event.getSender();
        // add auto machine cache
        List<AutomaticFishUser> automaticFishUserList = AutomaticFishUser.getAutomaticFishUser(group.getId(), user.getId());
        if (!CollectionUtils.isEmpty(automaticFishUserList)) {
            automaticFishUserList.stream().forEach(AutomaticFishUser::remove);
        }
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 按小时
        LocalDateTime endTime = now.plus(Duration.ofHours(8L)).withMinute(0).withSecond(0);
        String cron = getCronStringHour(now);
        // 按分钟demo
//        LocalDateTime endTime = now.plus(Duration.ofMinutes(8L)).withSecond(0);
//        String cron = getCronStringMinutes(now);
        List<AutomaticFish> automaticFishStr = new ArrayList<>();

        // 存入数据库
        AutomaticFishUser saveAuto = new AutomaticFishUser(group.getId(), user.getId(), now, endTime, cron,
                automaticFishStr);
        saveAuto.saveOrUpdate();

        CacheUtils.addAutomaticFishBuff(group.getId(), user.getId(), AutomaticFishTask.getAutomaticFishTaskId(group.getId(), user.getId()));

        //唯一id
        String autoTaskId = AutomaticFishTask.getAutomaticFishTaskId(group.getId(), user.getId());
        //始终删除一次  用于防止刷新的时候 添加定时任务报错
        CronUtil.remove(autoTaskId);
        //建立任务类
        AutomaticFishTask automaticFishTask = new AutomaticFishTask(autoTaskId, endTime, group.getId(), user.getId());
        //添加定时任务到调度器
        // 3 10-23/2,0,2 * * *

        Log.info("自动钓鱼机-定时:" + cron);

        CronUtil.schedule(autoTaskId, cron, automaticFishTask);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append(propsCard.getDescription())
                .build());
    }

    private String getCronStringMinutes(LocalDateTime now) {
        String sp = " ";
        int seconds = now.getSecond();

        List<Integer> minutesList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            minutesList.add(now.plus(Duration.ofMinutes(i+1)).getMinute());
        }
        String minuteStr = CollUtil.join(minutesList, ",");
        // [秒] [分] [时] [日] [月] [周] [年]
        return seconds + sp + minuteStr + sp + "*" + sp + "*" + sp + "*" + sp + "?";
    }

    public static String getCronStringHour(LocalDateTime now) {
        String sp = " ";
        int minus = now.getMinute();
        int seconds = now.getSecond();

        List<Integer> hourList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            hourList.add(now.plus(Duration.ofHours(i+1)).getHour());
        }
        String hourStr = CollUtil.join(hourList, ",");
        // [秒] [分] [时] [日] [月] [周] [年]
        return seconds + sp + minus + sp + hourStr + sp + "*" + sp + "*" + sp + "?";
    }
}

