package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.aop.PropUtils;
import cn.chahuyun.economy.constant.DailyPropCode;
import cn.chahuyun.economy.constant.FishSignConstant;
import cn.chahuyun.economy.constant.PropExchangeDict;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.*;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RBloomFilter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 道具管理<p>
 *
 * @author Moyuyanli
 * @date 2022/11/14 12:27
 */
public class PropsManagerImpl implements PropsManager {

    /**
     * 注册道具<p>
     * 道具的<p>
     * [code] [name] [cost] [reuse]<p>
     * [description]<p>
     * 不能为空<p>
     *
     * @param propsBase
     */
    @Override
    public boolean registerProps(PropsBase propsBase) {
        String code;
        try {
            code = propsBase.getCode();
            if (StrUtil.isBlankIfStr(code)) {
                return false;
            }
            String description = propsBase.getDescription();
            if (StrUtil.isBlankIfStr(description)) {
                return false;
            }
            String name = propsBase.getName();
            if (StrUtil.isBlankIfStr(name)) {
                return false;
            }
        } catch (Exception e) {
            Log.error("道具管理:注册道具出错!");
            return false;
        }
        PropsType.add(code, propsBase);
        return true;
    }

    @Override
    public void clearProps() {
        PropsType.clear();
    }


    /**
     * 获取该用户的所有道具<p>
     *
     * @param userInfo 用户
     * @return List<E> 道具id集合
     */
    @Override
    public List<PropsBase> getPropsByUser(UserInfo userInfo) {
        //todo 获取该用户的所有道具
        List<PropsBase> props = new ArrayList<>();

        List<UserBackpack> backpacks = Optional.ofNullable(userInfo.getBackpacks()).orElse(new ArrayList<>());
        for (UserBackpack backpack : backpacks) {
            Class<? extends PropsBase> aClass;
            try {
                aClass = (Class<? extends PropsBase>) Class.forName(backpack.getClassName());
            } catch (ClassNotFoundException e) {
                Log.error("道具管理:获取所有道具-获取道具子类出错!", e);
                continue;
            }
            PropsBase fromSession = HibernateUtil.factory.fromSession(session -> session.get(aClass, backpack.getPropId()));
            props.add(fromSession);
        }
        return props;
    }


    /**
     * 获取该用户的对应 [code] 的道具<p>
     *
     * @param userInfo 用户
     * @param code     道具编码
     * @param clazz    对应道具的类
     * @return java.util.List<?> 道具集合
     * @author Moyuyanli
     * @date 2022/11/15 15:44
     */
    @Override
    public <E extends PropsBase> List<E> getPropsByUserFromCode(UserInfo userInfo, Class<E> clazz) {
        List<UserBackpack> backpacks = userInfo.getBackpacks();
        if (backpacks == null || backpacks.size() == 0) {
            return new ArrayList<>();
        }
        List<E> propList = new ArrayList<>();
        for (UserBackpack backpack : backpacks) {
//            if (backpack.getPropsCode().equals(code)) {
//                continue;
//            }
            E base = HibernateUtil.factory.fromSession(session -> session.get(clazz, backpack.getPropId()));
            propList.add(base);
        }
        return propList;
    }

    /**
     * 删除 [用户] 对应的 [道具]
     *
     * @param userInfo 用户
     * @param props    用户的道具
     * @param clazz    道具类型
     * @return true 成功删除
     */
    @Override
    public <E> boolean deleteProp(UserInfo userInfo, PropsBase props, Class<E> clazz) {
        return false;
    }

    /**
     * 删除 [用户] 对应的 [道具]
     *
     * @param userInfo 用户
     * @param props    用户道具
     * @return true 成功删除
     */
    @Override
    public UserInfo deleteProp(UserInfo userInfo, PropsBase props, int limit) {
        List<UserBackpack> backpacks = Optional.ofNullable(userInfo.getBackpacks()).orElse(new ArrayList<>());
        backpacks.stream().filter(Objects::nonNull)
                .filter(back -> back.getPropId() == props.getId())
                .limit(limit).forEach(UserBackpack::remove);
        return UserManager.getUserInfo(userInfo.getUser());
    }
//    @Override
//    public UserInfo deleteProp(UserInfo userInfo, PropsBase props, int deleteCount) {
//        List<UserBackpack> backpacks = Optional.ofNullable(userInfo.getBackpacks())
//                .orElse(new ArrayList<>());
//
//        // 使用迭代器安全删除[5][6]
//        Iterator<UserBackpack> it = backpacks.iterator();
//        int removed = 0;
//        while (it.hasNext() && removed < deleteCount) {
//            UserBackpack item = it.next();
//            if (item != null && item.getPropId() == props.getId()) {
//                it.remove();
//                removed++;
//            }
//        }
//        return UserManager.getUserInfo(userInfo.getUser());
//    }

    @Override
    public UserInfo deleteProp(UserInfo userInfo, PropsBase props) {
        return deleteProp(userInfo,props,1);
    }

    /**
     * 查询道具系统
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/11/23 10:36
     */
    @Override
    public void propStore(MessageEvent event) {
        //todo 后期尝试用反射来实现通过扫描道具的继承类实现道具系统
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

      //   ForwardMessageBuilder iNodes = new ForwardMessageBuilder(subject);
        ForwardMessageBuilder propCard = new ForwardMessageBuilder(subject);
       // iNodes.add(bot, new PlainText("道具系统"));
        propCard.add(bot, new PlainText("道具卡商店"));
        Set<String> strings = PropsType.getProps().keySet();
        List<String> stringsSort = strings.stream().sorted().collect(Collectors.toList());
        for (String string : stringsSort) {
            if (string.startsWith("K-")) {
                String propInfo = String.format("道具编号:%s\n", PropsType.getNo(string));
                propInfo += PropsType.getPropsInfo(string).toString();
                propCard.add(bot, new PlainText(propInfo));
            }
            if (string.startsWith("FISH-")) {
                if(PropsType.getPropsInfo(string) instanceof PropsFishCard){
                    PropsFishCard propsFishCard =(PropsFishCard)PropsType.getPropsInfo(string);
                    if(propsFishCard.getBuy() && !propsFishCard.getOffShelf()){
                        String propInfo = PropsType.getPropsInfo(string).toString();
                        propCard.add(bot, new PlainText(propInfo));
                    }
                }
            }
        }
        propCard.add(bot, new PlainText("兑换商店"));
        for (String string : stringsSort) {
            if (string.startsWith("FISH-")) {
                if(PropsType.getPropsInfo(string) instanceof PropsFishCard){
                    PropsFishCard propsFishCard =(PropsFishCard)PropsType.getPropsInfo(string);
                    if(propsFishCard.getExchange() && !propsFishCard.getOffShelf()){
                        String propInfo = PropsType.getPropsInfo(string).toString();
                        propCard.add(bot, new PlainText(propInfo));
                    }
                }
            }
        }
        // iNodes.add(bot, propCard.build());
        subject.sendMessage(propCard.build());

    }

    /**
     * 购买一个道具，加入到用户背包
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/11/28 15:05
     */
    @Override
    public void buyPropFromStore(MessageEvent event) {
        Contact subject = event.getSubject();
        User sender = event.getSender();
        MessageChain message = event.getMessage();

        MessageChainBuilder messages = MessageUtil.quoteReply(message);

        String code = message.serializeToMiraiCode();

        String[] s = code.split(" ");
        String no = s[1];
        int num = 1;
        if (s.length == 3) {
            num = Integer.parseInt(s[2]);
        }

        String propCode = PropsType.getCode(no);
        if (propCode == null) {
            Log.warning("道具系统:购买道具为空");
            subject.sendMessage(MessageUtil.formatMessageChain(message,"\uD83D\uDE23bobo没有这个……"));
            return;
        }
        Log.info("道具系统:购买道具-Code " + propCode);


        UserInfo userInfo = UserManager.getUserInfo(sender);
        if (userInfo == null) {
            Log.warning("道具系统:获取用户为空！");
            subject.sendMessage("系统出错，请联系主人!");
            return;
        }

        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        Integer cost = propsInfo.getCost();
        if(propsInfo instanceof PropsFishCard){
            PropsFishCard card = (PropsFishCard) propsInfo;
            if(card.getOffShelf()){
                messages.append(new PlainText("😣 ["+ propsInfo.getName() + "]已下架"));
                subject.sendMessage(messages.build());
                return;
            }
            if(!card.getBuy()){
                messages.append(new PlainText("😣 ["+ propsInfo.getName() + "]非卖品"));
                subject.sendMessage(messages.build());
                return;
            }
            //购买道具合计金额
            if (cost < 0) {
                // 100*rodlevel+900
                FishInfo userFishInfo = userInfo.getFishInfo();
                cost = 60 * userFishInfo.getRodLevel() + 200;
            }
            if ( "FISH-30".equals(card.getCode())) {
                if(num != 1){
                    messages.append(new PlainText("["  + propsInfo.getName() + "]每人每天限制购买1个"));
                    subject.sendMessage(messages.build());
                    return;
                }
                // 判断今天是否已经购买
                RBloomFilter<Long> rBloomFilter = RedisUtils.initOneDayPropBloomFilter(subject.getId(), card.getCode());
                if (rBloomFilter.contains(sender.getId())) {
                    messages.append(new PlainText("[" + propsInfo.getName() + "]每人每天限制购买1个"));
                    subject.sendMessage(messages.build());
                    return;
                }
            }
            if("FISH-2".equals(card.getCode())){
                if(num > 2){
                    messages.append(new PlainText("["  + propsInfo.getName() + "]每人每天最大购买2个"));
                    subject.sendMessage(messages.build());
                    return;
                }
                if(RedisUtils.getDogSisterCount(subject.getId(), sender.getId()) >= 2){
                    messages.append(new PlainText("["  + propsInfo.getName() + "]每人每天最大购买2个"));
                    subject.sendMessage(messages.build());
                    return;
                }
                IntStream.range(0, num)
                        .forEach(i -> RedisUtils.addDogSisterCount(subject.getId(), sender.getId()));
            }
            // 如果是购买wditbb
            if("FISH-51".equals(card.getCode())){
                if(num > 100000){
                    messages.append(new PlainText("["  + propsInfo.getName() + "]每人每天限制购买100000个"));
                    subject.sendMessage(messages.build());
                    return;
                }
                Double count = RedisUtils.getWditBBCount(subject.getId(), sender.getId());
                if(count + num > 100000){
                    messages.append(new PlainText("["  + propsInfo.getName() + "]每人每天限制购买100000个,目前你已经购买了" + count + "个"));
                    subject.sendMessage(messages.build());
                    return;
                }
                // new 赛季
                double userMoney = EconomyUtil.getMoneyByBank(sender);
                //购买道具合计金额
                int total = 2 * num;
                if (userMoney - total < 0) {
                    messages.append(new PlainText("没"+ SeasonCommonInfoManager.getSeasonMoney()+"就不要想买" + propsInfo.getName() + "！"));
                    subject.sendMessage(messages.build());
                    return;
                }
                if (!EconomyUtil.minusMoneyToBank(sender, total)) {
                    Log.warning("道具系统:减少余额失败!");
                    subject.sendMessage("系统出错，请联系主人!");
                    return;
                }

                if (!EconomyUtil.plusMoneyToUser(userInfo.getUser(), num)) {
                    messages.append("购买失败！");
                    subject.sendMessage(messages.build());
                    return;
                }else {
                    RedisUtils.setWditBBCount(subject.getId(), sender.getId(),count + num );
                    messages.append("成功购买" + num + " " + "获得" + num + " " + card.getName() + " 你还有" + (100000 - (count + num)) + "额度");
                    subject.sendMessage(messages.build());

                    SeasonManager.checkUserDailyWork(event, subject);
                    return;
                }

            }
        }
        //用户钱包现有余额
        //double money = EconomyUtil.getMoneyByUser(sender);
        // new 赛季币
        double money = EconomyUtil.getMoneyByBank(sender);
        //购买道具合计金额
        int total = cost * num;

        if (money - total < -cost * 5) {
            messages.append(new PlainText("😣"  + propsInfo.getName() + "可不能卖给你！"));
            subject.sendMessage(messages.build());
            return;
        } else if (money - total < 0) {
            messages.append(new PlainText("没"+ SeasonCommonInfoManager.getSeasonMoney()+"就不要想买" + propsInfo.getName() + "！"));
            subject.sendMessage(messages.build());
            return;
        }

       // if (!EconomyUtil.minusMoneyToUser(sender, total)) {
        // new 赛季币
        if (!EconomyUtil.minusMoneyToBank(sender, total)) {
            Log.warning("道具系统:减少余额失败!");
            subject.sendMessage("系统出错，请联系主人!");
            return;
        }
        PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(propCode);
        if (Objects.isNull(propsBase)) {
            Log.error("道具系统:道具创建为空");
            return;
        }

        int number = num;
        while (number != 0) {
            UserBackpack userBackpack = new UserBackpack(userInfo, propsBase);
            if (!userInfo.addPropToBackpack(userBackpack)) {
                Log.warning("道具系统:添加道具到用户背包失败!");
                subject.sendMessage("系统出错，请联系主人!");
                return;
            }
            number--;
        }

        money = EconomyUtil.getMoneyByBank(sender);

        // 判断是否是姐狗
        if("FISH-30".equals(propsInfo.getCode())){
            RBloomFilter rBloomFilter = RedisUtils.initOneDayPropBloomFilter(subject.getId(), propsInfo.getCode());
            rBloomFilter.add(sender.getId());
        }
        messages.append(String.format("成功购买 %s %d%s,你还有 %s 枚 %s", propsInfo.getName(), num, propsInfo.getUnit(), money, SeasonCommonInfoManager.getSeasonMoney()));

        Log.info("道具系统:道具购买成功");

        subject.sendMessage(messages.build());

    }

    /**
     * 使用一个道具
     *
     * @param event 消息事件
     */
    @Override
    public void userProp(MessageEvent event) {
        Contact subject = event.getSubject();
        User sender = event.getSender();
        MessageChain message = event.getMessage();

        MessageChainBuilder messages = MessageUtil.quoteReply(message);

        String code = message.serializeToMiraiCode();
        String str = "";
        int i = 0;
        for (SingleMessage singleMessage : message) {
            Log.info(singleMessage.contentToString());
            if(i ==1){
                str = singleMessage.contentToString();
            }
            i++;
        }
        String[] s = str.split(" ");
        String no = s[1];

        String propCode = PropsType.getCode(no);
        Log.info("道具系统:使用道具-Code " + propCode);
        if(Objects.isNull(propCode)){
            subject.sendMessage(messages.append("请输入正确的道具名称!").build());
            return;
        }
        UserInfo userInfo = UserManager.getUserInfo(sender);
        if (propCode.startsWith("FISH-")) {
            List<PropsFishCard> propsByUserFromCode = getPropsByUserFromCode(userInfo, PropsFishCard.class);
            if (propsByUserFromCode.isEmpty()) {
                subject.sendMessage(messages.append("你的包里没有道具!").build());
                return;
            }
            Optional<PropsFishCard> optionalPropsFishCard = propsByUserFromCode.stream()
                    .filter(propsFishCard -> Objects.nonNull(propsFishCard) && propCode.equals(propsFishCard.getCode()))
                    .findFirst();

            if (optionalPropsFishCard.isPresent()) {
                PropsFishCard card = optionalPropsFishCard.get();
                if (card.getOffShelf()) {
                    messages.append(new PlainText("😣 [" + card.getName() + "]已下架"));
                    subject.sendMessage(messages.build());
                    return;
                }
                PropUtils.excute(card, userInfo, event);

                if(propCode.equals(DailyPropCode.FISH_34)){
                    SeasonManager.checkUserDailyWork(event, subject);
                }
            } else {
                subject.sendMessage(messages.append("你的包里没有这个道具!").build());
            }

        }

    }

    /**
     * 查询用户背包
     *
     * @param event 消息事件
     */
    @Override
    public void viewUserBackpack(MessageEvent event) {
        Contact subject = event.getSubject();
        User sender = event.getSender();
        Bot bot = event.getBot();

        UserInfo userInfo = UserManager.getUserInfo(sender);

        assert userInfo != null;
        List<PropsBase> propsByUser = getPropsByUser(userInfo)
                .stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (propsByUser.size() == 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你的背包空荡荡的..."));
            return;
        }
        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        if(group ==null){
            return;
        }
        ForwardMessageBuilder iNodes = new ForwardMessageBuilder(subject);
        // 背包增加@用户
        iNodes.add(bot, new PlainText("").plus(new At(sender.getId()).getDisplay(group)));
        Map<String, List<PropsBase>> propsListMap = new HashMap<>();
        List<PropsBase> propsBaseList = new ArrayList<>();

        for (PropsBase propsBase : propsByUser) {
            String code = propsBase.getCode();
            if (propsBase.isStack()) {
                if (propsListMap.containsKey(code)) {
                    propsListMap.get(code).add(propsBase);
                } else {
                    propsListMap.put(code, new ArrayList<>() {{
                        add(propsBase);
                    }});
                }
            } else {
                propsBaseList.add(propsBase);
            }
        }

//        for (Map.Entry<String, List<PropsBase>> stringListEntry : propsListMap.entrySet()) {
//            PropsBase propsInfo = PropsType.getPropsInfo(stringListEntry.getKey());
//            String no = PropsType.getNo(stringListEntry.getKey());
//            List<PropsBase> value = stringListEntry.getValue();
//            int size = value.size();
//            int open = 0;
//            if (propsInfo instanceof PropsCard) {
//                for (PropsBase propsBase : value) {
//                    PropsCard propsCard = (PropsCard) propsBase;
//                    if (propsCard.isStatus()) {
//                        open++;
//                    }
//                }
//            }
//            String format = String.format("道具编号:%s\n道具名称:%s\n道具描述:%s\n总数量:%d\n启用数量:%d", no, propsInfo.getName(), propsInfo.getDescription(), size, open);
//            iNodes.add(bot, new PlainText(format));
//        }

        Map<String, List<PropsBase>> propsBaseMap = propsBaseList.stream().collect(Collectors.groupingBy(PropsBase::getCode));
        propsBaseMap = sortMapByKey(propsBaseMap);
        for (Map.Entry<String, List<PropsBase>> entry : propsBaseMap.entrySet()) {
            String no = PropsType.getNo(entry.getKey());
            Optional<PropsBase> propsBases = entry.getValue().stream().findAny();
            if(propsBases.isPresent()){
                PropsBase p = propsBases.get();
                String format = String.format("道具%s:%s\r\n道具描述:%s\r\n道具数量:%s",
                        no, p.getName(), p.getDescription(), entry.getValue().size());
                iNodes.add(bot, new PlainText(format));
            }
        }
//        for (PropsBase propsBase : propsBaseList) {
//            String no = PropsType.getNo(propsBase.getCode());
//            String format = String.format("道具编号:%s\n道具名称:%s\n道具描述:%s", no, propsBase.getName(), propsBase.getDescription());
//            iNodes.add(bot, new PlainText(format));
//        }

        subject.sendMessage(iNodes.build());

    }

    /**
     * 出售
     * @param event
     */
    @Override
    public void sellPropFromStore(MessageEvent event) {
        Contact subject = event.getSubject();
        User sender = event.getSender();
        MessageChain message = event.getMessage();

        MessageChainBuilder messages = MessageUtil.quoteReply(message);

        String code = message.serializeToMiraiCode();

        String[] s = code.split(" ");
        String no = s[1];
        int num = 1;
        if (s.length == 3) {
            num = Integer.parseInt(s[2]);
        }
        UserInfo userInfo = UserManager.getUserInfo(sender);
        if (userInfo == null) {
            Log.warning("道具系统:获取用户为空！");
            subject.sendMessage("系统出错，请联系主人!");
            return;
        }
        String propCode = PropsType.getCode(no);
        List<UserBackpack> backpacks = Optional.ofNullable(userInfo.getBackpacks()).orElse(new ArrayList<>())
                .stream()
                .filter(back->back.getPropsCode().equals(propCode))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(backpacks)){
            messages.append("没有这个道具");
            subject.sendMessage(messages.build());
            return;
        }
        if(num > backpacks.size()){
            num = backpacks.size();
        }
        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        deleteProp(userInfo, propsInfo,num);

        int price = RandomUtil.randomInt(10, 300);
        int quantity = num * price;
       // EconomyUtil.plusMoneyToUser(sender, quantity);
        // new 赛季币
        EconomyUtil.plusMoneyToBank(sender, quantity);

       // double money = EconomyUtil.getMoneyByUser(sender);
        // new 赛季币
        double money = EconomyUtil.getMoneyByBank(sender);
        messages.append(String.format("成功出售 %s %d%s,获得 %s "+ SeasonCommonInfoManager.getSeasonMoney()+",你还有 %s 枚"+ SeasonCommonInfoManager.getSeasonMoney()+"", propsInfo.getName(), num, propsInfo.getUnit(),quantity, money));
        subject.sendMessage(messages.build());
    }

    /**
     * 兑换
     *
     * @param event
     */
    @Override
    public void exchangePropFromStore(MessageEvent event) {
        Contact subject = event.getSubject();
        User sender = event.getSender();
        MessageChain message = event.getMessage();

        MessageChainBuilder messages = MessageUtil.quoteReply(message);

        String code = message.serializeToMiraiCode();

        String[] s = code.split(" ");
        String no = s[1];
        int num = 1;
        if (s.length == 3) {
            num = Integer.parseInt(s[2]);
        }

        String propCode = PropsType.getCode(no);
        if (propCode == null) {
            Log.warning("道具系统:兑换道具为空");
            subject.sendMessage(MessageUtil.formatMessageChain(message,"\uD83D\uDE23bobo没有这个……"));
            return;
        }

        UserInfo userInfo = UserManager.getUserInfo(sender);
        if (userInfo == null) {
            Log.warning("道具系统:获取用户为空！");
            subject.sendMessage("系统出错，请联系主人!");
            return;
        }

        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        if(propsInfo instanceof PropsFishCard) {
            PropsFishCard card = (PropsFishCard) propsInfo;
            if (card.getOffShelf()) {
                messages.append(new PlainText("😣 [" + propsInfo.getName() + "]已下架"));
                subject.sendMessage(messages.build());
                return;
            }

            if (!card.getExchange()) {
                messages.append(new PlainText("😣 [" + propsInfo.getName() + "]不可兑换"));
                subject.sendMessage(messages.build());
                return;
            }
            // 是否可以用bb直接兑换赛季币 比例1:1
            if (card.getDelete()) {
                double moneyByUser = EconomyUtil.getMoneyByUser(sender);
                if (moneyByUser - num < 0) {
                    messages.append(String.format("你的币币不够%s了", num));
                    subject.sendMessage(messages.build());
                    return;
                }
                if (EconomyUtil.turnUserToBank(sender, num)) {
                    messages.append("成功兑换"+ num +  SeasonCommonInfoManager.getSeasonMoney());
                    subject.sendMessage(messages.build());
                } else {
                    messages.append("兑换失败!");
                    subject.sendMessage(messages.build());
                    Log.error("兑换管理:存款失败!");
                }
                return;
            }
        }

        // 道具背包
        List<UserBackpack> userBackpack = userInfo.getBackpacks();

        // 是否按照数量兑换
        Map<String, Long> countMap = PropExchangeDict.PROP_EXCHANGE_COUNT.get(propsInfo.getCode());
        if (MapUtils.isNotEmpty(countMap)) {
            // 1. 道具数量统计（优化分组计算）
            Map<String, Long> userPropCount = userBackpack.stream()
                    .filter(item -> item.getPropsCode() != null)
                    .collect(Collectors.groupingBy(
                            UserBackpack::getPropsCode,
                            Collectors.counting()
                    ));

            // 2. 道具充足性检查（替换AtomicBoolean）
            boolean isAllPropsSufficient = countMap.entrySet().stream()
                    .allMatch(entry ->
                            userPropCount.getOrDefault(entry.getKey(), 0L) >= entry.getValue()
                    );

            if (!isAllPropsSufficient) {
                messages.append(new PlainText("😣 请集齐道具再来兑换"));
                subject.sendMessage(messages.build());
                return;
            }

            try {
                // 3. 批量删除道具（一次处理多个道具）
                countMap.forEach((pCode, count) -> {
                    PropsBase prop = PropsType.getPropsInfo(pCode);
                    // 调用支持批量删除的方法[7]
                    for(int i =0; i < count; i++){
                        UserInfo newUserInfo = UserManager.getUserInfo(userInfo.getUser());
                        deleteProp(newUserInfo, prop);
                    }

                });

                // 4. 新增道具（添加事务处理）
                UserInfo newUserInfo = UserManager.getUserInfo(userInfo.getUser());
                UserBackpack newBackpackItem = new UserBackpack(newUserInfo, propsInfo);

                if (!newUserInfo.addPropToBackpack(newBackpackItem)) {
                    // 5. 失败补偿机制（回滚已删除道具）
                    Log.error("道具添加失败，执行回滚");
                    subject.sendMessage("系统出错，请联系管理员！");
                    return;
                }
                // 兑换成功 加入徽章信息
                String signCode = propCode.toUpperCase(Locale.ROOT);
                // 成就
                if (FishSignConstant.getSignPropCode().contains(signCode)) {
                    BadgeInfoManager.updateOrInsertBadgeInfo(subject.getId(), userInfo.getQq(), signCode, null, null);
                }
                // 6. 成功反馈
                messages.append(new PlainText(propsInfo.getName() + "兑换成功！请到背包查看"));
                subject.sendMessage(messages.build());

            } catch (Exception e) {
                Log.error("道具兑换异常: " + e.getMessage(), e);
                subject.sendMessage("系统出错，请联系管理员！");
            }
            return;
        }

        // 获取组成的道具
        List<String> propsList = PropExchangeDict.PROP_EXCHANGE.get(propsInfo.getCode());
        if (CollectionUtils.isEmpty(propsList)) {
            messages.append("兑换道具不存在!");
            subject.sendMessage(messages.build());
            return;
        }
        userBackpack = userBackpack.stream().filter(user -> propsList.contains(user.getPropsCode())).collect(Collectors.toList());
        // 如果有这些道具
        if (checkUserBackPack(userBackpack, propsList)) {
            // 删除道具
            propsList.forEach(prop -> {
                PropsBase propsEntity = PropsType.getPropsInfo(prop);
                deleteProp(userInfo, propsEntity);
            });
            UserInfo newUserInfo = UserManager.getUserInfo(userInfo.getUser());
            // 新增道具
            UserBackpack userNewBackpack = new UserBackpack(newUserInfo, propsInfo);
            if (!newUserInfo.addPropToBackpack(userNewBackpack)) {
                Log.warning("道具系统:添加道具到用户背包失败!");
                subject.sendMessage("系统出错，请联系主人!");
                return;
            }
            // 兑换成功 加入徽章信息
            String signCode = propCode.toUpperCase(Locale.ROOT);
            // 成就
            if (FishSignConstant.getSignPropCode().contains(signCode)) {
                BadgeInfoManager.updateOrInsertBadgeInfo(subject.getId(), userInfo.getQq(), signCode, null, null);
                // RedisUtils.getFishSignBloomFilter(subject.getId(), signCode).add(userInfo.getQq());
            }
            messages.append(new PlainText(propsInfo.getName() + "兑换成功！请到背包查看"));
            subject.sendMessage(messages.build());
            return;
        } else {
            messages.append(new PlainText("😣 请集齐道具再来兑换"));
            subject.sendMessage(messages.build());
            return;

        }
    }

    @Override
    public void addProp(UserInfo userInfo, PropsBase propsBase) {
        UserBackpack userBackpack = new UserBackpack(userInfo, propsBase);
        if (!userInfo.addPropToBackpack(userBackpack)) {
            Log.warning("道具系统-addProp:添加道具到用户背包失败!");
        }
    }

    private boolean checkUserBackPack(List<UserBackpack> userBackpack, List<String> list) {
        List<String> userBackpackCode = userBackpack.stream()
                .map(UserBackpack::getPropsCode)
                .collect(Collectors.toList());
        userBackpackCode.sort(Comparator.comparing(String::hashCode));
        list.sort(Comparator.comparing(String::hashCode));
        return userBackpackCode.containsAll(list);
    }

    public static Map<String, List<PropsBase>> sortMapByKey(Map<String, List<PropsBase>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        // FISH-1
        // FISH-101
        // FISH-2
        Map<String, List<PropsBase>> sortMap = new TreeMap<String, List<PropsBase>>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

}
class MapKeyComparator implements Comparator<String> {
    @Override
    public int compare(String key1, String key2) {
        // Extract the numeric part from the keys
        int num1 = extractNumber(key1);
        int num2 = extractNumber(key2);

        // Compare the numeric parts
        return Integer.compare(num1, num2);
    }

    private int extractNumber(String key) {
        // Assuming the format "FISH-<number>"
        String[] parts = key.split("-");
        return Integer.parseInt(parts[1]);
    }
}

