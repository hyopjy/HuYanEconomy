package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
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

    @Override
    public abstract boolean checkOrder();


    @Override
    public abstract void excute();

}
