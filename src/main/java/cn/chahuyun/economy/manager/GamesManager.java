package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.BuffPropsEnum;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.dto.AutomaticFish;
import cn.chahuyun.economy.dto.Buff;
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
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
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
        Group group;
        if (subject instanceof Group) {
            group = (Group) subject;
        } else {
            group = null;
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
        fishPond.setName("日夜颠岛");
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
        Log.info(String.format("%s消耗%s币币开始钓鱼", userInfo.getName(), Optional.ofNullable(userPay.get(user.getId())).orElse(0.0)));

      //  String[] errorMessages = new String[]{"钓鱼失败:哎呀，风吹的……", "钓鱼失败:哎呀，眼花了……", "钓鱼失败:bobo摇头", "钓鱼失败:呀！切线了！", "钓鱼失败:什么都没有钓上来！"};

        //随机睡眠
        try {
            Thread.sleep(RandomUtil.randomInt(5 * 60 * 1000, 60 * 60 * 1000));
//            Thread.sleep(RandomUtil.randomInt(100, 6000));
        } catch (InterruptedException e) {
            Log.debug(e);
        }

        // 困难度
        // 溜鱼增加difficultymin，之前的difficultymin=1+根号(14*RodLevel)
        int difficultyMin = (int) (1 + Math.sqrt(userFishInfo.getRodLevel() * 14));
        int difficultyMax = 99 + userFishInfo.getRodLevel();
        int rankMin = 1;
        int rankMax = userFishInfo.getRodLevel() / 8 + 2;

        Log.info("[fishing-start]" +
                ",difficultyMin:" + difficultyMin +
                ",difficultyMax:" + difficultyMax +
                ",rankMin:" + rankMin +
                ",rankMax:" + rankMax);
        subject.sendMessage(MessageUtils.newChain(new At(user.getId()), new PlainText("有动静了，快来！")));
        //开始拉扯
        while (true) {
            MessageEvent newMessage = ShareUtils.getNextMessageEventFromUser(user, subject, false);
            String nextMessageCode = newMessage.getMessage().serializeToMiraiCode();
            if (Pattern.matches("[!！收起提竿杆]{1,2}", nextMessageCode)) {
                user_pull.remove(user.getId());
                user_right.remove(user.getId());
                user_left.remove(user.getId());
                break;
            }
        }

        /**
         * 收杆时- 增加buff
         */
        List<Fish> fishList = new ArrayList<>();
        //彩蛋
        boolean winning = false;
        // 拉扯
        boolean rankStatus = true;
        // 钓鱼buff
        boolean otherFishB = false;
        int addDifficultyMin = 0;
        int addRankMin = 0;
        String buffName = "";
        Buff buff = CacheUtils.getBuff(group.getId(), userInfo.getQq());
        if (Objects.nonNull(buff)) {
            buffName = buff.getBuffName() + "-第" + ((buff.getNum() - buff.getCount() + 1)) + "杆";
            // 增加difficult
            addDifficultyMin = BuffUtils.getIntegerPropValue(buff, BuffPropsEnum.DIFFICULTY_MIN.getName());
            // 增加rankMin
            addRankMin = BuffUtils.getIntegerPropValue(buff, BuffPropsEnum.RANK_MIN.getName());
            // 上钩指定的鱼
            String specialFish = BuffUtils.getBooleanPropType(buff, BuffPropsEnum.SPECIAL_FISH.getName());
            if (!StrUtil.isBlank(specialFish)) {
                int specialLevel = Integer.parseInt(BuffUtils.getBooleanPropType(buff, BuffPropsEnum.SPECIAL_LEVEL.getName()));
                List<Fish> levelFishList = fishPond.getFishList(specialLevel);
                Fish special = levelFishList.stream().filter(fish -> specialFish.equals(fish.getName())).findFirst().get();
                fishList.add(special);
                rankStatus = false;
            }
            // 额外增加一条鱼
            String otherFish = BuffUtils.getBooleanPropType(buff, BuffPropsEnum.OTHER_FISH.getName());
            if(!StrUtil.isBlank(otherFish)){
                Log.info("otherFishB");
                otherFishB = true;
            }
            // 减去buff
            BuffUtils.reduceBuffCount(group.getId(), userInfo.getQq());
        }

        // 结束时 计算buff
        difficultyMin = difficultyMin + addDifficultyMin;
        difficultyMax = Math.max(difficultyMin, difficultyMax + 1);
        rankMin = rankMin + addRankMin;
        //roll等级
        int rank = rankMin;
        if (rankMin != rankMax + 1) {
            rank = RandomUtil.randomInt(Math.min(rankMin, rankMax + 1), Math.max(rankMin, rankMax + 1));
        }
        Log.info("[buff]-addDifficultyMin:" + addDifficultyMin + ",addRankMin:" + addRankMin);
        Log.info("[fishing-end]" +
                ",difficultyMin:" + difficultyMin +
                ",difficultyMax:" + difficultyMax +
                ",rankMin:" + rankMin +
                ",rankMax:" + rankMax +
                ",rank:" + rank);


        while (rankStatus) {
            if (rank == 0) {
                subject.sendMessage("切线了我去！");
                userFishInfo.switchStatus();
                return;
            }
            //roll难度
            int difficulty = RandomUtil.randomInt(difficultyMin, difficultyMax + 1);

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
            fishList.add(collect.get(RandomUtil.randomInt(size > 6 ? size - 6 : 0, size)));
            // 额外增加一条鱼
            if (otherFishB) {
                Log.info("额外增加一条鱼");
                fishList.add(collect.get(RandomUtil.randomInt(size > 6 ? size - 6 : 0, size)));
            }
            rankStatus = false;
            // break;
        }
        String finalBuffName = buffName;
        boolean finalWinning = winning;
        Group finalGroup = group;
        MessageChainBuilder messages = new MessageChainBuilder();
        messages.append(new At(userInfo.getQq()));
        fishList.forEach(fish->{
            //roll尺寸
            int dimensions = fish.getDimensions(finalWinning);
            int money = fish.getPrice() * dimensions;
            double v = money * (1 - fishPond.getRebate());

            NormalMember normalMember = finalGroup.get(HuYanEconomy.config.getOwner());
            if (Objects.nonNull(normalMember)) {
                EconomyUtil.plusMoneyToUser(normalMember, money * fishPond.getRebate());
            }
            // buff
            String buffDesc = StrUtil.isBlank(finalBuffName) ? "" : "[" + finalBuffName + "] ";
            // 道具
            if(fish.isSpecial()){
                String propCode = PropsType.getCode(fish.getName());
                Log.info("钓鱼系统:获取道具-Code " + propCode);
                PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(propCode);
                if (Objects.isNull(propsBase)) {
                    Log.error("钓鱼系统:获取道具为空");
                    // 折现-钓鱼
                    sendFishInfoMessage(userInfo, user, subject, fishPond, fish, dimensions, money, v, buffDesc,messages);
                }else {
                    UserBackpack userBackpack = new UserBackpack(userInfo, propsBase);
                    if (!userInfo.addPropToBackpack(userBackpack)) {
                        Log.error("钓鱼系统:添加道具到用户背包失败!");
                        // subject.sendMessage("系统出错，请联系主人!");
                        // 折现-钓鱼
                        sendFishInfoMessage(userInfo, user, subject, fishPond, fish, dimensions, money, v, buffDesc,messages);
                    }else {
                        String format = String.format("\r\n" + buffDesc + "起竿咯！获取道具 \r\n%s\r\n等级:%s\r\n单价:%s\r\n尺寸:%d\r\n" +
                                        "总金额:%d\r\n收益:%s\r\n%s\r\n",
                                fish.getName(), fish.getLevel(), fish.getPrice(), dimensions, money,
                                propsBase.getName(), fish.getDescription());
                        messages.append(new PlainText(format));
                        // subject.sendMessage(messages.build());
                        Log.info("钓鱼系统:添加道具到用户-Code " + propCode);
                    }
                }
            }else {
                // 钓鱼
                sendFishInfoMessage(userInfo, user, subject, fishPond, fish, dimensions, money, v, buffDesc, messages);
            }
            if(!fish.isSpecial()){
                new FishRanking(userInfo.getQq(), userInfo.getName(), dimensions, money, userFishInfo.getRodLevel(), fish, fishPond).save();
            }
            // 存入日志
            WorldBossConfigManager.saveWorldBossUserLog(group.getId(), userInfo.getQq(), dimensions);
            messages.append("-----------\r\n");
        });

        if (RandomHelperUtil.checkRandomLuck1_1000()) {
            int level = userFishInfo.getRodLevel();
            userFishInfo.downFishRod();
            messages.append("触发币币回收计划之：每次上钩都有0.1%的概率鱼竿折断，掉两级\r\n");
            messages.append(String.format("哇！你的鱼竿更强了! %s->%s\r\n", level, userFishInfo.getRodLevel()));
        }

        subject.sendMessage(messages.build());
        userFishInfo.switchStatus();
    }

    private static void sendFishInfoMessage(UserInfo userInfo, User user, Contact subject, FishPond fishPond,
                                            Fish fish, int dimensions, int money, double v,String buffDesc, MessageChainBuilder messages ) {
        v = NumberUtil.round(v, 2).doubleValue();
        if (EconomyUtil.plusMoneyToUser(user, v)
                && EconomyUtil.plusMoneyToBankForId(fishPond.getCode(), fishPond.getDescription(),
                money * fishPond.getRebate())) {
            fishPond.addNumber();
            String format = String.format("\r\n" + buffDesc + "起竿咯！\r\n%s\r\n等级:%s\r\n单价:%s\r\n尺寸:%d\r\n总金额:%d\r\n" +
                            "收益:%s\r\n%s\r\n",
                    fish.getName(), fish.getLevel(), fish.getPrice(), dimensions, money, v+"", fish.getDescription());
//            MessageChainBuilder messages = new MessageChainBuilder();
            messages.append(new PlainText(format));
          //  subject.sendMessage(messages.build());
        } else {
            subject.sendMessage("钓鱼失败!");
            playerCooling.remove(userInfo.getQq());
        }
    }

    public static Boolean checkUserPay(User user) {
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
            query.where(builder.equal(from.get("status"), true), builder.equal(from.get("qq"), senderId));
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

    public static AutomaticFish getAutomaticFish(User user, Group group) {
        Log.info("[自动钓鱼机-自动钓鱼] 开始：" + user.getId() + "," + group.getId());
        UserInfo userInfo = UserManager.getUserInfo(user);
        //获取玩家钓鱼信息
        FishInfo userFishInfo = userInfo.getFishInfo();
        // 鱼池信息
        FishPond fishPond = userFishInfo.getFishPond();
        if(fishPond == null){
            Log.error("[自动钓鱼机-发生异常] fishPon null");
            return new AutomaticFish("[鱼呢]", "切线了", 0, 0,"");
        }
        double result = 1 + Math.sqrt(userFishInfo.getRodLevel() * 14);
        int difficultyMin =(int) result;
        int difficultyMax = 131;
        int rankMin = (int) (userFishInfo.getLevel() + 20) / 20;
        int rankMax = 1;
 //       rankMin = Math.max((userFishInfo.getLevel() / 8) + 1, rankMin);
        rankMax = Math.max(rankMin + 1, Math.min(userFishInfo.getLevel(), fishPond.getPondLevel()));
      //  int userFishLevel = (userFishInfo.getLevel() / 8) + 1;
       // rankMax = Math.max(Math.max(userFishLevel, rankMin), Math.min(userFishLevel, fishPond.getPondLevel()));
        Log.info("[自动钓鱼机-自动钓鱼-start] "
                +",difficultyMin:" + difficultyMin
                +",difficultyMax:" + difficultyMax
                +",rankMin:" + rankMin
                +",rankMax:" + rankMax);

        int rollIndex = RandomUtil.randomInt(0,3);
        Log.info("[自动钓鱼机-自动钓鱼] 随机-rollIndex:" + rollIndex);
        switch (rollIndex){
            case 1:
                int randomLeftInt = RandomUtil.randomInt(10, 50);
                difficultyMin += randomLeftInt;
                break;
            case 2:
                int randomRightInt = RandomUtil.randomInt(0, 20);
                difficultyMin += randomRightInt;
                // int randomRankMaxRight = RandomUtil.randomInt(1, 4);
                rankMax += 1;
                break;
            case 0:
                int randomPullInt = RandomUtil.randomInt(0, 30);
                difficultyMin = difficultyMin + randomPullInt;

                rankMax = rankMin;
                break;
        }
        difficultyMax = Math.max(difficultyMin + 1, difficultyMax + userFishInfo.getRodLevel());
        //roll等级
        int rank = rankMin;
        if (rankMin != rankMax + 1) {
            rank = RandomUtil.randomInt(Math.min(rankMin, rankMax + 1), Math.max(rankMin, rankMax + 1));
        }
        Log.info("[自动钓鱼机-自动钓鱼-end]" +
                ",difficultyMin:" + difficultyMin +
                ",difficultyMax:" + difficultyMax +
                ",rankMin:" + rankMin +
                ",rankMax:" + rankMax +
                ",rank:" + rank);
        //彩蛋
        boolean winning = false;
        Fish fish;
        while (true) {
            if (rank == 0) {
                return new AutomaticFish("[鱼呢]", "切线了", 0, 0,"");
            }
            //roll难度
            int difficulty = RandomUtil.randomInt(Math.min(difficultyMin, difficultyMax + 1), Math.max(difficultyMin, difficultyMax + 1));

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
            fish = collect.get(RandomUtil.randomInt(size > 6 ? size - 6 : 0, size));
            break;
        }

        AutomaticFish automaticFish;
        //roll尺寸
        int dimensions = fish.getDimensions(winning);
        int money = fish.getPrice() * dimensions;
        double v = money * (1 - fishPond.getRebate());

        NormalMember normalMember = group.get(HuYanEconomy.config.getOwner());
        if (Objects.nonNull(normalMember)) {
            EconomyUtil.plusMoneyToUser(normalMember, money * fishPond.getRebate());
        }
        if (fish.isSpecial()) {
            String propCode = PropsType.getCode(fish.getName());
            PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(propCode);
            if (Objects.isNull(propsBase)) {
                automaticFish = getAutomaticFishInfo(user,fishPond, fish, dimensions, money, v);
            } else {
                UserBackpack userBackpack = new UserBackpack(userInfo, propsBase);
                if (!userInfo.addPropToBackpack(userBackpack)) {
                    Log.error("自动钓鱼机:添加道具到用户背包失败!");
                    automaticFish = getAutomaticFishInfo(user,fishPond, fish, dimensions, money, v);
                } else {
                    automaticFish = getAutomaticPropCard(fish, dimensions, money);
                }
            }
        }else {
            // 钓鱼
            automaticFish = getAutomaticFishInfo(user,fishPond, fish, dimensions, money, v);
        }
        if(!fish.isSpecial()){
            new FishRanking(userInfo.getQq(), userInfo.getName(), dimensions, money, userFishInfo.getRodLevel(), fish, fishPond).save();
        }
        // 存入世界boss战日志
        WorldBossConfigManager.saveWorldBossUserLog(group.getId(), userInfo.getQq(), dimensions);

        if (RandomHelperUtil.checkRandomLuck1_1000()) {
            userFishInfo.downFishRod();
            automaticFish.setOtherMessage("触发币币回收计划之：每次上钩都有0.1%的概率鱼竿折断，掉两级\r\n");
        }
        Log.info("[自动钓鱼机-自动钓鱼-结束]");
        return automaticFish;
    }

    private static AutomaticFish getAutomaticPropCard(Fish fish, int dimensions, int money) {
        String message = String.format("[道具]%s|等级:%s|单价:%s|尺寸:%d|总金额:%d",
                fish.getName(), fish.getLevel(), fish.getPrice(), dimensions, money);
        return new AutomaticFish(fish.getName(), message, money, 0,"");
    }

    private static AutomaticFish getAutomaticFishInfo(User user, FishPond fishPond, Fish fish, int dimensions,
                                                      int money, double v) {
        v = NumberUtil.round(v, 2).doubleValue();
        if (EconomyUtil.plusMoneyToUser(user, v)
                && EconomyUtil.plusMoneyToBankForId(fishPond.getCode(), fishPond.getDescription(),
                money * fishPond.getRebate())) {
            fishPond.addNumber();
            String message = String.format("[鱼]%s|等级:%s|单价:%s|尺寸:%d|总金额:%d|收益:%s", fish.getName(), fish.getLevel(),
                    fish.getPrice(),
                    dimensions, money, v + "");
            return new AutomaticFish(fish.getName(), message, money, v,"");
        } else {
            return new AutomaticFish("[鱼呢]", "鱼溜了", 0, 0,"");
        }
    }
}
