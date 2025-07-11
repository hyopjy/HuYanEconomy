package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.aop.Prop;
import cn.chahuyun.economy.constant.PropConstant;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.utils.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
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
                iPropUsage = factory.createGlassBead();
                break;
            case PropConstant.SISTER_DOG:
                iPropUsage = factory.createSisterDog();
                break;
            case PropConstant.FIVE_FLAVORED_FISH:
                iPropUsage = factory.createFiveFlavoredFish();
                break;
            case PropConstant.MASK:
                iPropUsage = factory.createMask();
                break;
            // todo FBPFK
            case PropConstant.FBPFK:
                iPropUsage = factory.createFBPFK();
                break;
            // todo FBTNK
            case PropConstant.FBTNK:
                iPropUsage = factory.createFBTNK();
                break;
            // todo HK警匪片——FreenBecky版
            case PropConstant.HKFB:
                iPropUsage = factory.createHKFB();
                break;
            case PropConstant.LITTLE_TRUMPET_FISH_STORY:
                iPropUsage = factory.createLittleTrumpetFishStory();
                break;
            case PropConstant.SCH_DINGER_FISH:
                iPropUsage = factory.createSchDingerFish();
                break;
            case PropConstant.FREEN_FISH:
                iPropUsage = factory.createFreenFish();
                break;
            case PropConstant.BECKY_FISH:
                iPropUsage = factory.createBeckyFish();
                break;
            case PropConstant.SPECIAL_TITLE_ALAWYS:
                iPropUsage = factory.createSpecialTitleAlawys();
                break;
            case PropConstant.SPECIAL_TITLE_ONE_DAY:
                iPropUsage  = factory.createSpecialTitleOneDay();
                break;
            case PropConstant.AUTOMATIC_FISH:
                iPropUsage = factory.createAutomaticFish();
                break;
            case PropConstant.FISH_ON_THE_BLADE:
                iPropUsage = factory.createFishOnTheBlade();
                break;
            case PropConstant.WHOLE_HEARTED_FISHING_PERMIT:
                iPropUsage = factory.createWholeheartedFishingPermit();
                break;
            case PropConstant.FISH_TROUBLED_WATERS:
                iPropUsage = factory.createFishTroubledWaters();
                break;
            case PropConstant.FISHING_ROD:
                iPropUsage = factory.createFishingRod();
                break;
            case PropConstant.THE_SHELL_TRUMPET:
                iPropUsage = factory.createTheShellTrumpet();
                break;
            case PropConstant.CLICKER:
                iPropUsage = factory.createClicker();
                break;
            case PropConstant.SPORTS_AND_ART_STUDENTS:
                iPropUsage = factory.createSportsAndArtStudents();
                break;
            case PropConstant.WDIT_BB_4_0:
                iPropUsage = factory.createWDITBB40();
                break;
            case PropConstant.ULNS_2324:
                iPropUsage = factory.createULNS_2324();
                break;
            case PropConstant.MOONLIGHT_SPRINKLES_INTO_FROSTING:
                iPropUsage = factory.createMoonlightSprinklesIntoFrosting();
                break;
            case PropConstant.OMNIPOTENT_PROPS:
                iPropUsage = factory.createOmnipotentProps();
                break;
            case PropConstant.CHAMPIONSHIP_TROPHY:
                iPropUsage = factory.createChampionshipTrophy();
                break;
            case PropConstant.FALLING_FLOWERS_FALL:
                iPropUsage = factory.createFallingFlowersFall();
                break;
            case PropConstant.GAP:
                iPropUsage = factory.createGAP();
                break;
            case PropConstant.FISH_SIGN_1:
            case PropConstant.FISH_SIGN_2:
            case PropConstant.FISH_SIGN_3:
            case PropConstant.FISH_SIGN_4:
            case PropConstant.FISH_SIGN_5:
            case PropConstant.FISH_SIGN_6:
            case PropConstant.FISH_SIGN_7:
            case PropConstant.FISH_SIGN_8:
                iPropUsage = factory.createFishingCostume();
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
            if(abstractPropUsage.getDeleteProp()){
                PluginManager.getPropsManager().deleteProp(userInfo, propsCard);
            }
        }
    }

}
