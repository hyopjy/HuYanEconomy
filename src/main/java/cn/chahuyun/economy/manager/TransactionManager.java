package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.dto.TransactionMessageInfo;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 交易管理
 */
public class TransactionManager {

    public static Long getSenderIdByEvent(MessageEvent event){
       return event.getSender().getId();
    }

    /**
     * 获取交易信息
     * @param event
     * @return
     */
    private static TransactionMessageInfo getSendMessage(MessageEvent event){
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();
        String[] s = code.split(" ");
        if (s.length < 5) {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请按照格式输入交易信息！"));
            return null;
        }
        Long transactionUserId = null;
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                transactionUserId = at.getTarget();
            }
        }
        // 需要的道具
        String initiatePropCode = PropsType.getCode(s[1]);
        if(StringUtils.isBlank(initiatePropCode)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "需要的道具不存在！"));
            return null;
        }
        // 需要的道具数量
        Integer initiatePropCount = null;
        try{
            String initiatePropCountStr = PropsType.getCode(s[2]);
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

        String transactionCode = s[3];
        if (Constant.FISH_NAME_BB_LIST.contains(transactionCode)) {
            transactionCode = Constant.FISH_CODE_BB;
        } else if (SeasonCommonInfoManager.getSeasonMoneyNameList().contains(transactionCode)) {
            transactionCode = Constant.FISH_CODE_SEASON;
        } else {
            transactionCode = PropsType.getCode(s[4]);
            if (StringUtils.isBlank(transactionCode)) {
                subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交换的道具信息！"));
                return null;
            }
        }

        /**
         * 交易的数量
         */
        Integer transactionCount = null;
        try{
            String transactionCountStr = PropsType.getCode(s[4]);
            if(StringUtils.isBlank(transactionCountStr)){
                subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交易的数量！"));
                return null;
            }
            transactionCount = Integer.parseInt(transactionCountStr);
        }catch(NumberFormatException | NullPointerException exception  ) {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入交易的数量！"));
            return null;
        }

        TransactionMessageInfo transactionMessageInfo = TransactionMessageInfo
                .builder()
                .initiateUserId(getSenderIdByEvent(event))
                .initiatePropCode(initiatePropCode)
                .initiatePropCount(initiatePropCount)
                .transactionCode(transactionCode)
                .transactionCount(transactionCount)
                .transactionUserId(transactionUserId)
                .build();

        return transactionMessageInfo;
    }

    /**
     * 交易道具
     * @param event
     */
    public static void transactionProp(MessageEvent event) {
        // 获取交易信息
        TransactionMessageInfo transactionMessageInfo = getSendMessage(event);
        if(Objects.isNull(transactionMessageInfo)){
            return;
        }
    }
}
