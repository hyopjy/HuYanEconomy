package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.aop.Prop;
import cn.chahuyun.economy.constant.PropConstant;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Objects;

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
                iPropUsage = factory.createGlassBead();
                break;
            case PropConstant.SISTER_DOG:
                iPropUsage = factory.createSisterDog();
                break;
            case PropConstant.MASK:
                iPropUsage = factory.createMask();
                break;
            case PropConstant.FIVE_FLAVORED_FISH:
                iPropUsage = factory.createFiveFlavoredFish();
                break;
            case PropConstant.LITTLE_TRUMPET_FISH_STORY:
                iPropUsage = factory.createLittleTrumpetFishStory();
                break;
            case PropConstant.SCH_DINGER_FISH:
                iPropUsage = factory.createSchDingerFish();
                break;
            default:
                event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "道具暂未开放～"));
                break;
        }
        if(iPropUsage ==null){
            return;
        }
        Contact subject = event.getSubject();

        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        if(group ==null){
            return;
        }
        AbstractPropUsage abstractPropUsage = (AbstractPropUsage) iPropUsage;
        abstractPropUsage.setEvent(event);
        abstractPropUsage.setPropsCard(propsCard);
        abstractPropUsage.setUserInfo(userInfo);
        abstractPropUsage.setGroup(group);
        abstractPropUsage.setSubject(subject);
        // 校验命令是否合适
        if(abstractPropUsage.checkOrder()){
            // 如果正在使用buff 则返回
            if(!abstractPropUsage.checkBuff()){
                return;
            }
            abstractPropUsage.excute();
            PluginManager.getPropsManager().deleteProp(userInfo,propsCard);
        }
    }

}
