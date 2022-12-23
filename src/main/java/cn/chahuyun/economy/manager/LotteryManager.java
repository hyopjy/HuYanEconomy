package cn.chahuyun.economy.manager;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.LotteryInfo;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.*;

/**
 * 彩票管理<p>
 * 3种彩票：<p>
 * 一分钟开一次<p>
 * 一小时开一次<p>
 * 一天开一次<p>
 *
 * @author Moyuyanli
 * @date 2022/11/15 10:01
 */
public class LotteryManager {


    private static final Map<String, LotteryInfo> minutesLottery = new HashMap<>();
    private static final Map<String, LotteryInfo> hoursLottery = new HashMap<>();
    private static final Map<String, LotteryInfo> dayLottery = new HashMap<>();

    private LotteryManager() {
    }

    /**
     * 初始化彩票<p>
     *
     * @author Moyuyanli
     * @date 2022/12/6 11:23
     */
    public static void init(boolean type) {
        List<LotteryInfo> lotteryInfos;
        try {
            lotteryInfos = HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<LotteryInfo> query = builder.createQuery(LotteryInfo.class);
                JpaRoot<LotteryInfo> from = query.from(LotteryInfo.class);
                query.select(from);
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            Log.error("彩票管理:彩票初始化失败!", e);
            return;
        }


        for (LotteryInfo lotteryInfo : lotteryInfos) {
            switch (lotteryInfo.getType()) {
                case 1:
                    minutesLottery.put(lotteryInfo.getNumber(), lotteryInfo);
                    continue;
                case 2:
                    hoursLottery.put(lotteryInfo.getNumber(), lotteryInfo);
                    continue;
                case 3:
                    dayLottery.put(lotteryInfo.getNumber(), lotteryInfo);
            }
        }

        if (minutesLottery.size() > 0) {
            //唯一id
            String minutesTaskId = "minutesTask";
            //始终删除一次  用于防止刷新的时候 添加定时任务报错
            CronUtil.remove(minutesTaskId);
            //建立任务类
            LotteryMinutesTask minutesTask = new LotteryMinutesTask(minutesTaskId, minutesLottery.values());
            //添加定时任务到调度器
            CronUtil.schedule(minutesTaskId, "0 * * * * ?", minutesTask);
        }
        if (hoursLottery.size() > 0) {
            String hoursTaskId = "hoursTask";
            CronUtil.remove(hoursTaskId);
            LotteryHoursTask hoursTask = new LotteryHoursTask(hoursTaskId, hoursLottery.values());
            CronUtil.schedule(hoursTaskId, "0 0 * * * ?", hoursTask);
        }
        if (dayLottery.size() > 0) {
            String dayTaskId = "dayTask";
            CronUtil.remove(dayTaskId);
            var dayTask = new LotteryDayTask(dayTaskId, dayLottery.values());
            CronUtil.schedule(dayTaskId, "0 0 0 * * ?", dayTask);
        }
    }

    /**
     * 购买一个彩票<p>
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/6 11:23
     */
    public static void addLottery(MessageEvent event) {
        User user = event.getSender();
        Contact subject = event.getSubject();

        if (subject instanceof Group) {
            Group group = (Group) subject;
            List<Long> longs = EconomyConfig.INSTANCE.getLotteryGroup();
            if (!longs.contains(group.getId())) {
                return;
            }
        }


        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();

        String[] split = code.split(" ");
        StringBuilder number = new StringBuilder(split[1]);

        double money = Double.parseDouble(split[2]);

        double moneyByUser = EconomyUtil.getMoneyByUser(user);
        if (moneyByUser - money <= 0) {
            subject.sendMessage("你都穷的叮当响了，还来猜签？");
            return;
        }

        int type;
        String typeString;
        switch (number.length()) {
            case 3:
                type = 1;
                typeString = "小签";
                break;
            case 4:
                type = 2;
                typeString = "中签";
                break;
            case 5:
                type = 3;
                typeString = "大签";
                break;
            default:
                subject.sendMessage("猜签类型错误!");
                return;
        }

        if (type == 1) {
            if (!(0 < money && money <= 1000)) {
                subject.sendMessage("你投注的金额不属于这个签!");
                return;
            }
        } else if (type == 2) {
            if (!(0 < money && money <= 10000)) {
                subject.sendMessage("你投注的金额不属于这个签!");
                return;
            }
        } else {
            if (!(0 < money && money <= 1000000)) {
                subject.sendMessage("你投注的金额不属于这个签!");
                return;
            }
        }

        String string = number.toString();
        char[] chars = string.toCharArray();
        number = new StringBuilder(String.valueOf(chars[0]));
        for (int i = 1; i < string.length(); i++) {
            String aByte = String.valueOf(chars[i]);
            number.append(",").append(aByte);
        }
        LotteryInfo lotteryInfo = new LotteryInfo(user.getId(), subject.getId(), money, type, number.toString());
        if (!EconomyUtil.minusMoneyToUser(user, money)) {
            subject.sendMessage("猜签失败！");
            return;
        }
        lotteryInfo.save();
        subject.sendMessage(String.format("猜签成功:\n猜签类型:%s\n猜签号码:%s\n猜签金币:%s", typeString, number, money));
        init(false);
    }

    /**
     * 发送彩票结果信息
     * <p>
     *
     * @param location    猜中数量
     * @param lotteryInfo 彩票信息
     * @author Moyuyanli
     * @date 2022/12/6 16:52
     */
    public static void result(int type, int location, LotteryInfo lotteryInfo) {
        if (location == 0) {
            return;
        }
        Bot bot = HuYanEconomy.INSTANCE.bot;
        Group group = bot.getGroup(lotteryInfo.getGroup());
        assert group != null;
        NormalMember member = group.get(lotteryInfo.getQq());
        assert member != null;
        lotteryInfo.remove();
        switch (type) {
            case 1:
                minutesLottery.remove(lotteryInfo.getNumber());
                break;
            case 2:
                hoursLottery.remove(lotteryInfo.getNumber());
                break;
            case 3:
                dayLottery.remove(lotteryInfo.getNumber());
        }
        if (!EconomyUtil.plusMoneyToUser(member, lotteryInfo.getBonus())) {
            member.sendMessage("奖金添加失败，请联系管理员!");
            return;
        }
        member.sendMessage(lotteryInfo.toMessage());
        if (location == 3) {
            group.sendMessage(String.format("得签着:%s(%s),奖励%s金币", member.getNick(), member.getId(), lotteryInfo.getBonus()));
        }
    }

    /**
     * 关闭定时器
     */
    public static void close() {
        CronUtil.stop();
    }

}


/**
 * 彩票定时任务<p>
 * 分钟<p>
 *
 * @author Moyuyanli
 * @date 2022/12/6 14:43
 */
class LotteryMinutesTask implements Task {

    private final String id;
    private List<LotteryInfo> lotteryInfos;

    LotteryMinutesTask(String id, Collection<LotteryInfo> lotteryInfos) {
        this.id = id;
        this.lotteryInfos = List.copyOf(lotteryInfos);
    }

    /**
     * 执行作业
     * <p>
     * 作业的具体实现需考虑异常情况，默认情况下任务异常在监听中统一监听处理，如果不加入监听，异常会被忽略<br>
     * 因此最好自行捕获异常后处理
     */
    @Override
    public void execute() {
        Bot bot = HuYanEconomy.INSTANCE.bot;
        String[] current = {
                String.valueOf(RandomUtil.randomInt(0, 9)),
                String.valueOf(RandomUtil.randomInt(0, 9)),
                String.valueOf(RandomUtil.randomInt(0, 9))
        };
        StringBuilder currentString = new StringBuilder(current[0]);
        for (int i = 1; i < current.length; i++) {
            String s = current[i];
            currentString.append(",").append(s);
        }


        Set<Long> groups = new HashSet<>();

        for (LotteryInfo lotteryInfo : lotteryInfos) {
            groups.add(lotteryInfo.getGroup());
            //位置正确的数量
            int location = 0;
            //计算奖金
            double bonus = 0;

            String[] split = lotteryInfo.getNumber().split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s.equals(current[i])) {
                    location++;
                }
            }
            switch (location) {
                case 3:
                    bonus = lotteryInfo.getMoney() * 160;
                    break;
                case 2:
                    bonus = lotteryInfo.getMoney() * 6;
                    break;
                case 1:
                    bonus = lotteryInfo.getMoney() * 0.7;
                    break;
            }
            lotteryInfo.setBonus(bonus);
            lotteryInfo.setCurrent(currentString.toString());
            lotteryInfo.save();
            LotteryManager.result(1, location, lotteryInfo);
        }
        for (Long group : groups) {
            String format = String.format("本期小签开签啦！\n开签号码%s", currentString);
            Objects.requireNonNull(bot.getGroup(group)).sendMessage(format);
        }
        lotteryInfos = new ArrayList<>();
        //定时任务执行完成，清除自身  我这里需要 其实可以不用
        CronUtil.remove(id);
    }
}

/**
 * 彩票定时任务<p>
 * 小时<p>
 *
 * @author Moyuyanli
 * @date 2022/12/6 14:43
 */
class LotteryHoursTask implements Task {
    private final String id;
    private final List<LotteryInfo> lotteryInfos;


    LotteryHoursTask(String id, Collection<LotteryInfo> lotteryInfos) {
        this.id = id;
        this.lotteryInfos = List.copyOf(lotteryInfos);
    }

    /**
     * 执行作业
     * <p>
     * 作业的具体实现需考虑异常情况，默认情况下任务异常在监听中统一监听处理，如果不加入监听，异常会被忽略<br>
     * 因此最好自行捕获异常后处理
     */
    @Override
    public void execute() {
        Bot bot = HuYanEconomy.INSTANCE.bot;
        String[] current = {
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10))
        };
        StringBuilder currentString = new StringBuilder(current[0]);
        for (int i = 1; i < current.length; i++) {
            String s = current[i];
            currentString.append(",").append(s);
        }

        Set<Long> groups = new HashSet<>();

        for (LotteryInfo lotteryInfo : lotteryInfos) {
            groups.add(lotteryInfo.getGroup());
            //位置正确的数量
            int location = 0;
            //计算奖金
            double bonus = 0;

            String[] split = lotteryInfo.getNumber().split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s.equals(current[i])) {
                    location++;
                }
            }
            switch (location) {
                case 4:
                    bonus = lotteryInfo.getMoney() * 1250;
                    break;
                case 3:
                    bonus = lotteryInfo.getMoney() * 35;
                    break;
                case 2:
                    bonus = lotteryInfo.getMoney() * 2.5;
                    break;
                case 1:
                    bonus = lotteryInfo.getMoney() * 0.5;
                    break;
            }
            lotteryInfo.setBonus(bonus);
            lotteryInfo.setCurrent(currentString.toString());
            lotteryInfo.save();
            LotteryManager.result(2, location, lotteryInfo);
        }
        for (Long group : groups) {
            String format = String.format("本期中签开签啦！\n开签号码%s", currentString);
            Objects.requireNonNull(bot.getGroup(group)).sendMessage(format);
        }
        CronUtil.remove(id);
    }
}

/**
 * 彩票定时任务<p>
 * 天<p>
 *
 * @author Moyuyanli
 * @date 2022/12/6 14:43
 */
class LotteryDayTask implements Task {

    private final String id;
    private final List<LotteryInfo> lotteryInfos;

    LotteryDayTask(String id, Collection<LotteryInfo> lotteryInfos) {
        this.id = id;
        this.lotteryInfos = List.copyOf(lotteryInfos);
    }

    /**
     * 执行作业
     * <p>
     * 作业的具体实现需考虑异常情况，默认情况下任务异常在监听中统一监听处理，如果不加入监听，异常会被忽略<br>
     * 因此最好自行捕获异常后处理
     */
    @Override
    public void execute() {
        Bot bot = HuYanEconomy.INSTANCE.bot;
        String[] current = {
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10)),
                String.valueOf(RandomUtil.randomInt(0, 10))
        };
        StringBuilder currentString = new StringBuilder(current[0]);
        for (int i = 1; i < current.length; i++) {
            String s = current[i];
            currentString.append(",").append(s);
        }

        Set<Long> groups = new HashSet<>();
        List<LotteryInfo> list = new ArrayList<>();

        for (LotteryInfo lotteryInfo : lotteryInfos) {
            groups.add(lotteryInfo.getGroup());
            //位置正确的数量
            int location = 0;
            //计算奖金
            double bonus = 0;

            String[] split = lotteryInfo.getNumber().split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s.equals(current[i])) {
                    location++;
                }
            }
            switch (location) {
                case 5:
                    bonus = lotteryInfo.getMoney() * 10000;
                    break;
                case 4:
                    bonus = lotteryInfo.getMoney() * 200;
                    break;
                case 3:
                    bonus = lotteryInfo.getMoney() * 12;
                    break;
                case 2:
                    bonus = lotteryInfo.getMoney() * 1.4;
                    break;
                case 1:
                    bonus = lotteryInfo.getMoney() * 0.3;
                    break;
            }
            lotteryInfo.setBonus(bonus);
            lotteryInfo.setCurrent(currentString.toString());
            lotteryInfo.save();
            LotteryManager.result(3, location, lotteryInfo);
            if (location == 5) {
                list.add(lotteryInfo);
            }
        }
        for (Long group : groups) {
            Group botGroup = bot.getGroup(group);
            MessageChainBuilder singleMessages = new MessageChainBuilder();
            String format = String.format("本期大签开签啦！\n开签号码%s", currentString);
            singleMessages.append(format).append("\n以下是本期大签开签着:↓");
            if (list.size() == 0) {
                singleMessages.append("无!");
            } else {
                for (LotteryInfo lotteryInfo : list) {
                    assert botGroup != null;
                    NormalMember normalMember = botGroup.get(lotteryInfo.getQq());
                    if (normalMember == null) {
                        singleMessages.append(String.format("%s:%s->奖金:%s", lotteryInfo.getQq(), lotteryInfo.getNumber(), lotteryInfo.getBonus()));
                    } else {
                        singleMessages.append(String.format("%s:%s->奖金:%s", normalMember.getNick(), lotteryInfo.getNumber(), lotteryInfo.getBonus()));
                    }
                }
            }
            Objects.requireNonNull(botGroup).sendMessage(format);
        }
        CronUtil.remove(id);
    }
}