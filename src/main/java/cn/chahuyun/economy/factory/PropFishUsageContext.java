package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.aop.Prop;
import cn.chahuyun.economy.constant.PropConstant;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.plugin.PluginManager;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.event.events.MessageEvent;

@Getter
@Setter
public class PropFishUsageContext {

    public PropFishUsageContext() {
    }

    @Prop
    public void excute(PropsFishCard propsCard, UserInfo userInfo, MessageEvent event) {
        IPropUsage iPropUsage  = null;
        PropFishUsageFactory factory = new PropFishUsageFactory();
        String propsCardName = propsCard.getName();
        switch (propsCardName) {
            case PropConstant.GLASS_BEAD:
                iPropUsage = factory.createGlassBead(propsCard, userInfo, event);
                break;
            default:
                break;
        }
        if(iPropUsage ==null){
            return;
        }
        if(iPropUsage.checkOrder()){
            iPropUsage.excute();
            PluginManager.getPropsManager().deleteProp(userInfo,propsCard,1);
        }
    }

}
