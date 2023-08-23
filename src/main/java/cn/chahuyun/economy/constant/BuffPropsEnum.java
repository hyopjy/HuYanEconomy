package cn.chahuyun.economy.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BuffPropsEnum {
    DIFFICULTY_MIN("difficultyMin"),
    RANK_MIN("rankMin"),

    /**
     * 其他鱼
     */
    OTHER_FISH("other"),

    /**
     * 指定鱼
     */
    SPECIAL_FISH("special_fish"),

    /**
     * 指定等级
     */
    SPECIAL_LEVEL("special_level")

    ;
    final String name;
}
