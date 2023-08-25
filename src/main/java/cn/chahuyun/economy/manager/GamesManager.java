package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.entity.fish.FishRanking;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.PropsType;
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

    public static final Map<Long, Double> userPay = new HashMap<>();

    public static final Set<Long> user_left = new HashSet<>();
    public static final Set<Long> user_right = new HashSet<>();
    public static final Set<Long> user_pull = new HashSet<>();

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
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();
        //能否钓鱼
        if (!userFishInfo.isFishRod()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "没有鱼竿，bobo也帮不了你🥹"));
            return;
        }
        //是否已经在钓鱼
        if (userFishInfo.getStatus()) {
            Double constMoney = userPay.get(user.getId());
            Boolean checkUser = checkUserPay(user);
            if (checkUser) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,还你%s💰", Optional.ofNullable(constMoney).orElse(0.0)));
            } else {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
            }
            return;
        }
        //钓鱼冷却
        if (playerCooling.containsKey(userInfo.getQq())) {
            Date date = playerCooling.get(userInfo.getQq());
            long between = DateUtil.between(date, new Date(), DateUnit.MINUTE, true);
            if (between < 5) {
                Double constMoney = userPay.get(user.getId());
                Boolean checkUser = checkUserPay(user);
                if (checkUser) {
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,你还差%s分钟来抛第二杆!,还你%s币币", 5 - between, Optional.ofNullable(constMoney).orElse(0.0)));
                } else {
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
                }
                return;
            } else {
                playerCooling.remove(userInfo.getQq());
            }
        } else {
            playerCooling.put(userInfo.getQq(), new Date());
        }
        //是否已经在钓鱼
        if (userFishInfo.isStatus()) {
            Double constMoney = userPay.get(user.getId());
            Boolean checkUser = checkUserPay(user);
            if (checkUser) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了,还你%s💰", Optional.ofNullable(constMoney).orElse(0.0)));
            } else {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
            }
            return;
        }
        //获取鱼塘
        FishPond fishPond = userFishInfo.getFishPond();
        if (fishPond == null) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "默认鱼塘不存在!"));
            return;
        }
        //获取鱼塘限制鱼竿最低等级
        int minLevel = fishPond.getMinLevel();
        if (userFishInfo.getRodLevel() < minLevel) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "鱼竿等级太低，bobo拒绝你在这里钓鱼\uD83D\uDE45\u200D♀️"));
            return;
        }
        String userName = userInfo.getName();
        if (Objects.nonNull(group)) {
            NormalMember member = group.get(userInfo.getQq());
            if (Objects.nonNull(member)) {
                userName = member.getNameCard();
            }
        }

        //开始钓鱼
        String start = String.format("%s开始钓鱼\n鱼塘:%s\n等级:%s\n最低鱼竿等级:%s\n%s", userName, fishPond.getName(), fishPond.getPondLevel(), fishPond.getMinLevel(), fishPond.getDescription());
        subject.sendMessage(start);
        Log.info(String.format("%s开始钓鱼", userInfo.getName()));

        String[] errorMessages = new String[]{"钓鱼失败:哎呀，风吹的……", "钓鱼失败:哎呀，眼花了……", "钓鱼失败:bobo摇头", "钓鱼失败:呀！切线了！", "钓鱼失败:什么都没有钓上来！"};

        //随机睡眠
        try {
            Thread.sleep(RandomUtil.randomInt(5 * 60 * 1000, 60 * 60 * 1000));
//            Thread.sleep(RandomUtil.randomInt(100, 6000));
        } catch (InterruptedException e) {
            Log.debug(e);
        }


        //初始钓鱼信息
        boolean theRod = false;
        // 困难度
        // 溜鱼增加difficultymin，之前的difficultymin=1+根号(14*RodLevel)
        double result =1 + Math.sqrt(userFishInfo.getRodLevel() * 14);
        int difficultyMin = (int) result;
        int difficultyMax = 131;
        int rankMin = 1;
        int rankMax = 1;
        rankMin = Math.max((userFishInfo.getLevel() / 8) + 1, rankMin);
        rankMax = Math.max(rankMin + 1, Math.min(userFishInfo.getLevel(), fishPond.getPondLevel()));

        Log.info("start-->--------------------------->");
        Log.info("difficultyMin-->"+ difficultyMin);
        Log.info("difficultyMax-->"+ difficultyMax);
        Log.info("rankMin-->"+ rankMin);
        Log.info("rankMax-->"+ rankMax);
        subject.sendMessage(MessageUtils.newChain(new At(user.getId()), new PlainText("有动静了，快来！")));
        //开始拉扯
        boolean rankStatus = true;
        int pull = 0;
        while (rankStatus) {
            MessageEvent newMessage = ShareUtils.getNextMessageEventFromUser(user, subject, false);
            String nextMessageCode = newMessage.getMessage().serializeToMiraiCode();
            switch (nextMessageCode) {
                case "向左拉":
                case "左":
                case "1":
                    if(user_right.contains(user.getId()) || user_left.contains(user.getId())){
                        break;
                    }
                    pull = pull + 1;
                    int randomLeftInt = RandomUtil.randomInt(10, 50);
                    difficultyMin += randomLeftInt;
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "\uD83E\uDD16你横向拉动了鱼竿，最小难度%s", (randomLeftInt < 0 ? "-" : "+") + randomLeftInt));
                    user_left.add(user.getId());
                    break;
                case "向右拉":
                case "右":
                case "2":
                    if(user_left.contains(user.getId()) || user_right.contains(user.getId())){
                        break;
                    }
                    pull = pull + 1;
                    int randomRightInt = RandomUtil.randomInt(0, 20);
                    difficultyMin += randomRightInt;
                    // 计算rankMax
                    rankMax = Math.max(rankMin + 1, Math.min(userFishInfo.getLevel(), Math.min(fishPond.getPondLevel(), rankMax)));

                    int randomRankMaxRight = RandomUtil.randomInt(1, 4);
                    rankMax += randomRankMaxRight;
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "\uD83E\uDD16你纵向拉动了鱼竿，最小难度%s，最大等级+%s",
                            (randomRightInt<0 ?"-":"+")+randomRightInt, randomRankMaxRight));
                    user_right.add(user.getId());
                    break;
                case "放":
                case "0":
                    if(!(user_left.contains(user.getId()) || user_right.contains(user.getId()))){
                        break;
                    }
                    if(user_pull.contains(user.getId())){
                        break;
                    }
                    pull++;
                    int randomPullInt = RandomUtil.randomInt(0, 30);
                    difficultyMin = difficultyMin + randomPullInt;

                    rankMax = rankMin;
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "\uD83E\uDD16你把收回的线又放出去了！最小难度%s,最大等级=%s", (randomPullInt < 0 ? "-" : "+") + randomPullInt, rankMin));

                    user_pull.add(user.getId());
                    break;
                default:
                    if (Pattern.matches("[!！收起提竿杆]{1,2}", nextMessageCode)) {
                        if (pull == 0) {
                            theRod = true;
                        }
                        user_pull.remove(user.getId());
                        user_right.remove(user.getId());
                        user_left.remove(user.getId());
                        rankStatus = false;
                    }
                    break;
            }
        }

        //空军
        if (theRod) {
            if (RandomUtil.randomInt(0, 101) >= 50) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), errorMessages[RandomUtil.randomInt(0, 5)]));
                userFishInfo.switchStatus();
                return;
            }
        }
        /*
        最小钓鱼等级 = max((钓鱼竿支持最大等级/5)+1,基础最小等级）
        最大钓鱼等级 = max(最小钓鱼等级+1,min(钓鱼竿支持最大等级,鱼塘支持最大等级,拉扯的等级))
        */
        // rankMin = Math.max((userFishInfo.getLevel() / 8) + 1, rankMin);
        // rankMax = Math.max(rankMin + 1, Math.min(userFishInfo.getLevel(), Math.min(fishPond.getPondLevel(), rankMax)));
        /*
        最小难度 = 拉扯最小难度
        最大难度 = max(拉扯最小难度,基本最大难度+鱼竿等级)
         */
        difficultyMax = Math.max(difficultyMin + 1, difficultyMax + userFishInfo.getRodLevel());
        //roll等级
        int rank = RandomUtil.randomInt(rankMin, rankMax + 1);


        Log.info("difficultyMin-->"+ difficultyMin);
        Log.info("difficultyMax-->"+ difficultyMax);
        Log.info("rankMin-->"+ rankMin);
        Log.info("rankMax-->"+ rankMax);
        Log.info("rank-->"+ rank);
        Log.info("end-->--------------------------->");
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
            int difficulty;
            if (difficultyMin > difficultyMax) {
                difficulty = RandomUtil.randomInt(difficultyMax, difficultyMin);
            } else if (difficultyMin == difficultyMax) {
                difficulty = difficultyMin;
            } else {
                difficulty = RandomUtil.randomInt(difficultyMin, difficultyMax);
            }
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

        if (Objects.nonNull(group)) {
            NormalMember normalMember = group.get(HuYanEconomy.config.getOwner());
            if (Objects.nonNull(normalMember)) {
                EconomyUtil.plusMoneyToUser(normalMember, money * fishPond.getRebate());
            }
        }
        // 道具
        if(fish.isSpecial()){
            String propCode = PropsType.getCode(fish.getName());
            Log.info("钓鱼系统:获取道具-Code " + propCode);
            PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(propCode);
            if (Objects.isNull(propsBase)) {
                Log.error("钓鱼系统:获取道具为空");
                // 折现-钓鱼
                sendFishInfoMessage(userInfo, user, subject, fishPond, fish, dimensions, money, v);
            }else {
                UserBackpack userBackpack = new UserBackpack(userInfo, propsBase);
                if (!userInfo.addPropToBackpack(userBackpack)) {
                    Log.error("钓鱼系统:添加道具到用户背包失败!");
                    // 折现-钓鱼
                    sendFishInfoMessage(userInfo, user, subject, fishPond, fish, dimensions, money, v);
                } else {
                    String format = String.format("\r\n起竿咯！获取道具 \r\n%s\r\n等级:%s\r\n单价:%s\r\n尺寸:%d\r\n总金额:%d\r\n%s", fish.getName(), fish.getLevel(), fish.getPrice(), dimensions, money, fish.getDescription());
                    MessageChainBuilder messages = new MessageChainBuilder();
                    messages.append(new At(userInfo.getQq())).append(new PlainText(format));
                    subject.sendMessage(messages.build());
                }
                Log.info("钓鱼系统:添加道具到用户-Code " + propCode);
            }
        }else {
            // 钓鱼
            sendFishInfoMessage(userInfo, user, subject, fishPond, fish, dimensions, money, v);
        }
        userFishInfo.switchStatus();
        new FishRanking(userInfo.getQq(), userInfo.getName(), dimensions, money, userFishInfo.getRodLevel(), fish, fishPond).save();
    }

    private static void sendFishInfoMessage(UserInfo userInfo, User user, Contact subject, FishPond fishPond, Fish fish, int dimensions, int money, double v) {
        if (EconomyUtil.plusMoneyToUser(user, v) && EconomyUtil.plusMoneyToBankForId(fishPond.getCode(), fishPond.getDescription(), money * fishPond.getRebate())) {
            fishPond.addNumber();
            String format = String.format("\r\n起竿咯！\r\n%s\r\n等级:%s\r\n单价:%s\r\n尺寸:%d\r\n总金额:%d\r\n%s",
                    fish.getName(), fish.getLevel(), fish.getPrice(), dimensions, money, fish.getDescription());
            MessageChainBuilder messages = new MessageChainBuilder();
            messages.append(new At(userInfo.getQq())).append(new PlainText(format));
            subject.sendMessage(messages.build());
        } else {
            subject.sendMessage("钓鱼失败!");
            playerCooling.remove(userInfo.getQq());
        }
    }

    private static Boolean checkUserPay(User user) {
        Double constMoney = userPay.get(user.getId());
        if (Objects.nonNull(constMoney)) {
            EconomyUtil.plusMoneyToUser(user, constMoney);
            userPay.remove(user.getId());
            return true;
        }
        return false;
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
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经有一把钓鱼竿了，不用再买了！"));
            return;
        }

        double moneyByUser = EconomyUtil.getMoneyByUser(user);
        if (moneyByUser - 250 < 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "\uD83E\uDD16只要250枚耀眼的WDIT币币，才能买到这么神奇的鱼竿！你有这么多币币吗？！"));
            return;
        }

        if (EconomyUtil.minusMoneyToUser(user, 250)) {
            fishInfo.setFishRod(true);
            fishInfo.save();
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "收好你的鱼竿，高定产品，bobo不提供售后！"));
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
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "没有鱼竿，bobo不能帮你升级!"));
            return;
        }
        if (fishInfo.getStatus()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "钓鱼\uD83C\uDFA3期间不可升级鱼竿!"));
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
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "暂时没人钓鱼!"));
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
        userPay.clear();
        user_pull.clear();
        user_right.clear();
        user_left.clear();
        if (status) {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "钓鱼状态刷新成功!"));
        } else {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "钓鱼状态刷新成功!"));
        }
    }

    /**
     * 刷新钓鱼状态
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/16 11:04
     */
    public static void refresh(MessageEvent event,Long senderId) {
        Boolean status = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<FishInfo> query = builder.createQuery(FishInfo.class);
            JpaRoot<FishInfo> from = query.from(FishInfo.class);
            query.select(from);
            query.where(builder.equal(from.get("status"), true));
            query.where(builder.equal(from.get("qq"), senderId));
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
        playerCooling.remove(senderId);
        userPay.remove(senderId);
        user_pull.remove(senderId);
        user_right.remove(senderId);
        user_left.remove(senderId);
        if (status) {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "钓鱼状态刷新成功!"));
        } else {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "钓鱼状态刷新成功!"));
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
