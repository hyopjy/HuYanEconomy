package cn.chahuyun.economy.manager;

import java.util.ArrayList;
import java.util.List;

public class SeasonCommonInfoManager {

    /**
     * 获取当前赛季币信息
     *
     * @return
     */
    public static String getSeasonMoney(){
       return "雪币❄";
    }

    public static List<String> getSeasonMoneyNameList(){
        List<String> str = new ArrayList<>(3);
        str.add("雪币");
        str.add("雪花币");
        str.add("雪花");
        return str;
    }

    /**
     * 当前赛季最高鱼竿等级
     *
     * @return
     */
    public static Integer getMaxFishRodLevel(){
        return 135;
    }

    /**
     * 当前赛季鱼竿单价
     *
     * @return
     */
    public static Integer getFishRodPrice(){
        return 200;
    }

    /**
     * 当前赛季wditBB限定额度
     *
     * @return
     */
    public static Double getWditBB(){
        return 88888.00;
    }

}
