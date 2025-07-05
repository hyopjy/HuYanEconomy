package cn.chahuyun.economy.manager;

import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupAdminManager {
    public static void giveCup(GroupMessageEvent event) {
        Contact subject = event.getSubject();
        MessageChain chain = event.getMessage();
        long senderId = event.getSender().getId();

        // 检查权限和命令前缀
        if (!EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(senderId) ||
                !chain.contentToString().startsWith("颁发")) {
            return;
        }

        // 解析消息链
        String propIdentifier = null;
        int amount = 1; // 默认数量为1
        List<Long> targets = new ArrayList<>();
        boolean foundAmount = false;

        for (SingleMessage msg : chain) {
            if (msg instanceof PlainText) {
                String text = ((PlainText) msg).getContent().trim();

                if (text.startsWith("颁发")) {
                    // 处理"颁发"后的内容
                    String afterAward = text.substring(2).trim();
                    if (!afterAward.isEmpty()) {
                        // 尝试分割道具标识和可能的数量
                        String[] parts = afterAward.split("\\s+", 2);
                        propIdentifier = parts[0];

                        // 检查是否有数量部分
                        if (parts.length > 1) {
                            String amountPart = parts[1];
                            Matcher matcher = Pattern.compile("^(\\d+)\\s*").matcher(amountPart);
                            if (matcher.find()) {
                                try {
                                    amount = Integer.parseInt(matcher.group(1));
                                    foundAmount = true;
                                } catch (NumberFormatException ignored) {
                                    // 不是有效数字，保持默认值
                                }
                            }
                        }
                    }
                }
            }
            else if (msg instanceof At) {
                targets.add(((At) msg).getTarget());
            }
        }

        // 验证解析结果
        if (propIdentifier == null || propIdentifier.isEmpty()) {
            event.getGroup().sendMessage("道具名称不能为空！格式：颁发 道具名称 [数量] @用户");
            return;
        }

        if (targets.isEmpty()) {
            event.getGroup().sendMessage("请指定要颁发道具的用户！格式：颁发 道具名称 [数量] @用户");
            return;
        }

        for(int i=0; i< targets.size(); i++){
            Long userId = targets.get(i);
            for(int j =0; j < amount; j++ ){
                giveProp(PropsType.getCode(propIdentifier), event.getGroup().get(userId));
            }
        }
        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "颁发成功"));
    }

    public static String giveCupProp(User user){
        return giveProp("FISH-100", user);
    }

    public static String giveTheChosenOneProp( User user){
        return giveProp("FISH-118", user);
    }

    public static String giveAllIn(User user){
        return giveProp("FISH-113", user);
    }

    public static String giveProp(String propCode, User user){
        if(Objects.isNull(user)){
            Log.info("给道具失败 用户为空");
            return " 用户不存在";
        }
        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        if(Objects.isNull(propsInfo)){
            return "   道具不存在";
        }
        UserInfo newUserInfo = UserManager.getUserInfo(user);
        UserBackpack newBackpackItem = new UserBackpack(newUserInfo, propsInfo);
        newUserInfo.addPropToBackpack(newBackpackItem);
        return propsInfo.getName();
    }
}


