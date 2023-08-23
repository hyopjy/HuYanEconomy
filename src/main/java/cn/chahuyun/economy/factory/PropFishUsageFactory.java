package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.command.*;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import net.mamoe.mirai.event.events.MessageEvent;

public class PropFishUsageFactory {
    /**
     * 创建玻璃球
     *
     * @return
     */
    public IPropUsage createGlassBead() {
        return new Glassbead();
    }

    /**
     * 姐姐的狗
     * @return
     */
    public IPropUsage createSisterDog() {
        return new SisterDog();
    }

    /**
     * 面具
     * @return
     */
    public IPropUsage createMask() {
        return new Mask();
    }

    public IPropUsage createFiveFlavoredFish() {
        return new FiveFlavoredFish();
    }

    public IPropUsage createLittleTrumpetFishStory() {
        return new LittleTrumpetFishStory();
    }

    public IPropUsage createSchDingerFish() {
        return new SchDingerFish();
    }

    public IPropUsage createFreenFish() {
        return new FreenFish();
    }

    public IPropUsage createBeckyFish() {
        return new BeckyFish();
    }

    public IPropUsage createSpecialTitleAlawys() {
        return new SpecialTitleAlawys();
    }

    public IPropUsage createFishOnTheBlade() {
        return new FishOnTheBlade();
    }
}
