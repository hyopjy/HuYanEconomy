package cn.chahuyun.economy.constant;

import java.util.HashSet;
import java.util.Set;

public class FishSignConstant {
    public static final String FISH_31 = "FISH-31";

    public static final String FISH_32 = "FISH-32";

    public static final String FISH_33 = "FISH-33";


    public static final String FISH_17 = "FISH-17";

    public static final String FISH_15 = "FISH-15";

    public static final String FISH_16 = "FISH-16";

    public static final String FISH_20 = "FISH-20";

    public static final String FISH_39 = "FISH-39";

    public static final String FISH_49 = "FISH-49";

    /**
     * 特殊成就
     */
    public static final String FISH_SPECIAL= "FISH-SPECIAL";

    public static final String FISH_62 = "FISH-62";

    /**
     * 通用成就
     * @return
     */
    public static Set<String> getSignPropCode(){
        HashSet<String> strings = new HashSet<>(7);
        strings.add(FISH_17);
        strings.add(FISH_15);
        strings.add(FISH_16);
        strings.add(FISH_20);
        strings.add(FISH_39);
        strings.add(FISH_49);
        strings.add(FISH_62);
        return strings;
    }
}
