package cn.chahuyun.economy.constant;

import java.util.HashSet;
import java.util.Set;

public class FishSignConstant {

    // 设置 fish-15 数量 位置
//            FISH-15	PFPFK
//            FISH-16	PFTNK
//            FISH-17	HK警匪片——FreenBecky版
//            FISH-20	《小喇叭鱼的故事》
//            FISH-31	姐一徽章
//            FISH-32	妹一徽章
//            FISH-33	互攻徽章

    public static final String FISH_31 = "FISH-31";

    public static final String FISH_32 = "FISH-32";

    public static final String FISH_33 = "FISH-33";


    public static final String FISH_17 = "FISH-17";

    public static final String FISH_15 = "FISH-15";

    public static final String FISH_16 = "FISH-16";

    public static final String FISH_20 = "FISH-20";

    public static Set<String> getSignPropCode(){
        HashSet<String> strings = new HashSet<>(4);
        strings.add(FISH_17);
        strings.add(FISH_15);
        strings.add(FISH_16);
        strings.add(FISH_20);
        return strings;
    }
}
