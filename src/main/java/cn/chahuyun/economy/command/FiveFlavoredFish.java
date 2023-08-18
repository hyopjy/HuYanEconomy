package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;

import java.util.regex.Pattern;

/**
 * 年年有鱼
 */
public class FiveFlavoredFish extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        String match = "使用 年年有鱼( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 姐姐的狗]"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {

    }
}
