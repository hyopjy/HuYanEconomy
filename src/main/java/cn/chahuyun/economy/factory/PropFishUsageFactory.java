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

    public IPropUsage createSpecialTitleOneDay() {
        return new SpecialTitleOneDay();
    }
    public IPropUsage createFishOnTheBlade() {
        return new FishOnTheBlade();
    }

    public IPropUsage createWholeheartedFishingPermit() {
        return new WholeheartedFishingPermit();
    }

    public IPropUsage createFishTroubledWaters() {
        return new FishTroubledWaters();
    }

    public IPropUsage createAutomaticFish() {
        return new AutomaticFishingMachine();
    }

    public IPropUsage createFBPFK() {
        return new FBPFK();
    }

    public IPropUsage createFBTNK() {
        return new FBTNK();
    }

    public IPropUsage createHKFB() {
        return new HKFB();
    }

    public IPropUsage createFishingRod() {return new FishingRod();}

    public IPropUsage createTheShellTrumpet() {
        return new TheShellTrumpet();
    }

    public IPropUsage createClicker() {
        return new Clicker();
    }

    public IPropUsage createSportsAndArtStudents() {
        return new SportsAndArtStudents();
    }

    public IPropUsage createWDITBB40() {
        return new WDITBB40();
    }

    public IPropUsage createULNS_2324() {
        return new Ulns2324();
    }
}
