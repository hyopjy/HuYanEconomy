package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import net.mamoe.mirai.event.events.MessageEvent;

public interface PropUsageFactory {

    IPropUsage createGlassBead();


    IPropUsage createSisterDog();

    IPropUsage createMask();

    IPropUsage createFiveFlavoredFish();

    IPropUsage createLittleTrumpetFishStory();
}
