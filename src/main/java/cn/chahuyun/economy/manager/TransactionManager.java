package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.dto.TransactionMessageInfo;
import cn.chahuyun.economy.dto.TransactionUserInfo;
import cn.chahuyun.economy.entity.Transaction;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 交易管理
 */
public class TransactionManager {

    // 等待交易
    private static final Integer TRANSACTION_WAIT = 0 ;

    // 完成交易
    private static final Integer TRANSACTION_SUCCESS = 1 ;



    public static Long getSenderIdByEvent(MessageEvent event){
       return event.getSender().getId();
    }

    /**
     * 获取交易信息
     * @param event
     * @return
     */
    private static TransactionMessageInfo getOpenTransactionMessage(MessageEvent event){
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();
        Long senderId = getSenderIdByEvent(event);
        String[] s = code.split(" ");
        if (s.length < 5) {
//            subject.sendMessage(MessageUtil.formatMessageChain(message, "交易 xx道具 数量 币币/bb/雪币/道具 数量 @B"));
            Log.info("[道具交易-格式不正确]: " + senderId + " {" + code + "}");
            return null;
        }

        String s1 = "";
        Long transactionUserId = null;
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                transactionUserId = at.getTarget();
            }
            if(singleMessage instanceof PlainText){
                PlainText text = (PlainText)singleMessage;
                if(StringUtils.isBlank(text.serializeToMiraiCode())){
                    continue;
                }
                s1 = text.serializeToMiraiCode();
            }
        }
        String[] s1Arr = s1.split(" ");
        if (s1Arr.length != 5 || Objects.isNull(transactionUserId)) {
//            subject.sendMessage(MessageUtil.formatMessageChain(message, "请按照格式输入交易信息！"));
            Log.info("[道具交易-不正确]: " + senderId + " {" + code + "}");
            return null;
        }
        if(senderId.equals(transactionUserId)){
        //    subject.sendMessage(MessageUtil.formatMessageChain(message, "不能和自己交易哦！"));
            Log.info("[道具交易-和自己交易]: " + senderId);
            return null;
        }
        // 需要的道具
        String initiatePropCode = PropsType.getCode(s1Arr[1]);
        if(StringUtils.isBlank(initiatePropCode)){
//            subject.sendMessage(MessageUtil.formatMessageChain(message, "需要的道具不存在！"));
            Log.info("[道具交易-需要的道具不存在]: " + senderId + "-" + initiatePropCode);
            return null;
        }
        PropsFishCard propsFishCard = getPropsFishCard(subject, message, initiatePropCode);
        if(Objects.isNull(propsFishCard)){
            return null;
        }
        // 需要的道具数量
        Integer initiatePropCount;
        try{
            String initiatePropCountStr = s1Arr[2];
            if(StringUtils.isBlank(initiatePropCountStr)){
            //    subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入道具数量！"));
                Log.info("[道具交易-道具数量为空]: " + senderId);
                return null;
            }
            initiatePropCount = Integer.parseInt(initiatePropCountStr);
        }catch(NumberFormatException | NullPointerException exception  ) {
//            subject.sendMessage(MessageUtil.formatMessageChain(message, "需要的道具数量请输入数量！"));
            Log.info("[道具交易-道具数量为空]: " + senderId);
            return null;
        }

        // 交换的道具/**
        //         * 交易的编码
        //         * - FISH_CODE_BB
        //         * - FISH_CODE_SEASON
        //         * - FISH_xxx
        //         */

        String transactionCode = s1Arr[3];
        if (Constant.FISH_NAME_BB_LIST.contains(transactionCode)) {
            transactionCode = Constant.FISH_CODE_BB;
        } else if (SeasonCommonInfoManager.getSeasonMoneyNameList().contains(transactionCode)) {
            transactionCode = Constant.FISH_CODE_SEASON;
        } else {
            transactionCode = PropsType.getCode(transactionCode);
            if (StringUtils.isBlank(transactionCode)) {
               // subject.sendMessage(MessageUtil.formatMessageChain(message, "交易道具信息有误！"));
                Log.info("[道具交易-交易道具信息]: " + senderId + "-" + transactionCode);
                return null;
            }
            PropsFishCard transactionPropsFishCard = getPropsFishCard(subject, message, transactionCode);
            if(Objects.isNull(transactionPropsFishCard)){
                Log.info("[道具交易-交易道具信息有为空]: " + senderId + "-" + transactionCode);
                return null;
            }
        }

        /**
         * 交易的数量
         */
        Integer transactionCount;
        try{
            String transactionCountStr = s1Arr[4];
            if(StringUtils.isBlank(transactionCountStr)){
               // subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交易的数量！"));
                Log.info("[道具交易]: " + senderId + " 请输入交易的数量！");
                return null;
            }
            transactionCount = Integer.parseInt(transactionCountStr);
        }catch(NumberFormatException | NullPointerException exception  ) {
           // subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交易的数量！"));
            Log.info("[道具交易]: " + senderId + " 请输入交易的数量！");
            return null;
        }

        return TransactionMessageInfo
                .builder()
                .initiateUserId(senderId)
                .initiatePropCode(initiatePropCode)
                .initiatePropCount(initiatePropCount)
                .transactionCode(transactionCode)
                .transactionCount(transactionCount)
                .transactionUserId(transactionUserId)
                .build();
    }

    private static PropsFishCard getPropsFishCard(Contact subject ,MessageChain message,String propCode) {
        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        if(propsInfo instanceof PropsFishCard){
            PropsFishCard card = (PropsFishCard) propsInfo;
            if(card.getOffShelf()){
               // subject.sendMessage(MessageUtil.formatMessageChain(message, "道具已下架！"));
                Log.info("[道具交易]: " + propCode + " 道具已下架！");
                return null;
            }
            if(!card.getTradable()){
               //  subject.sendMessage(MessageUtil.formatMessageChain(message, "道具不可交易！"));
                Log.info("[道具交易]: " + propCode + " 道具不可交易！");
                return null;
            }
            return card;
        }else {
            // subject.sendMessage(MessageUtil.formatMessageChain(message, "无法使用！"));
            Log.info("[道具交易]: " + propCode + "无法使用");
        }
        return null;
    }

    /**
     * 交易道具
     * @param event
     */
    public static void transactionProp(MessageEvent event) {
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        Group group = null;
        if(subject instanceof Group){
            group = (Group) subject;
        }
        if(Objects.isNull(group)){
            return;
        }
        User sender = event.getSender();

        // 获取交易信息
        TransactionMessageInfo transactionMessageInfo = getOpenTransactionMessage(event);
        if(Objects.isNull(transactionMessageInfo)){
            return;
        }
        // 你和目标用户有未完成的交易 请完成后
        List<Transaction> tList = getTransactionByUserType(transactionMessageInfo.getInitiateUserId(),
                transactionMessageInfo.getTransactionUserId(),TRANSACTION_WAIT);
        if(CollectionUtils.isNotEmpty(tList)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "你和目标用户有未完成的交易 交易完成后再进行交易"));
            return;
        }
        User transactionUserGroup = group.get(transactionMessageInfo.getTransactionUserId());
        if(Objects.isNull(transactionUserGroup)){
            return;
        }
        UserInfo transactionUser = UserManager.getUserInfo(transactionUserGroup);
        if(Objects.isNull(transactionUser)){
            return;
        }
        // check 目标有没有道具
        List<UserBackpack> list = Optional.ofNullable(transactionUser.getBackpacks()).orElse(Lists.newArrayList()).stream()
                .filter(back -> transactionMessageInfo.getInitiatePropCode().equals(back.getPropsCode()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(list) || list.size() < transactionMessageInfo.getInitiatePropCount()){
            //subject.sendMessage(MessageUtil.formatMessageChain(message, "对方没有足够的道具哦。！"));
            Log.info("[道具交易-交易目标没有足够道具]" + transactionMessageInfo.getInitiateUserId() + " to " + transactionMessageInfo.getTransactionUserId() + "-" + transactionMessageInfo.getInitiatePropCode());
            return;
        }

        // check 交易方余额
        if (Constant.FISH_CODE_BB.equals(transactionMessageInfo.getTransactionCode())) {
            double bbMoney = EconomyUtil.getMoneyByUser(sender);
            if(bbMoney  <  transactionMessageInfo.getTransactionCount()){
                // subject.sendMessage(MessageUtil.formatMessageChain(message, "你没有足够的bb哦。！"));
                Log.info("[道具交易-交易方没有足够的bb]" + transactionMessageInfo.getInitiateUserId() + " to " + transactionMessageInfo.getTransactionUserId() + "-" + transactionMessageInfo.getTransactionCode());
                return;
            }
        } else if (Constant.FISH_CODE_SEASON.equals(transactionMessageInfo.getTransactionCode())) {
            // check 交易方赛季币
            double seasonMoney = EconomyUtil.getMoneyByBank(sender);
            if (seasonMoney <  transactionMessageInfo.getTransactionCount() ) {
                // subject.sendMessage(MessageUtil.formatMessageChain(message, "你没有足够的" + SeasonCommonInfoManager.getSeasonMoney() + "哦。！"));
                Log.info("[道具交易-交易方没有足够的赛季币]" + transactionMessageInfo.getInitiateUserId() + " to " + transactionMessageInfo.getTransactionUserId() + "-" + transactionMessageInfo.getTransactionCode());
                return;
            }
        } else {
            User initiateUserGroup = group.get(transactionMessageInfo.getInitiateUserId());
            if(Objects.isNull(initiateUserGroup)){
                return;
            }
            UserInfo initiateUser = UserManager.getUserInfo(initiateUserGroup);
            if(Objects.isNull(initiateUser)){
                return;
            }
            // check 交易方道具
            List<UserBackpack> ownList = Optional.ofNullable(initiateUser.getBackpacks()).orElse(Lists.newArrayList()).stream()
                    .filter(back -> transactionMessageInfo.getTransactionCode().equals(back.getPropsCode()))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(ownList) || ownList.size() < transactionMessageInfo.getTransactionCount()){
               // subject.sendMessage(MessageUtil.formatMessageChain(message, "你没有足够的道具哦。！"));
                Log.info("[道具交易-交易方没有足够的道具]" + transactionMessageInfo.getInitiateUserId() + " to " + transactionMessageInfo.getTransactionUserId() + "-" + transactionMessageInfo.getTransactionCode());
                return;
            }
        }
        Transaction transaction = new Transaction(IdUtil.getSnowflakeNextId(),
                transactionMessageInfo.getInitiateUserId(),
                transactionMessageInfo.getInitiatePropCode(),
                transactionMessageInfo.getInitiatePropCount(),
                transactionMessageInfo.getTransactionCode(),
                transactionMessageInfo.getTransactionCount(),
                transactionMessageInfo.getTransactionUserId(),
                TRANSACTION_WAIT
        );
        transaction.save();
    }


    public static List<Transaction> getTransactionByUserType(Long initiateUserId, Long transactionUserId, Integer transactionStatus){
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Transaction> query = builder.createQuery(Transaction.class);
            JpaRoot<Transaction> from = query.from(Transaction.class);
            query.select(from);
            query.where(
                    builder.equal(from.get("initiateUserId"), initiateUserId),
                    builder.equal(from.get("transactionUserId"), transactionUserId),
                    builder.equal(from.get("transactionStatus"), transactionStatus)
            );
            return session.createQuery(query).list();
        });
    }

    /**
     * 拒绝交易删除交易信息
     * @param event
     */
    public static void refuseTransaction(MessageEvent event) {
        TransactionUserInfo userInfo = getTransactionUserInfo(event, "refuse");
        if(Objects.isNull(userInfo)){
            return;
        }
        deleteTransactionInfo(event, userInfo);
    }

    private static void deleteTransactionInfo(MessageEvent event, TransactionUserInfo userInfo) {
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        List<Transaction> tList = getTransactionByUserType(userInfo.getInitiateUserId(),
                userInfo.getTransactionUserId(),TRANSACTION_WAIT);
        if(CollectionUtils.isEmpty(tList)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "和目标用户没有交易信息"));
            return;
        }
        tList.forEach(Transaction::remove);
        subject.sendMessage(MessageUtil.formatMessageChain(message, "取消交易成功"));
    }

    /**
     * 拒绝交易、同意交易
     *
     * @param event
     * @return
     */
    private static TransactionUserInfo getTransactionUserInfo(MessageEvent event, String type) {
        MessageChain message = event.getMessage();
        Long senderId = getSenderIdByEvent(event);
        Long targetUserId = null;
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                targetUserId = at.getTarget();
            }
        }
        if (Objects.isNull(targetUserId)) {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(message, "@用户不存在！"));
            return null;
        }
        if ("cancel".equals(type)) {
            return TransactionUserInfo.builder()
                    .initiateUserId(senderId)
                    .transactionUserId(targetUserId)
                    .build();
        } else if ("refuse".equals(type) || "confirm".equals(type)) {
            return TransactionUserInfo.builder()
                    .initiateUserId(targetUserId)
                    .transactionUserId(senderId)
                    .build();
        } else {
            return null;
        }
    }


    /**
     * 取消交易
     *
     * @param event
     */
    public static void cancelTransaction(MessageEvent event) {
        TransactionUserInfo userInfo = getTransactionUserInfo(event, "cancel");
        if(Objects.isNull(userInfo)){
            return;
        }
        deleteTransactionInfo(event,userInfo);
    }

    /**
     * 确认交易
     *
     * @param event
     */
    public static void confirmTransaction(MessageEvent event) {
        TransactionUserInfo userInfo = getTransactionUserInfo(event, "confirm");
        if(Objects.isNull(userInfo)){
            return;
        }
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();

        // 目标用户
        UserInfo transactionUserInfo = getTransactionUserInfo(userInfo.getTransactionUserId(), subject);
        if(Objects.isNull(transactionUserInfo)){
            return;
        }
        // 交易用户
        UserInfo initiateUserInfo = getTransactionUserInfo(userInfo.getInitiateUserId(), subject);
        if(Objects.isNull(initiateUserInfo)){
            return;
        }

        List<Transaction> tList = getTransactionByUserType(userInfo.getInitiateUserId(), userInfo.getTransactionUserId(),TRANSACTION_WAIT);
        //
       // subject.sendMessage(MessageUtil.formatMessageChain(message, "正在交易中......"));
        AtomicReference<Boolean> check = new AtomicReference<>(false);
        tList.stream().forEach(t->{
            String initiatePropCode = t.getInitiatePropCode();
            Integer initiatePropCount = t.getInitiatePropCount();
            String transactionPropMessage = checkTransactionPropStep(initiatePropCode, initiatePropCount, transactionUserInfo);
            if(StringUtils.isNotBlank(transactionPropMessage)){
                subject.sendMessage(MessageUtil.formatMessageChain(message, transactionPropMessage));
                return;
            }

            String transactionCode = t.getTransactionCode();
            Integer transactionCount = t.getTransactionCount();
            String initiatePropMessage = checkInitiatePropStep(transactionCode, transactionCount, initiateUserInfo);
            if(StringUtils.isNotBlank(initiatePropMessage)){
                subject.sendMessage(MessageUtil.formatMessageChain(message, initiatePropMessage));
                return;
            }

            // 目标用户 -> 交易用户 : 交易道具
            transactionPropStep_1(initiatePropCode, initiatePropCount, transactionUserInfo, initiateUserInfo);

            // 交易用户 -> 目标用户： 交易道具、交易bb、交易雪花
            transactionPropStep_2(transactionCode, transactionCount, transactionUserInfo, initiateUserInfo);
            t.remove();
            check.set(true);
        });
        if(check.get()){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "交易完成"));
        }

    }

    private static String checkInitiatePropStep(String propCode, Integer propCount, UserInfo initiateUserInfo) {
        if(Constant.FISH_CODE_BB.equals(propCode)){
            // 交易用户金钱
            if (Double.parseDouble(propCount + "") > EconomyUtil.getMoneyByUser(initiateUserInfo.getUser())) {
                return "交易发起人币币不足 无法交易";
            }
        }else if(Constant.FISH_CODE_SEASON.equals(propCode)){
            // 交易用户减少额度
            if (Double.parseDouble(propCount + "") > EconomyUtil.getMoneyByBank(initiateUserInfo.getUser())) {
                return "交易发起人"+SeasonCommonInfoManager.getSeasonMoney() + "不足 无法交易";
            }
        }else {
            // 交易用户减少对应的道具数量
            List<UserBackpack> backpacksList = Optional.ofNullable(initiateUserInfo.getBackpacks())
                    .orElse(Lists.newArrayList()).stream()
                    .filter(back -> propCode.equals(back.getPropsCode()))
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(backpacksList) || backpacksList.size() < propCount){
                return "交易发起人道具数量不足";
            }
        }
        return null;
    }

    private static String checkTransactionPropStep(String transactionCode, Integer transactionCount, UserInfo transactionUserInfo) {
        // 目标用户减少对应的道具数量
        List<UserBackpack> backpacksList = Optional.ofNullable(transactionUserInfo.getBackpacks())
                .orElse(Lists.newArrayList()).stream()
                .filter(back -> transactionCode.equals(back.getPropsCode()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(backpacksList) || backpacksList.size() < transactionCount){
            return "您道具数量不足,无法完成交易";
        }
        return null;

    }

    /**
     * 交易道具
     */
    private static void transactionPropStep_1(String code, Integer count, UserInfo transactionUserInfo, UserInfo initiateUserInfo){
        // 目标用户减少对应的道具数量
        List<UserBackpack> backpacksList = Optional.ofNullable(transactionUserInfo.getBackpacks())
                .orElse(Lists.newArrayList()).stream()
                .filter(back -> code.equals(back.getPropsCode()))
                .limit(count)
                .collect(Collectors.toList());
        // 交易用户增加
        backpacksList.forEach(back->{
            PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(code);
            UserBackpack userBackpack = new UserBackpack(initiateUserInfo, propsBase);
            if (!initiateUserInfo.addPropToBackpack(userBackpack)) {
                Log.error("交易失败:添加道具到用户背包失败!" + initiateUserInfo.getId() + "--" + code);
                return;
            }
            back.remove();
        });
    }

    private static void transactionPropStep_2(String propCode, Integer propCount, UserInfo transactionUserInfo, UserInfo initiateUserInfo){
         if(Constant.FISH_CODE_BB.equals(propCode)){
             // 交易用户减少额度
             EconomyUtil.minusMoneyToUser(initiateUserInfo.getUser(), Double.parseDouble(propCount +""));
             // 增加被交易用户的钱数
             double targetCount =  NumberUtil.round( NumberUtil.mul(Double.parseDouble(propCount +""), 0.90), 2).doubleValue();
             EconomyUtil.plusMoneyToUser(transactionUserInfo.getUser(), targetCount);
         }else if(Constant.FISH_CODE_SEASON.equals(propCode)){
             // 交易用户减少额度
             EconomyUtil.minusMoneyToBank(initiateUserInfo.getUser(), Double.parseDouble(propCount +""));
             // 增加被交易用户的钱数
             double targetCount =  NumberUtil.round( NumberUtil.mul(Double.parseDouble(propCount +""), 0.90), 2).doubleValue();
             EconomyUtil.plusMoneyToBank(transactionUserInfo.getUser(), targetCount);
         }else {
             // 交易用户减少对应的道具数量
             List<UserBackpack> backpacksList = Optional.ofNullable(initiateUserInfo.getBackpacks())
                     .orElse(Lists.newArrayList()).stream()
                     .filter(back -> propCode.equals(back.getPropsCode()))
                     .limit(propCount)
                     .collect(Collectors.toList());

             // 目标用户增加道具
            backpacksList.forEach(back->{
                PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(propCode);
                UserBackpack userBackpack = new UserBackpack(transactionUserInfo, propsBase);
                if (!transactionUserInfo.addPropToBackpack(userBackpack)) {
                    Log.error("交易失败:添加道具到用户背包失败!" + transactionUserInfo.getId() + "--" + propCode);
                    return;
                }
                back.remove();
            });
         }
    }



    private static UserInfo getTransactionUserInfo(Long userId, Contact subject){
       Group group = null;
       if(subject instanceof Group){
           group = (Group) subject;
       }
       if(Objects.isNull(group)){
           return null;
       }

       User groupUser = group.get(userId);
       if(Objects.isNull(groupUser)){
           return null;
       }
       UserInfo user = UserManager.getUserInfo(groupUser);
       if(Objects.isNull(user)){
           return null;
       }
       return user;
   }

    /**
     * 查看交易列表
     *
     * @param event
     */
    public static void listTransaction(MessageEvent event) {
        Contact subject = event.getSubject();
        Group group = null;
        if(subject instanceof Group){
            group = (Group) subject;
        }
        if(Objects.isNull(group)){
            return;
        }
        MessageChain message = event.getMessage();
        Long senderId = getSenderIdByEvent(event);
        StringBuilder messageFormat = new StringBuilder();
        // 我发起的交易
        List<Transaction> initiateUserList = queryInitiateUserTransaction(senderId);
        if(CollectionUtils.isNotEmpty(initiateUserList)){
            messageFormat.append("---我发起的交易---").append("\r\n");
            Group finalGroup = group;
            initiateUserList.forEach(initiate->{
                String messageInfo = changeMessage(initiate, finalGroup, "Initiate");
                messageFormat.append(messageInfo).append("\r\n");
                messageFormat.append("--------").append("\r\n");
            });
        }
        // 待我审核的交易
        List<Transaction> transactionUserList = queryTransactionUserTransaction(senderId);
        if(CollectionUtils.isNotEmpty(transactionUserList)){
            messageFormat.append("---待完成的交易---").append("\r\n");
            Group finalGroup = group;
            transactionUserList.forEach(transaction->{
                String messageInfo = changeMessage(transaction, finalGroup, "Transaction");
                messageFormat.append(messageInfo).append("\r\n");
                messageFormat.append("--------").append("\r\n");
            });
        }
        if(StringUtils.isBlank(messageFormat.toString())){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "暂无交易信息"));
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(message, messageFormat.toString()));
        }
    }

    private static String changeMessage(Transaction transaction, Group group, String type) {
        StringBuilder sb = new StringBuilder();
        if("Initiate".equals(type)){
            PropsBase initiatePropsInfo = PropsType.getPropsInfo(transaction.getInitiatePropCode());
            PropsFishCard card = (PropsFishCard) initiatePropsInfo;
            sb.append("道具：").append(card.getName()).append("x").append(transaction.getInitiatePropCount()).append("\r\n");
            String transactionPropName = "";
            if(Constant.FISH_CODE_BB.equals(transaction.getTransactionCode())){
                transactionPropName = Constant.FISH_NAME_BB_LIST.get(0);
            }else if(Constant.FISH_CODE_SEASON.equals(transaction.getTransactionCode())){
                transactionPropName = SeasonCommonInfoManager.getSeasonMoneyNameList().get(0);
            }else {
                PropsBase transactionPropsInfo = PropsType.getPropsInfo(transaction.getTransactionCode());
                PropsFishCard cardTransaction = (PropsFishCard) transactionPropsInfo;
                transactionPropName = cardTransaction.getName();
            }
            sb.append("交换人：").append(new At(transaction.getTransactionUserId()).getDisplay(group)).append("\r\n");;
            sb.append("交换物：").append(transactionPropName).append("x").append(transaction.getTransactionCount()).append("\r\n");
        }

        if("Transaction".equals(type)){
            PropsBase initiatePropsInfo = PropsType.getPropsInfo(transaction.getTransactionCode());
            PropsFishCard card = (PropsFishCard) initiatePropsInfo;
            sb.append("道具：").append(card.getName()).append("x").append(transaction.getTransactionCount()).append("\r\n");

            String initiatePropName = "";
            if(Constant.FISH_CODE_BB.equals(transaction.getInitiatePropCode())){
                initiatePropName = Constant.FISH_NAME_BB_LIST.get(0);
            }else if(Constant.FISH_CODE_SEASON.equals(transaction.getInitiatePropCode())){
                initiatePropName = SeasonCommonInfoManager.getSeasonMoneyNameList().get(0);
            }else {
                PropsBase initiatePropCodePropsInfo = PropsType.getPropsInfo(transaction.getInitiatePropCode());
                PropsFishCard cardTransaction = (PropsFishCard) initiatePropCodePropsInfo;
                initiatePropName = cardTransaction.getName();
            }
            sb.append("发起人：").append(new At(transaction.getInitiateUserId()).getDisplay(group)).append("\r\n");;
            sb.append("交换物：").append(initiatePropName).append("x").append(transaction.getInitiatePropCount()).append("\r\n");
        }

        return sb.toString();
    }

    private static List<Transaction> queryInitiateUserTransaction(Long senderId){
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Transaction> query = builder.createQuery(Transaction.class);
            JpaRoot<Transaction> from = query.from(Transaction.class);
            query.select(from);
            query.where(builder.equal(from.get("initiateUserId"), senderId),
                    builder.equal(from.get("transactionStatus"), TRANSACTION_WAIT)
            );
            return session.createQuery(query).list();
        });
    }

    private static List<Transaction> queryTransactionUserTransaction(Long senderId){
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Transaction> query = builder.createQuery(Transaction.class);
            JpaRoot<Transaction> from = query.from(Transaction.class);
            query.select(from);
            query.where(builder.equal(from.get("transactionUserId"), senderId), builder.equal(from.get(
                    "transactionStatus"), TRANSACTION_WAIT)
            );
            return session.createQuery(query).list();
        });
    }
}
