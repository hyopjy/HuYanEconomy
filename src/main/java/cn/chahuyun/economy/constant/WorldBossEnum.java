package cn.chahuyun.economy.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public enum WorldBossEnum {
    BOSS_STATUS(1,"boss_status", "开关设置", Constant.BOSS_TYPE_BOOLEAN,"true"),
    FISH_SIZE(2,"fish_size", "目标尺寸",  Constant.BOSS_TYPE_INT, "200"),
    WDIT_BB(3,"wdit_bb", "奖励金额",  Constant.BOSS_TYPE_DOUBLE, "5200"),
    WDIT_BB_PROP(9,"wdit_bb_prop", "币币概率奖励金额",  Constant.BOSS_TYPE_DOUBLE, "300"),
    WDIT_BB_COUNT(10,"wdit_bb_count", "币币数量奖励金额",  Constant.BOSS_TYPE_DOUBLE, "600"),



    OPEN_HOUR(4,"open_hour", "开始时间小时",  Constant.BOSS_TYPE_INT, "13"),
    END_HOUR(5,"end_hour", "结束时间小时",  Constant.BOSS_TYPE_INT, "18"),
    CORN_GOAL(6,"world_boss_corn_goal", "达成播报",  Constant.BOSS_TYPE_STRING, "0 0 18 * * ?"),
    CORN_PROGRESS(7,"world_boss_corn_progress", "进度播报",  Constant.BOSS_TYPE_STRING, "0 50 17 * * ?｜0 0/30 14,15,16,17 * * ?"),
    OPEN_HOUR_MINUTE(8,"open_hour_minute", "开始时间小时分钟",  Constant.BOSS_TYPE_INT, "30"),
    CORN_OPEN(11,"world_boss_corn_open", "开始播报定时",  Constant.BOSS_TYPE_STRING, "0 30 13 * * ?"),

//    OPEN_HOUR(4,"open_hour", "开始时间小时",  Constant.BOSS_TYPE_INT, "0"),
//    END_HOUR(5,"end_hour", "结束时间小时",  Constant.BOSS_TYPE_INT, "23"),
//    OPEN_HOUR_MINUTE(8,"open_hour_minute", "开始时间小时分钟",  Constant.BOSS_TYPE_INT, "0"),
//    CORN_GOAL(6,"world_boss_corn_goal", "达成播报",  Constant.BOSS_TYPE_STRING, "0 0/3 * * * ?"),
//    CORN_PROGRESS(7,"world_boss_corn_progress", "进度播报",  Constant.BOSS_TYPE_STRING, "0/30 * * * * ?"),
//    CORN_OPEN(11,"world_boss_corn_open", "开始播报定时",  Constant.BOSS_TYPE_STRING, "0 0/1 * * * ?"),

    ;
    final int keyId;

    final String keyString;

    final String keyDesc;

    /**
     * 1 boolean  2 string 3 integer
     */
    final int type;

    final String value;

    public  static List<WorldBossEnum> getWorldBossEnumList(){
        List<WorldBossEnum> list = new ArrayList<>(11);
        list.add(BOSS_STATUS);
        list.add(FISH_SIZE);
        list.add(WDIT_BB);
        list.add(WDIT_BB_PROP);
        list.add(WDIT_BB_COUNT);

        list.add(OPEN_HOUR);
        list.add(END_HOUR);
        list.add(OPEN_HOUR_MINUTE);
        list.add(CORN_OPEN);
        list.add(CORN_GOAL);
        list.add(CORN_PROGRESS);
        return list;
    }

    public  static List<WorldBossEnum> getWorldBossNotFixEnumList(){
        List<WorldBossEnum> list = new ArrayList<>(6);
        list.add(OPEN_HOUR);
        list.add(END_HOUR);
        list.add(OPEN_HOUR_MINUTE);
        list.add(CORN_OPEN);
        list.add(CORN_GOAL);
        list.add(CORN_PROGRESS);
        return list;
    }
}
