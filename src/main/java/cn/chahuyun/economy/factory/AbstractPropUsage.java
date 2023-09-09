package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.constant.PropConstant;
import cn.chahuyun.economy.dto.Buff;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Objects;

@Getter
@Setter
public abstract class AbstractPropUsage implements IPropUsage{
    protected PropsFishCard propsCard;

    protected UserInfo userInfo;

    protected MessageEvent event;

    protected Group group;

    protected Contact subject;

    protected Long target;

    protected Boolean isBuff = false;
    @Override
    public abstract boolean checkOrder();


    @Override
    public abstract void excute();

    @Override
    public boolean checkBuff(){
        // 不是buff不限制
        if(!isBuff){
            return true;
        }
        // 校验是否正在使用BUff -- 执行结束后删除
        // 判断命令执行者是否在使用其他buff
        User sender = event.getSender();
        if (PropConstant.AUTOMATIC_FISH.equals(propsCard.getName())) {
            if (CacheUtils.checkAutomaticFishBuff(group.getId(), sender.getId())) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在使用[" + propsCard.getName() + "]buff"));
                return false;
            }
        } else {
            Buff buff = CacheUtils.getBuff(group.getId(), sender.getId());
            if (isBuff && Objects.nonNull(buff)) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在使用[" + buff.getBuffName() + "]buff"));
                return false;
            }
        }
        return true;
    }

}
