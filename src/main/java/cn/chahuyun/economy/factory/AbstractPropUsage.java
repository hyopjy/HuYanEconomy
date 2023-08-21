package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;

@Getter
@Setter
public abstract class AbstractPropUsage implements IPropUsage{
    protected PropsFishCard propsCard;

    protected UserInfo userInfo;

    protected MessageEvent event;

    protected Group group;

    protected Contact subject;

    protected Long target;

    protected Boolean isBuff;
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
        String buffCacheValue = CacheUtils.getBuffCacheValue(group.getId(), subject.getId());
        if (isBuff && !StrUtil.isBlank(buffCacheValue)) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在使用[" + buffCacheValue + "]buff"));
            return false;
        }
        return true;
    }

}
