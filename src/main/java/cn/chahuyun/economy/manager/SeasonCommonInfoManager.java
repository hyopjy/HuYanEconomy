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
       return "雪币";
    }

    public static List<String> getSeasonMoneyNameList(){
        List<String> str = new ArrayList<>(1);
        str.add("雪币");
        return str;
    }

    /**
     * 当前赛季最高鱼竿等级
     *
     * @return
     */
    public static Integer getMaxFishRodLevel(){
        return 134;
    }

    /**
     * 点亮鱼竿成就-限制等级
     *
     * @return
     */
    public static Integer getBadgeFishRodLevel(){
        return 99;
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
