package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Pattern;

/**
 * 薛定谔的鱼
 */
public class SchDingerFish extends AbstractPropUsage {
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
        CacheUtils.addSchDingerFishKey(group.getId(), target);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName()).append("使用成功").append("\r\n")
                .append(new At(target).getDisplay(group)).append("获得「薛定谔」buff，发送的所有消息都会被撤回").append("\r\n")
                .build());
    }
}
