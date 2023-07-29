package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.entity.fish.FishRanking;
import cn.chahuyun.economy.utils.*;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 游戏管理<p>
 * 24点|钓鱼<p>
 *
 * @author Moyuyanli
 * @date 2022/11/14 12:26
 */
public class GamesManager {

    /**
     * 玩家钓鱼冷却
     */
    public static final Map<Long, Date> playerCooling = new HashMap<>();

    private GamesManager() {
    }

    /**
     * 开始钓鱼游戏
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/9 16:16
     */
    public static void fishing(MessageEvent event) {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();
        Contact subject = event.getSubject();
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();
        //能否钓鱼
        if (!userFishInfo.isFishRod()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"没有鱼竿，bobo也帮不了你🥹"));
            return;
        }
        //是否已经在钓鱼
        if (userFishInfo.getStatus()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"你已经在钓鱼了！"));
            return;
        }
        //钓鱼冷却
        if (playerCooling.containsKey(userInfo.getQq())) {
            Date date = playerCooling.get(userInfo.getQq());
            long between = DateUtil.between(date, new Date(), DateUnit.SECOND, true);
            if (between < 10) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"你还差%s秒来抛第二杆!", 10 - between));
                return;
            } else {
                playerCooling.remove(userInfo.getQq());
            }
        } else {
            playerCooling.put(userInfo.getQq(), new Date());
        }
        //是否已经在钓鱼
        if (userFishInfo.isStatus()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"你已经在钓鱼了！"));
            return;
        }
        //获取鱼塘
        FishPond fishPond = userFishInfo.getFishPond();
        if (fishPond == null) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"默认鱼塘不存在!"));
            return;
        }
        //获取鱼塘限制鱼竿最低等级
        int minLevel = fishPond.getMinLevel();
        if (userFishInfo.getRodLevel() < minLevel) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"鱼竿等级太低，bobo拒绝你在这里钓鱼\uD83D\uDE45\u200D♀️"));
            return;
        }
        //开始钓鱼
        String start = String.format("%s开始钓鱼\n鱼塘:%s\n等级:%s\n最低鱼竿等级:%s\n%s", userInfo.getName(), fishPond.getName(), fishPond.getPondLevel(), fishPond.getMinLevel(), fishPond.getDescription());
        subject.sendMessage(start);
        Log.info(String.format("%s开始钓鱼", userInfo.getName()));

        //初始钓鱼信息
        boolean theRod = false;
        // 困难度
        int difficultyMin = 0;
        int difficultyMax = 101;
        int rankMin = 1;
        int rankMax = 1;

        String[] successMessages = new String[]{"🎣溜成功了！(高价🐠概率+6)", "🎣轻松收线！(高价🐠概率+6)", "🎣慢慢的、慢慢的...(高价🐠概率+6)"};
        String[] failureMessages = new String[]{"😣拉不动了！(高价🐟概率-5)", "😣是不是操作失误了？(高价🐟概率-5)", "😣bobo开始怀疑你的钓鱼水平？(高价🐟概率-5)"};
        String[] otherMessages = new String[]{"🤗钓鱼就是这么简单(高价🐠概率+8)", "🤗太轻松了，能钓到大鱼吗(高价🐠概率+8)", "🤗收线~~！(高价🐠概率+8)"};
        String[] errorMessages = new String[]{"钓鱼失败:哎呀，风吹的……", "钓鱼失败:哎呀，眼花了……", "钓鱼失败:bobo摇头", "钓鱼失败:呀！切线了！", "钓鱼失败:什么都没有钓上来！"};


        //随机睡眠
        try {
            Thread.sleep(RandomUtil.randomInt(30000, 300000));
        } catch (InterruptedException e) {
            Log.debug(e);
        }
        subject.sendMessage(MessageUtils.newChain(new At(user.getId()), new PlainText("有动静了，快来！")));
        //开始拉扯
        boolean rankStatus = true;
        int pull = 0;
        while (rankStatus) {
            //获取下一条消息
            MessageEvent newMessage = ShareUtils.getNextMessageEventFromUser(user, subject, false);
            String nextMessageCode = newMessage.getMessage().serializeToMiraiCode();
            int randomInt = RandomUtil.randomInt(0, 3);
            switch (nextMessageCode) {
                case "向左拉":
                case "左":
                case "1":
                    if (randomInt == 1) {
                        difficultyMin += 6;
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), successMessages[randomInt]));
                        // subject.sendMessage(successMessages[randomInt]);
                    } else {
                        difficultyMin -= 5;
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), failureMessages[randomInt]));
                        // subject.sendMessage(failureMessages[randomInt]);
                    }
                    break;
                case "向右拉":
                case "右":
                case "2":
                    if (randomInt == 2) {
                        difficultyMin += 6;
                        // subject.sendMessage(successMessages[randomInt]);
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), successMessages[randomInt]));
                    } else {
                        difficultyMin -= 5;
                        // subject.sendMessage(failureMessages[randomInt]);
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), failureMessages[randomInt]));
                    }
                    break;
                case "收线":
                case "拉":
                case "收":
                case "0":
                    if (randomInt == 0) {
                        difficultyMin += 8;
                        // subject.sendMessage(otherMessages[randomInt]);
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), otherMessages[randomInt]));
                    } else {
                        difficultyMin -= 5;
//                        subject.sendMessage(failureMessages[randomInt]);
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), failureMessages[randomInt]));
                    }
                    rankMax++;
                    break;
                case "放线":
                case "放":
                case "~":
                    difficultyMin += 20;
                    rankMax = 1;
                    // subject.sendMessage("你把你收回来的线，又放了出去!");
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你把你收回来的线，又放了出去!"));

                    break;
                default:
                    if (Pattern.matches("[!！收起提竿杆]{1,2}", nextMessageCode)) {
                        if (pull == 0) {
                            theRod = true;
                        }
                        rankStatus = false;
                    }
                    break;
            }
            pull++;
        }
        //空军
        if (theRod) {
            if (RandomUtil.randomInt(0, 101) >= 50) {
//                subject.sendMessage(errorMessages[RandomUtil.randomInt(0, 5)]);
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), errorMessages[RandomUtil.randomInt(0, 5)]));
                userFishInfo.switchStatus();
                return;
            }
        }

        /*
        最小钓鱼等级 = max((钓鱼竿支持最大等级/5)+1,基础最小等级）
        最大钓鱼等级 = max(最小钓鱼等级+1,min(钓鱼竿支持最大等级,鱼塘支持最大等级,拉扯的等级))
         */
        rankMin = Math.max((userFishInfo.getLevel() / 5) + 1, rankMin);
        rankMax = Math.max(rankMin + 1, Math.min(userFishInfo.getLevel(), Math.min(fishPond.getPondLevel(), rankMax)));
        /*
        最小难度 = 拉扯最小难度
        最大难度 = max(拉扯最小难度,基本最大难度+鱼竿等级)
         */
        difficultyMax = Math.max(difficultyMin + 1, difficultyMax + userFishInfo.getRodLevel());
        //roll等级
        int rank = RandomUtil.randomInt(rankMin, rankMax + 1);
        Log.debug("钓鱼管理:roll等级min" + rankMin);
        Log.debug("钓鱼管理:roll等级max" + rankMax);
        Log.debug("钓鱼管理:roll等级" + rank);
        Fish fish;
        //彩蛋
        boolean winning = false;
        while (true) {
            if (rank == 0) {
                subject.sendMessage("切线了我去！");
                userFishInfo.switchStatus();
                return;
            }
            //roll难度
            int difficulty = RandomUtil.randomInt(difficultyMin, difficultyMax);
            Log.debug("钓鱼管理:等级:" + rank + "-roll难度min" + difficultyMin);
            Log.debug("钓鱼管理:等级:" + rank + "-roll难度max" + difficultyMax);
            Log.debug("钓鱼管理:等级:" + rank + "-roll难度" + difficulty);
            //在所有鱼中拿到对应的鱼等级
            List<Fish> levelFishList = fishPond.getFishList(rank);
            //过滤掉难度不够的鱼
            List<Fish> collect;
            collect = levelFishList.stream().filter(it -> it.getDifficulty() <= difficulty).collect(Collectors.toList());
            //如果没有了
            int size = collect.size();
            if (size == 0) {
                //降级重新roll难度处理
                rank--;
                continue;
            }
            //难度>=200 触发彩蛋
            if (difficulty >= 200) {
                winning = true;
            }
            //roll鱼
            fish = collect.get(RandomUtil.randomInt(size > 6 ? size - 6 : 0, size));
            break;
        }
        //roll尺寸
        int dimensions = fish.getDimensions(winning);
        int money = fish.getPrice() * dimensions;
        double v = money * (1 - fishPond.getRebate());
        Log.info("当前扣的点是: " + fishPond.getRebate());
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        if (Objects.nonNull(group)) {
            NormalMember normalMember = group.get(HuYanEconomy.config.getOwner());
            if(Objects.nonNull(normalMember)){
                EconomyUtil.plusMoneyToUser(normalMember, money * fishPond.getRebate());
            }
        }
        if (EconomyUtil.plusMoneyToUser(user, v) && EconomyUtil.plusMoneyToBankForId(fishPond.getCode(), fishPond.getDescription(), money * fishPond.getRebate())) {
            fishPond.addNumber();
            String format = String.format("\n起竿咯！\n%s\n等级:%s\n单价:%s\n尺寸:%d\n总金额:%d\n%s", fish.getName(), fish.getLevel(), fish.getPrice(), dimensions, money, fish.getDescription());
            MessageChainBuilder messages = new MessageChainBuilder();
            messages.append(new At(userInfo.getQq())).append(new PlainText(format));
            subject.sendMessage(messages.build());
        } else {
            subject.sendMessage("钓鱼失败!");
            playerCooling.remove(userInfo.getQq());
        }
        userFishInfo.switchStatus();
        new FishRanking(userInfo.getQq(), userInfo.getName(), dimensions, money, userFishInfo.getRodLevel(), fish, fishPond).save();
    }

    /**
     * 购买鱼竿
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/9 16:15
     */
    public static void buyFishRod(MessageEvent event) {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();
        FishInfo fishInfo = userInfo.getFishInfo();

        Contact subject = event.getSubject();

        if (fishInfo.isFishRod()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"你已经有一把钓鱼竿了，不用再买了！"));
            return;
        }

        double moneyByUser = EconomyUtil.getMoneyByUser(user);
        if (moneyByUser - 250 < 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"\uD83E\uDD16只要250枚耀眼的WDIT币币，才能买到这么神奇的鱼竿！你有这么多币币吗？！"));
            return;
        }

        if (EconomyUtil.minusMoneyToUser(user, 250)) {
            fishInfo.setFishRod(true);
            fishInfo.save();
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"收好你的鱼竿，高定产品，bobo不提供售后！"));
        } else {
            Log.error("游戏管理:购买鱼竿失败!");
        }
    }

    /**
     * 升级鱼竿
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/11 22:27
     */
    public static void upFishRod(MessageEvent event) {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());

        Contact subject = event.getSubject();

        FishInfo fishInfo = userInfo.getFishInfo();
        if (!fishInfo.isFishRod()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"没有鱼竿，bobo不能帮你升级!"));
            return;
        }
        if (fishInfo.getStatus()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"钓鱼\uD83C\uDFA3期间不可升级鱼竿!"));
            return;
        }
        SingleMessage singleMessage = fishInfo.updateRod(userInfo);
        subject.sendMessage(singleMessage);
    }


    /**
     * 钓鱼排行榜
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/14 15:27
     */
    public static void fishTop(MessageEvent event) {
        Bot bot = event.getBot();
        Contact subject = event.getSubject();
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();

        List<FishRanking> rankingList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<FishRanking> query = builder.createQuery(FishRanking.class);
            JpaRoot<FishRanking> from = query.from(FishRanking.class);
            query.select(from);
            query.orderBy(builder.desc(from.get("money")));
            List<FishRanking> list = session.createQuery(query).list();
            if (list.size() == 0) {
                return null;
            }
            return list.subList(0, Math.min(list.size(), 30));
        });
        if (rankingList == null || rankingList.size() == 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"暂时没人钓鱼!"));
            return;
        }
        ForwardMessageBuilder iNodes = new ForwardMessageBuilder(subject);
        iNodes.add(bot, new PlainText("钓鱼排行榜:"));

        int start = 0;
        int end = 10;

        for (int i = start; i < end && i < rankingList.size(); i++) {
            FishRanking ranking = rankingList.get(i);
            iNodes.add(bot, ranking.getInfo(i));
        }
//        while (true) {
//          todo 钓鱼榜分页
//        }

        subject.sendMessage(iNodes.build());
    }

    /**
     * 刷新钓鱼状态
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/16 11:04
     */
    public static void refresh(MessageEvent event) {
        Boolean status = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<FishInfo> query = builder.createQuery(FishInfo.class);
            JpaRoot<FishInfo> from = query.from(FishInfo.class);
            query.select(from);
            query.where(builder.equal(from.get("status"), true));
            List<FishInfo> list;
            try {
                list = session.createQuery(query).list();
            } catch (Exception e) {
                return false;
            }
            for (FishInfo fishInfo : list) {
                fishInfo.setStatus(false);
                session.merge(fishInfo);
            }
            return true;
        });
        playerCooling.clear();
        if (status) {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"钓鱼状态刷新成功!"));
        } else {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"钓鱼状态刷新成功!"));
        }
    }

/**
 * 查看鱼竿等级
 *
 * @param event 消息事件
 * @author Moyuyanli
 * @date 2022/12/23 16:12
 */
    public static void viewFishLevel(MessageEvent event) {
        int rodLevel = UserManager.getUserInfo(event.getSender()).getFishInfo().getRodLevel();
        event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你的鱼竿等级为%s级", rodLevel));
    }

}
