package cn.chahuyun.economy.command;

import cn.chahuyun.config.AutomaticFish;
import cn.chahuyun.config.AutomaticFishConfig;
import cn.chahuyun.config.AutomaticFishUser;
import cn.chahuyun.config.DriverCarEventConfig;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.GamesManager;
import cn.chahuyun.economy.manager.UserManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * 岛岛全自动钓鱼机
 */
public class AutomaticFishingMachine extends AbstractPropUsage{
    /**
     * 创建配置文件
     * 启动定时任务
     * init
     * @return
     */
    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
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
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "鱼竿等级太低，bobo拒绝你在这里钓鱼\uD83D\uDE45\u200D♀️"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        String machineKey = "machine";
        User user = event.getSender();
        // add auto machine cache
        ConcurrentHashMap<Long, List<AutomaticFishUser>> map = AutomaticFishConfig.INSTANCE.getAutomaticFishUserList();
        List<AutomaticFishUser> automaticFishUserList = map.get(group.getId());
        if(CollectionUtil.isEmpty(automaticFishUserList)){
            automaticFishUserList = new CopyOnWriteArrayList<>();
        }
        Optional<AutomaticFishUser> automaticFishUser =
                automaticFishUserList.stream().filter(fUser -> user.getId() == fUser.getFishUser()).findFirst();
        if(automaticFishUser.isPresent()){
            automaticFishUserList.remove(automaticFishUser.get());
        }
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plus(Duration.ofHours(9L));
        String cron = getCronString(now,endTime);

        AutomaticFishUser saveUser = new AutomaticFishUser(user.getId(),
                now.format(DateTimeFormatter.BASIC_ISO_DATE),
                endTime.format(DateTimeFormatter.BASIC_ISO_DATE),
                cron,
                new CopyOnWriteArrayList<>());
        automaticFishUserList.add(saveUser);
        map.put(group.getId(),automaticFishUserList);

        CacheUtils.addAutomaticFishBuff(group.getId(), user.getId(), machineKey);
        //  open time
        //  cron
        //  endtime
        //  List<fish>
        //  sendmessage
        //唯一id
        String minutesTaskId = machineKey + user.getId() + group.getId();
        //始终删除一次  用于防止刷新的时候 添加定时任务报错
        CronUtil.remove(minutesTaskId);
        //建立任务类
        AutomaticFishTask minutesTask = new AutomaticFishTask(minutesTaskId, endTime, group, user);
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

        CronUtil.schedule(minutesTaskId, cron, minutesTask);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append(propsCard.getDescription())
                .build());
    }
    static String getCronString(LocalDateTime now, LocalDateTime endTime){
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

class AutomaticFishTask implements Task {
    String id;

    LocalDateTime endTime;

     Group group;

     User user;

    public AutomaticFishTask(String id,LocalDateTime endTime, Group group, User user) {
        this.id = id;
        this.endTime = endTime;
        this.group = group;
        this.user = user;
    }

    @Override
    public void execute() {
        System.out.println("excute");
        ConcurrentHashMap<Long, List<AutomaticFishUser>> map = AutomaticFishConfig.INSTANCE.getAutomaticFishUserList();
        List<AutomaticFishUser> usersList = map.get(group.getId());
        Optional<AutomaticFishUser> automaticFishUserOptional = usersList.stream().filter(u->this.user.getId() == u.getFishUser()).findFirst();
        if(!automaticFishUserOptional.isPresent()){
            return;
        }
        AutomaticFishUser automaticFishUser = automaticFishUserOptional.get();
        usersList.remove(automaticFishUser);

        List<AutomaticFish> fish = automaticFishUser.getAutomaticFishList();
        // 添加字符串
        AutomaticFish automaticFish = GamesManager.getAutomaticFish();
        fish.add(automaticFish);

        //usersList.add(new AutomaticFishUser(automaticFishUser.getFishUser(),automaticFish))

        Optional.ofNullable(DriverCarEventConfig.INSTANCE.getDriverCar().get(group.getId())).orElse(new CopyOnWriteArrayList<>()).clear();
        AutomaticFishConfig.INSTANCE.getAutomaticFishUserList().put(group.getId(),usersList);

        if(LocalDateTime.now().equals(endTime) || LocalDateTime.now().isAfter(endTime)){
            // 输出鱼信息

            // 删除缓存
            CacheUtils.removeAutomaticFishBuff(group.getId(), user.getId());

        }
        CronUtil.remove(id);
    }
}

