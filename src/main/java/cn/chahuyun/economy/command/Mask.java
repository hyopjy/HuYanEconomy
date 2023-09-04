package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.chahuyun.economy.utils.RandomHelperUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Pattern;

/**
 * 面罩
 */
public class Mask extends AbstractPropUsage {

    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "@指定对象]"));
            return false;
        }
        // 校验使用次数是否超过限制
        if (CacheUtils.checkMaskCountKey(group.getId(), userInfo.getQq())) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "[面罩] 今日使用已达到限制"));
            return false;
        }

        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                this.target = at.getTarget();
            }
        }
        return true;
    }

    @Override
    public void excute() {
        User sender = event.getSender();
        //被bobo正义执行，抢劫失败并且罚款2000币币
        if (RandomHelperUtil.checkRandomLuck1_20()) {
            EconomyUtil.minusMoneyToUser(sender, 2000);
            subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                    .append("触发币币回收计划之：被bobo正义执行，抢劫失败并且罚款2000币币").append("\r\n")
                    .build());
        } else {
            int money = RandomUtil.randomInt(501, 1500);
            // 自己获得
            EconomyUtil.plusMoneyToUser(sender, money);
            // 减去目标用户
            NormalMember member = group.get(target);
            EconomyUtil.minusMoneyToUser(member, money);

            CacheUtils.addUserMaskCountKey(group.getId(), sender.getId());
            subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                    .append(propsCard.getName() + "使用成功").append("\r\n")
                    .append("成功获得").append(new At(target).getDisplay(group))
                    .append("的" + money + "币币")
                    .build());
        }


    }
}
