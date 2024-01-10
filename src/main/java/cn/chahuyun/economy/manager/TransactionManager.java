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
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请按照格式输入交易信息！"));
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
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请按照格式输入交易信息！"));
            return null;
        }
        if(senderId.equals(transactionUserId)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "不能和自己交易哦！"));
            return null;
        }
        // 需要的道具
        String initiatePropCode = PropsType.getCode(s1Arr[1]);
        if(StringUtils.isBlank(initiatePropCode)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "需要的道具不存在！"));
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
                subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入道具数量！"));
                return null;
            }
            initiatePropCount = Integer.parseInt(initiatePropCountStr);
        }catch(NumberFormatException | NullPointerException exception  ) {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "需要的道具数量请输入数量！"));
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
                subject.sendMessage(MessageUtil.formatMessageChain(message, "交易道具信息有误！"));
                return null;
            }
            PropsFishCard transactionPropsFishCard = getPropsFishCard(subject, message, transactionCode);
            if(Objects.isNull(transactionPropsFishCard)){
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
                subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交易的数量！"));
                return null;
            }
            transactionCount = Integer.parseInt(transactionCountStr);
        }catch(NumberFormatException | NullPointerException exception  ) {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交易的数量！"));
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

    private static PropsFishCard getPropsFishCard( Contact subject ,MessageChain message,String propCode) {
        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        if(propsInfo instanceof PropsFishCard){
            PropsFishCard card = (PropsFishCard) propsInfo;
            if(card.getOffShelf()){
                subject.sendMessage(MessageUtil.formatMessageChain(message, "道具已下架！"));
                return null;
            }
            if(!card.getTradable()){
                subject.sendMessage(MessageUtil.formatMessageChain(message, "道具不可交易！"));
                return null;
            }
            return card;
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "无法使用！"));
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
            subject.sendMessage(MessageUtil.formatMessageChain(message, "你和目标用户有未完成的交易 交易完成后在进行交易"));
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
            subject.sendMessage(MessageUtil.formatMessageChain(message, "对方没有足够的道具哦。！"));
            return;
        }

        // check 交易方余额
        if (Constant.FISH_CODE_BB.equals(transactionMessageInfo.getTransactionCode())) {
            double bbMoney = EconomyUtil.getMoneyByUser(sender);
            if(bbMoney  <  transactionMessageInfo.getTransactionCount()){
                subject.sendMessage(MessageUtil.formatMessageChain(message, "你没有足够的bb哦。！"));
                return;
            }
        } else if (Constant.FISH_CODE_SEASON.equals(transactionMessageInfo.getTransactionCode())) {
            // check 交易方赛季币
            double seasonMoney = EconomyUtil.getMoneyByBank(sender);
            if (seasonMoney <  transactionMessageInfo.getTransactionCount() ) {
                subject.sendMessage(MessageUtil.formatMessageChain(message, "你没有足够的" + SeasonCommonInfoManager.getSeasonMoney() + "哦。！"));
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
                subject.sendMessage(MessageUtil.formatMessageChain(message, "你没有足够的道具哦。！"));
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
        subject.sendMessage(MessageUtil.formatMessageChain(message, "正在交易中......"));

        tList.stream().forEach(t->{
            String transactionCode = t.getTransactionCode();
            Integer transactionCount = t.getTransactionCount();
            // 目标用户 -> 交易用户 : 交易道具
            transactionPropStep_1(transactionCode, transactionCount, transactionUserInfo, initiateUserInfo);

            String initiatePropCode = t.getInitiatePropCode();
            Integer initiatePropCount = t.getInitiatePropCount();
            // 交易用户 -> 目标用户： 交易道具、交易bb、交易雪花
            transactionPropStep_2(initiatePropCode, initiatePropCount, transactionUserInfo, initiateUserInfo);
            t.remove();
        });

        subject.sendMessage(MessageUtil.formatMessageChain(message, "交易完成"));

    }

    /**
     * 交易道具
     */
    private static void transactionPropStep_1(String transactionCode, Integer transactionCount, UserInfo transactionUserInfo, UserInfo initiateUserInfo){
        // 目标用户减少对应的道具数量
        List<UserBackpack> backpacksList = Optional.ofNullable(transactionUserInfo.getBackpacks())
                .orElse(Lists.newArrayList()).stream()
                .filter(back -> transactionCode.equals(back.getPropsCode()))
                .limit(transactionCount)
                .collect(Collectors.toList());
        // 交易用户增加
        backpacksList.forEach(back->{
            PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(transactionCode);
            UserBackpack userBackpack = new UserBackpack(initiateUserInfo, propsBase);
            if (!initiateUserInfo.addPropToBackpack(userBackpack)) {
                Log.error("交易失败:添加道具到用户背包失败!" + initiateUserInfo.getId() + "--" + transactionCode);
                return;
            }
            back.remove();
        });
    }

    private static void transactionPropStep_2(String initiatePropCode, Integer initiatePropCount, UserInfo transactionUserInfo, UserInfo initiateUserInfo){
         if(Constant.FISH_CODE_BB.equals(initiatePropCode)){
             // 交易用户减少额度
             EconomyUtil.minusMoneyToUser(initiateUserInfo.getUser(), Double.parseDouble(initiatePropCount +""));
             // 增加被交易用户的钱数
             double targetCount =  NumberUtil.round( NumberUtil.mul(Double.parseDouble(initiatePropCount +""), 0.90), 2).doubleValue();
             EconomyUtil.plusMoneyToUser(transactionUserInfo.getUser(), targetCount);
         }else if(Constant.FISH_CODE_SEASON.equals(initiatePropCode)){
             // 交易用户减少额度
             EconomyUtil.minusMoneyToBank(initiateUserInfo.getUser(), Double.parseDouble(initiatePropCount +""));
             // 增加被交易用户的钱数
             double targetCount =  NumberUtil.round( NumberUtil.mul(Double.parseDouble(initiatePropCount +""), 0.90), 2).doubleValue();
             EconomyUtil.plusMoneyToBank(transactionUserInfo.getUser(), targetCount);
         }else {
             // 交易用户减少对应的道具数量
             List<UserBackpack> backpacksList = Optional.ofNullable(initiateUserInfo.getBackpacks())
                     .orElse(Lists.newArrayList()).stream()
                     .filter(back -> initiatePropCode.equals(back.getPropsCode()))
                     .limit(initiatePropCount)
                     .collect(Collectors.toList());

             // 目标用户增加道具
            backpacksList.forEach(back->{
                PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(initiatePropCode);
                UserBackpack userBackpack = new UserBackpack(transactionUserInfo, propsBase);
                if (!transactionUserInfo.addPropToBackpack(userBackpack)) {
                    Log.error("交易失败:添加道具到用户背包失败!" + transactionUserInfo.getId() + "--" + initiatePropCode);
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
}
