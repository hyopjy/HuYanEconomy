package cn.chahuyun.economy.utils;

import cn.hutool.core.util.RandomUtil;

public class RandomHelperUtil {
    /**
     * 20%的概率
     * @return
     */
    public static boolean checkRandomLuck1_20(){
        int random = RandomUtil.randomInt(1, 21);
        int luck = 10;
        return random == luck;
    }
    /**
     * 1%的概率
     * @return
     */
    public static boolean checkRandomLuck1_1000(){
        int random = RandomUtil.randomInt(1, 1000);
        int luck = 55;
        return random == luck;
    }
}
