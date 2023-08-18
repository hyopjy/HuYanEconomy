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
            default:
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
        if(abstractPropUsage.checkOrder()){
            //
            if(Objects.nonNull(abstractPropUsage.getTarget())){
                if (CacheUtils.TIME_PROHIBITION.containsKey(CacheUtils.timeCacheKey(group.getId(),abstractPropUsage.getTarget()))) {
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "指定对象正则被使用[姐姐的狗]"));
                    return;
                }

                if(CacheUtils.FISH_COUNT.containsKey(CacheUtils.userFishCountKey(group.getId(),abstractPropUsage.getTarget()))){
                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "指定对象正则被使用[年年有鱼]"));
                    return;
                }
            }

            abstractPropUsage.excute();
            PluginManager.getPropsManager().deleteProp(userInfo,propsCard);
        }
    }

}
