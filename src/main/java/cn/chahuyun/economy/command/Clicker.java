package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.message.data.*;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Clicker extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());

        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "@指定对象]"));
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
        // 消耗品，使用后可以指定下一条姐姐的狗的目标
        String key = "clicker:" + group.getId();
        RedisUtils.setKeyObject(key, target);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append("成功指定").append(new At(target).getDisplay(group))
                .append("成为下一条姐姐的狗的目标")
                .build());
    }
}
