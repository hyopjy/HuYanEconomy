package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.dto.TransactionMessageInfo;
import cn.chahuyun.economy.entity.Transaction;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.IdUtil;
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
                subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交换的道具信息！"));
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

    /**
     * 交易道具
     * @param event
     */
    public static void transactionProp(MessageEvent event) {
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        Group group = (Group) subject;
        User sender = event.getSender();
        // 获取交易信息
        TransactionMessageInfo transactionMessageInfo = getOpenTransactionMessage(event);
        if(Objects.isNull(transactionMessageInfo)){
            return;
        }
        // 你和目标用户有未完成的交易 请完成后

        UserInfo transactionUser = UserManager.getUserInfo(group.get(transactionMessageInfo.getTransactionUserId()));
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
            UserInfo initiateUser = UserManager.getUserInfo(group.get(transactionMessageInfo.getInitiateUserId()));
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
}
