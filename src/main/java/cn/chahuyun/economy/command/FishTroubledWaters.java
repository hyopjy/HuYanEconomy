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
import net.mamoe.mirai.message.data.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 浑水摸鱼
 * 消耗品，对指定目标使用，目标获得「浑水摸鱼」buff，之后的2次钓鱼都只会上钩[摸鱼]
 */
public class FishTroubledWaters extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
      //  this.isBuff = Boolean.TRUE;

        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
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
        Buff buff = CacheUtils.getBuff(group.getId(), target);
      //  if (isBuff && Objects.nonNull(buff)) {
        if (Objects.nonNull(buff)) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "目标用户正在使用[" + buff.getBuffName() + "]buff"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        Buff buff = new Buff();
        buff.setGroupId(group.getId());
        buff.setQq(target);
        buff.setBuffName(propsCard.getName());
        buff.setBuffType(Constant.BUFF_BACK);
        buff.setCount(2);
        List<BuffProperty> properties = new ArrayList<>(1);
        BuffProperty property1 = new BuffProperty(BuffPropsEnum.SPECIAL_FISH.getName(), "摸鱼");
        BuffProperty property2 = new BuffProperty(BuffPropsEnum.SPECIAL_LEVEL.getName(), "3");
        properties.add(property1);
        properties.add(property2);
        buff.setProperties(properties);

        CacheUtils.addBuff(group.getId(), target, buff);

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .append(new At(target).getDisplay(group)).append("之后的2次钓鱼都只会上钩[摸鱼]")
                .build());
    }
}
