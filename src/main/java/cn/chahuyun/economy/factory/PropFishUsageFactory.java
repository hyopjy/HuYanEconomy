package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import net.mamoe.mirai.event.events.MessageEvent;

public class PropFishUsageFactory implements PropUsageFactory{
    /**
     * 创建玻璃球
     *
     * @return
     */
    @Override
    public IPropUsage createGlassBead(PropsFishCard propsCard, UserInfo userInfo,MessageEvent event) {
        return new Glassbead(propsCard, userInfo, event);
    }
}
