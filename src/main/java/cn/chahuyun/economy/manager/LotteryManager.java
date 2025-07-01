package cn.chahuyun.economy.manager;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.dto.LotteryLocationInfo;
import cn.chahuyun.economy.entity.LotteryInfo;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.*;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    /**
     * 强制透
     */
    private static final Map<String, LotteryInfo> GRAND_LOTTO_LOTTERY = new ConcurrentHashMap<>();
    /**
     * 缺德球
     */
    private static final Map<String, LotteryInfo> UNION_LOTTO =  new ConcurrentHashMap<>();
    private static final Map<String, LotteryInfo> THREE_QIU = new HashMap<>();

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
            String key = lotteryInfo.getId() + "-" + lotteryInfo.getQq() + "-" + lotteryInfo.getGroup() + "-" + lotteryInfo.getNumber();
            switch (lotteryInfo.getType()) {
//                case 1:
//                    GRAND_LOTTO_LOTTERY.put(key, lotteryInfo);
//                    continue;
                case 2:
                    UNION_LOTTO.put(key, lotteryInfo);
                    continue;
                case 3:
                    THREE_QIU.put(key, lotteryInfo);
                    continue;
            }
        }

//        if (GRAND_LOTTO_LOTTERY.size() > 0) {
//            //唯一id
//            String minutesTaskId = "GrandLottoTask";
//            //始终删除一次  用于防止刷新的时候 添加定时任务报错
//            CronUtil.remove(minutesTaskId);
//            //建立任务类
//            LotteryMinutesTask minutesTask = new LotteryMinutesTask(minutesTaskId, GRAND_LOTTO_LOTTERY.values());
//            //添加定时任务到调度器
//            // CronUtil.schedule(minutesTaskId, "0 * * * * ?", minutesTask);
//
//            CronUtil.schedule(minutesTaskId, "0 0 12,18,22 * * ?", minutesTask);
//        }
        if (UNION_LOTTO.size() > 0) {
            String hoursTaskId = "UnionLotto";
            CronUtil.remove(hoursTaskId);
            LotteryHoursTask hoursTask = new LotteryHoursTask(hoursTaskId, UNION_LOTTO.values());
            //CronUtil.schedule(hoursTaskId, "0 0 * * * ?", hoursTask);
            // 缺德球改成2467的20
            CronUtil.schedule(hoursTaskId, "0 0 20 ? * 6,4,7,2", hoursTask);

        }
        if (THREE_QIU.size() > 0) {
            String dayTaskId = "ThreeQiu";
            CronUtil.remove(dayTaskId);
            var dayTask = new LotteryDayTask(dayTaskId, THREE_QIU.values());
            CronUtil.schedule(dayTaskId, "0 0 22 * * ?", dayTask);
            // CronUtil.schedule(dayTaskId, "0 * * * * ?", dayTask);
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
            subject.sendMessage(MessageUtil.formatMessageChain(message, "你都穷得叮当响了，还来猜签？"));
            return;
        }

        int type;
        String typeString;
        switch (number.length()) {
//            case 3:
//                type = 1;
//                typeString = "强制透";
//                break;
            case 4:
                type = 2;
                typeString = "缺德球";
                break;
            case 2:
                type = 3;
                typeString = "强制一";
                break;
            default:
                subject.sendMessage(MessageUtil.formatMessageChain(message,"猜签类型错误!"));
                return;
        }

        if (type == 1) {
            if (!(0 < money && money <= 5000)) {
                subject.sendMessage(MessageUtil.formatMessageChain(message, "强制透投注金额必须≤5000币币!"));
                return;
            }
        } else if (type == 2) {
            if (!(0 < money && money <= 50000)) {
                subject.sendMessage(MessageUtil.formatMessageChain(message, "缺德球投注金额必须≤50000币币!"));
                return;
            }
        } else if (type == 3) {
            if (money < 0 || money % 100 != 0) {
                subject.sendMessage(MessageUtil.formatMessageChain(message, "强制透投注金额必须是100*n币币!"));
                return;
            }
        }else {
            return;
        }

        String string = number.toString();
        if (string.length() != 2) {
            char[] chars = string.toCharArray();
            number = new StringBuilder(String.valueOf(chars[0]));
            for (int i = 1; i < string.length(); i++) {
                String aByte = String.valueOf(chars[i]);
                number.append(",").append(aByte);
            }
        }
        LotteryInfo lotteryInfo = new LotteryInfo(user.getId(), subject.getId(), money, type, number.toString());
        if (!EconomyUtil.minusMoneyToUser(user, money)) {
            subject.sendMessage(MessageUtil.formatMessageChain(message,"猜签失败！"));
            return;
        }
        lotteryInfo.save();
        // 通过单次猜签100000及以上币币获得 梭哈标识碎片 FISH-113
        if(money >= 100000){
            PropsBase propsInfo = PropsType.getPropsInfo("FISH-113");
            UserInfo newUserInfo = UserManager.getUserInfo(user);
            UserBackpack newBackpackItem = new UserBackpack(newUserInfo, propsInfo);
            newUserInfo.addPropToBackpack(newBackpackItem);
            subject.sendMessage(MessageUtil.formatMessageChain(message,
                    "猜签成功:\r\n猜签类型:%s\r\n猜签号码:%s\r\n猜签WDIT币币:%s\r\r获得道具：%s", typeString, number, money, propsInfo.getName()));
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(message,
                    "猜签成功:\r\n猜签类型:%s\r\n猜签号码:%s\r\n猜签WDIT币币:%s", typeString, number, money));
        }

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
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(bot == null){
            Log.info("[猜签-发生异常]-获取bot为空");
            return;
        }
        String key = lotteryInfo.getId() + "-" + lotteryInfo.getQq() + "-" + lotteryInfo.getGroup() + "-" + lotteryInfo.getNumber();
        switch (type) {
            case 1:
                GRAND_LOTTO_LOTTERY.remove(key);
                break;
            case 2:
                UNION_LOTTO.remove(key);
                break;
            case 3:
                THREE_QIU.remove(key);
        }
        lotteryInfo.remove();
        if (location == 0) {
            return;
        }
        Group group = bot.getGroup(lotteryInfo.getGroup());
        NormalMember member = group.get(lotteryInfo.getQq());
        assert member != null;
        if (!EconomyUtil.plusMoneyToUser(member, lotteryInfo.getBonus())) {
            member.sendMessage("奖金添加失败，请联系管理员!");
            return;
        }
//        member.sendMessage(lotteryInfo.toMessage());
//        if (location == 3) {
//            group.sendMessage(String.format("得签着:%s(%s),奖励%sWDIT币币", member.getNick(), member.getId(), lotteryInfo.getBonus()));
//        }
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
        Log.info("LotteryMinutesTask-->open-->强制透");
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(bot == null){
            Log.info("[猜签-发生异常]-获取bot为空");
            return;
        }
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

        Log.info("LotteryMinutesTask-->中奖号码-强制透："+currentString);

        Set<Long> groups = new HashSet<>();
        Map<Long,List<LotteryLocationInfo>> longListConcurrentHashMap = new ConcurrentHashMap<>();
        for (LotteryInfo lotteryInfo : lotteryInfos) {
            Log.info("LotteryMinutesTask-->qq群->"+lotteryInfo.getGroup());
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
                    bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),160),2).doubleValue();
                    break;
                case 2:
                    bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),6),2).doubleValue();
                    break;
                case 1:
                    bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),0.7),2).doubleValue();
                    break;
            }
            lotteryInfo.setBonus(bonus);
            lotteryInfo.setCurrent(currentString.toString());
            lotteryInfo = lotteryInfo.save();
            LotteryManager.result(1, location, lotteryInfo);
            Log.info("LotteryMinutesTask-->lotteryInfo->"+lotteryInfo.getQq() +"-->location"+location);
            // 获取中奖者
            if (location != 0) {
                List<LotteryLocationInfo> list = Optional.ofNullable(longListConcurrentHashMap
                        .get(lotteryInfo.getGroup())).orElse(new CopyOnWriteArrayList<>());
                LotteryLocationInfo lotteryLocationInfo = new LotteryLocationInfo();
                lotteryLocationInfo.setLotteryInfo(lotteryInfo);
                lotteryLocationInfo.setLocation(location);
                list.add(lotteryLocationInfo);
                longListConcurrentHashMap.put(lotteryInfo.getGroup(), list);
            }
        }
        for (Long group : groups) {
            sendTextMessae(currentString, longListConcurrentHashMap, group, bot);
        }
        lotteryInfos = new ArrayList<>();
        //定时任务执行完成，清除自身  我这里需要 其实可以不用
        CronUtil.remove(id);

        Log.info("LotteryMinutesTask-强制透-->end");

    }

    private void sendTextMessae(StringBuilder currentString, Map<Long, List<LotteryLocationInfo>> longListConcurrentHashMap, Long group, Bot bot) {
        Message m = new PlainText(String.format("本期强制透开签啦！\n开签: %s", currentString) + "\r\n");
        List<LotteryLocationInfo> list = Optional.ofNullable(longListConcurrentHashMap.get(group)).orElse(new CopyOnWriteArrayList<>());
        for(int i = 0 ; i <list.size() ; i ++ ){
            LotteryLocationInfo l = list.get(i);
            m = m.plus("\r\n");
            m = m.plus(new At(l.getLotteryInfo().getQq())
                    .plus("购买号码：" + l.getLotteryInfo().getNumber()+" "+"中奖金额："+ l.getLotteryInfo().getBonus() +"💰" + "\r\n"));
        }
        Objects.requireNonNull(bot.getGroup(group)).sendMessage(m);
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
        Log.info("LotteryHoursTask-->open-->缺德球");
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(bot == null){
            Log.info("[猜签-发生异常]-获取bot为空");
            return;
        }
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
        Log.info("LotteryMinutesTask-->中奖号码-缺德球："+currentString);

        Set<Long> groups = new HashSet<>();

        // 中奖列表
        Map<Long,List<LotteryLocationInfo>> longListConcurrentHashMap = new ConcurrentHashMap<>();
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
                    bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),1225),2).doubleValue();
                    break;
                case 3:
                    if (split[3].equals(current[3])) {
                        bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),625),2).doubleValue();
                    } else {
                        bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),35),2).doubleValue();
                    }
                    break;
                case 2:
                    if (split[3].equals(current[3])) {
                        bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),6.25),2).doubleValue();
                    } else {
                        bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),2.5),2).doubleValue();
                    }
                    break;
                case 1:
                    bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),0.5),2).doubleValue();
                    break;
            }
            Log.info("LotteryMinutesTask-->中奖号码-缺德球-特别号码："+current[3]);

            lotteryInfo.setBonus(bonus);
            lotteryInfo.setCurrent(currentString.toString());
            lotteryInfo = lotteryInfo.save();
            // 转账操作
            LotteryManager.result(2, location, lotteryInfo);
            Log.info("LotteryMinutesTask-->lotteryInfo->"+lotteryInfo.getQq() +"-->location"+location);

            // 获取中奖者
            if (location != 0) {
                List<LotteryLocationInfo> list = Optional.ofNullable(longListConcurrentHashMap
                        .get(lotteryInfo.getGroup())).orElse(new CopyOnWriteArrayList<>());
                LotteryLocationInfo lotteryLocationInfo = new LotteryLocationInfo();
                lotteryLocationInfo.setLotteryInfo(lotteryInfo);
                lotteryLocationInfo.setLocation(location);
                list.add(lotteryLocationInfo);
                longListConcurrentHashMap.put(lotteryInfo.getGroup(), list);
            }
        }
        for (Long group : groups) {
            sendTextMessae(currentString,longListConcurrentHashMap,group,bot,current[3]);
        }
        CronUtil.remove(id);
        Log.info("LotteryMinutesTask-缺德球-->end");

    }

    private void sendTextMessae(StringBuilder currentString, Map<Long, List<LotteryLocationInfo>> longListConcurrentHashMap, Long group, Bot bot,String current) {
        Message m = new PlainText(String.format("本期缺德球开签啦！\r\n开签号码%s \r\n特别号码%s", currentString, current) + "\r\n");
        List<LotteryLocationInfo> list = Optional.ofNullable(longListConcurrentHashMap.get(group)).orElse(new CopyOnWriteArrayList<>());
        if (!CollectionUtil.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                LotteryLocationInfo l = list.get(i);
                m = m.plus("\r\n");
                m = m.plus(new At(l.getLotteryInfo().getQq())
                        .plus("购买号码：" + l.getLotteryInfo().getNumber()+" "+"中奖金额："+ l.getLotteryInfo().getBonus() +"💰" + "\r\n"));
            }
        }else {
            m = m.plus("根本没有人中奖！\uD83E\uDD7A"+  "\r\n");
        }

        Objects.requireNonNull(bot.getGroup(group)).sendMessage(m);
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
    private  List<LotteryInfo> lotteryInfos;

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
        Log.info("LotteryDayTask-->open-->三色球");
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(bot == null){
            Log.info("[猜签-发生异常]-获取bot为空");
            return;
        }
        List<String> listResult = new ArrayList<>(3);
        // 姐一妹一互攻
        listResult.add("姐一");
        listResult.add("妹一");
        listResult.add("互攻");
        String current = RandomUtil.randomEle(listResult);

        Map<String, Set<String>> otherName = new HashMap<>(3);
        Set<String> j1 = new HashSet<>(2);
        j1.add("姐一");
        j1.add("姐1");
        otherName.put("姐一", j1);
        Set<String> m1 = new HashSet<>(2);
        m1.add("妹一");
        m1.add("妹1");
        otherName.put("妹一", m1);
        Set<String> m2 = new HashSet<>(1);
        m2.add("互攻");
        otherName.put("互攻", m2);

        Set<String> currentSet = otherName.get(current);
        Set<Long> groups = new HashSet<>();
        Map<Long,List<LotteryLocationInfo>> longListConcurrentHashMap = new ConcurrentHashMap<>();
        for (LotteryInfo lotteryInfo : lotteryInfos) {
            groups.add(lotteryInfo.getGroup());
            //位置正确的数量
            int location = 0;
            //计算奖金
            double bonus = 0;
            String number = lotteryInfo.getNumber();
            if(currentSet.contains(number)){
                location = 1;
                bonus = NumberUtil.round(NumberUtil.mul(lotteryInfo.getMoney(),2),2).doubleValue();
            }
            lotteryInfo.setBonus(bonus);
            lotteryInfo.setCurrent(current);
            lotteryInfo = lotteryInfo.save();
            LotteryManager.result(3, location, lotteryInfo);
            Log.info("LotteryDayTask-->lotteryInfo->"+lotteryInfo.getQq() +"-->location"+location);
            // 获取中奖者
            if (location != 0) {
                List<LotteryLocationInfo> list = Optional.ofNullable(longListConcurrentHashMap
                        .get(lotteryInfo.getGroup())).orElse(new CopyOnWriteArrayList<>());
                LotteryLocationInfo lotteryLocationInfo = new LotteryLocationInfo();
                lotteryLocationInfo.setLotteryInfo(lotteryInfo);
                lotteryLocationInfo.setLocation(location);
                list.add(lotteryLocationInfo);
                longListConcurrentHashMap.put(lotteryInfo.getGroup(), list);
            }
        }
        for (Long group : groups) {
            sendTextMessae(new StringBuilder(current), longListConcurrentHashMap, group, bot);
        }
        lotteryInfos = new ArrayList<>();

        CronUtil.remove(id);
        Log.info("LotteryMinutesTask-三色球-->end");
    }
    private void sendTextMessae(StringBuilder currentString,
                                Map<Long, List<LotteryLocationInfo>> longListConcurrentHashMap, Long group, Bot bot) {
        Message m = new PlainText(String.format("本期强制一开签啦！\n开签号码:%s", currentString) + "\r\n");
        List<LotteryLocationInfo> list =
                Optional.ofNullable(longListConcurrentHashMap.get(group)).orElse(new CopyOnWriteArrayList<>());
        if (!CollectionUtil.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                LotteryLocationInfo l = list.get(i);
                m = m.plus("\r\n");
                m = m.plus(new At(l.getLotteryInfo().getQq())
                        .plus("购买号码：" + l.getLotteryInfo().getNumber() + " " + "中奖金额：" + l.getLotteryInfo().getBonus() +
                                "💰" + "\r\n"));
            }
        } else {
            m = m.plus("根本没有人中奖！\uD83E\uDD7A" + "\r\n");
        }
        Objects.requireNonNull(bot.getGroup(group)).sendMessage(m);
    }
}
