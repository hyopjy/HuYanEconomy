package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.hutool.core.lang.func.Func;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;

@Getter
@Setter
public class Glassbead extends AbstractPropUsage{

    public Glassbead() {
    }

    public Glassbead(PropsFishCard propsCard, UserInfo userInfo,MessageEvent event) {
        this.event = event;
        this.propsCard = propsCard;
        this.userInfo = userInfo;
        this.group = getGroup();
    }

    @Override
    public boolean checkOrder() {
        System.out.println("order--check");
        return false;
    }

    @Override
    public void excute() {

        System.out.println("excute");
    }


}
