package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;

/**
 * 🫧泡泡标识
 * 🪙波币标识
 * 🎰梭哈标识
 * 💴万贯标识
 * 准点打工标识
 * 动物大使标识
 * 狗姐心选标识
 * 天选之子标识
 */
public class FishingCostume extends AbstractPropUsage {
    //    ˚.∘・◌・•°🫧・◦・🫧°•・◌・∘.˚
    //    AAA开始钓鱼
    //    鱼塘:日夜颠岛
    //    等级:6
    //    「纯天然湖泊，鱼情优秀，又大又多」
    //    .˚✧.˚✧.˚🫧˚✧.˚✧.˚🫧˚✧.˚✧.˚

    @Override
    public boolean checkOrder() {
        return false;
    }

    @Override
    public void excute() {
        // 使用后顶掉之前使用的道具
    }


}
