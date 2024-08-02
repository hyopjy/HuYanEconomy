package cn.chahuyun.economy.utils;


import org.apache.commons.lang3.RandomUtils;

public class RandomHelperUtil {
    /**
     * 20%的概率
     * @return
     */
    public static boolean checkRandomLuck1_20(){
        int random =  RandomUtils.nextInt(1, 101);
        return random <= 5;
    }
    /**
     * 1%的概率
     * @return
     */
    public static boolean checkRandomLuck1_1000(){
//        int random = RandomUtils.nextInt(1, 1001);
//        return random <= 1;
        return false;
    }

    public static boolean checkRandomByProp(Integer prop) {
        int random = RandomUtils.nextInt(1, 101);
        return random <= prop;
    }

    public static boolean checkRandomByProp(Double prop) {
        double random = RandomUtils.nextDouble(1, 101);
        return random <= prop;
    }
}
