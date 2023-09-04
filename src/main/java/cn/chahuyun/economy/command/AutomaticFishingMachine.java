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
import cn.hutool.cron.CronUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

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
        if (userFishInfo.isStatus()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
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
        AutomaticFishUser automaticFishUser = AutomaticFishUser.getAutomaticFishUser(group.getId(), user.getId());
        if (Objects.nonNull(automaticFishUser)) {
            automaticFishUser.remove();
        }
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plus(Duration.ofHours(9L));
        String cron = getCronString(now, endTime);
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
        // [秒] [分] [时] [日] [月] [周] [年]
        //　　*：代表整个时间段
        //　　？：用在日和周中，表示某一天或者某一周
        //　　/：表示增量，意思是每隔
        //　　L：用于月日和周，表示最后
        //　　W：用于指定最近给定日期的工作日
        //　　#：用于指定本月的第n个工作日
        //　　-：表示一个段
        //　　，：多个值之间通过逗号隔开

        Log.info("自动钓鱼机-定时:" + cron);

        CronUtil.schedule(autoTaskId, cron, automaticFishTask);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append(propsCard.getDescription())
                .build());
    }

    public static String getCronString(LocalDateTime now, LocalDateTime endTime) {
        String sp = " ";

        // 获取小时数
        int hour = now.getHour();
        int minus = now.getMinute();
        int seconds = now.getSecond();

        int hour8 = endTime.getHour();
        // [秒] [分] [时] [日] [月] [周] [年]
        return seconds + sp + minus + sp + hour + "-" + hour8 + "/1" + sp + "*" + sp + "*" + sp + "*";
    }
}

