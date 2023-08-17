package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.constant.PropConstant;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.manager.PropsManager;
import cn.chahuyun.economy.plugin.PluginManager;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.events.MessageEvent;

@Getter
@Setter
public class PropFishUsageContext {
    IPropUsage iPropUsage;
    Long  qq;

    public PropFishUsageContext() {
    }

    public PropFishUsageContext(PropsFishCard propsCard, UserInfo userInfo, MessageEvent event) {
        this.qq = userInfo.getQq();
        PropFishUsageFactory factory = new PropFishUsageFactory();
        String propsCardName = propsCard.getName();
        switch (propsCardName) {
            case PropConstant.GLASS_BEAD:
                iPropUsage = factory.createGlassBead(propsCard, userInfo, event);
                break;
            default:
                throw new IllegalArgumentException("Unsupport operation!");
        }

    }

    public void excute() {
        if(iPropUsage.checkOrder()){
            iPropUsage.excute();
            // PluginManager.getPropsManager().deleteProp();
        }
    }

}
