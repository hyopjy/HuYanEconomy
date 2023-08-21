package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.command.FiveFlavoredFish;
import cn.chahuyun.economy.command.Glassbead;
import cn.chahuyun.economy.command.Mask;
import cn.chahuyun.economy.command.SisterDog;
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
    public IPropUsage createGlassBead() {
        return new Glassbead();
    }

    /**
     * 姐姐的狗
     * @return
     */
    @Override
    public IPropUsage createSisterDog() {
        return new SisterDog();
    }

    /**
     * 面具
     * @return
     */
    @Override
    public IPropUsage createMask() {
        return new Mask();
    }

    @Override
    public IPropUsage createFiveFlavoredFish() {
        return new FiveFlavoredFish();
    }
}
