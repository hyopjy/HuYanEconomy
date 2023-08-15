package cn.chahuyun.economy.command;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.manager.PropsManager;
import cn.chahuyun.economy.utils.CacheUtils;
import net.mamoe.mirai.event.events.MessageEvent;

public abstract class PropUsage {
    protected MessageEvent event;

    protected UserInfo userInfo;
    protected PropsFishCard propsCard;

    public static PropUsage getPropUsage(PropsFishCard propsCard, UserInfo userInfo,MessageEvent event){
        if("玻璃珠".equals(propsCard.getName())){
            return new GlassbeadPropUsage(propsCard,userInfo,event);
        }
        return null;
    }
    public abstract void execute(int num);

    public abstract Boolean checkOrder(String order);

    public static void doRemove(Long qq){
        CacheUtils.USER_USE_CARD.remove(qq);
    }
}
