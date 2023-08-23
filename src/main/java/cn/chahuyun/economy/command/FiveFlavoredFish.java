package cn.chahuyun.economy.command;

import cn.chahuyun.economy.constant.BuffPropsEnum;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.dto.Buff;
import cn.chahuyun.economy.dto.BuffProperty;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 年年有鱼 消耗品，使用后获得「年年有鱼」buff，之后的5次钓鱼都会额外增加difficultymin50，rankmin5
 */
public class FiveFlavoredFish extends AbstractPropUsage {

    @Override
    public boolean checkOrder() {
        this.isBuff = Boolean.TRUE;

        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        User sender = event.getSender();
        // 消耗品，使用后获得「年年有鱼」buff，之后的5次钓鱼都会额外增加difficultymin50，rankmin5
        Buff buff = new Buff();
        buff.setGroupId(group.getId());
        buff.setQq(sender.getId());
        buff.setBuffName(propsCard.getName());
        buff.setBuffType(Constant.BUFF_FRONT);
        buff.setCount(5);
        List<BuffProperty> properties = new ArrayList<>(2);
        BuffProperty property1 = new BuffProperty(BuffPropsEnum.DIFFICULTY_MIN.getName(), 50);
        BuffProperty property2 = new BuffProperty(BuffPropsEnum.RANK_MIN.getName(), 5);
        properties.add(property1);
        properties.add(property2);
        buff.setProperties(properties);

        CacheUtils.addBuff(group.getId(), sender.getId(), buff);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append("之后5次钓鱼都会difficultymin增加50，rankmin增加5")
                .build());
    }

}
