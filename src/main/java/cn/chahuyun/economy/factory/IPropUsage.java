package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 抽象产品
 */
public interface IPropUsage {

    boolean checkOrder();
    void excute();
}
