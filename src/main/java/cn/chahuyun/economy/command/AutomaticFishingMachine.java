package cn.chahuyun.economy.command;

import cn.chahuyun.config.AutomaticFishConfig;
import cn.chahuyun.config.AutomaticFishUser;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.LotteryInfo;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.LotteryManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 岛岛全自动钓鱼机
 */
public class AutomaticFishingMachine {
        // extends AbstractPropUsage
    /**
     * 创建配置文件
     * 启动定时任务
     * init
     * @return
     */
//    @Override
//    public boolean checkOrder() {
//        String no = PropsType.getNo(propsCard.getCode());
//        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
//        String code = event.getMessage().serializeToMiraiCode();
//        Contact subject = event.getSubject();
//        if(!Pattern.matches(match, code)){
//            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
//            return false;
//        }
//        return true;
//    }

//    @Override
//    public void excute() {
//        User user = event.getSender();
//        ConcurrentHashMap<Long, List<AutomaticFishUser>> map = AutomaticFishConfig.INSTANCE.getAutomaticFishUserList();
//        // add auto machine cache
//        CacheUtils.addAutomaticFishBuff(group.getId(), user.getId(), "machine");
//        //  open time
//        //  cron
//        //  endtime
//        //  List<fish>
//        //  sendmessage
//    }
    static String getCronString(){
        String sp = " ";
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plus(Duration.ofHours(9L));
        // 获取小时数
        int hour = now.getHour();
        int minus = now.getMinute();
        int seconds = now.getSecond();

        int hour8 = endTime.getHour();
        // [秒] [分] [时] [日] [月] [周] [年]
        return seconds + sp + minus + sp + hour + "-" + hour8 + "/1" + sp + "*" + sp + "*" + sp + "*";
    }

    public static void main(String[] args) throws InterruptedException {
        //唯一id
        String minutesTaskId = "934415751:934415751";
        //始终删除一次  用于防止刷新的时候 添加定时任务报错
        CronUtil.remove(minutesTaskId);
        //建立任务类
        AutomaticFishTask minutesTask = new AutomaticFishTask(minutesTaskId);
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
        CronUtil.schedule(minutesTaskId, "0-59/5 * * * * *", minutesTask);

        System.out.println(getCronString());
    }
}

class AutomaticFishTask implements Task {
    String id;

    public AutomaticFishTask(String id) {
        this.id = id;
    }

    @Override
    public void execute() {
        System.out.println("excute");
        CronUtil.remove(id);
    }
}

