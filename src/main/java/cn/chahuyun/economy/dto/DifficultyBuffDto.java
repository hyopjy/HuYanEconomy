package cn.chahuyun.economy.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DifficultyBuffDto implements Serializable {
    /**
     * buff名称
     */
    private String buffName = "";

    /**
     * difficultyMin ++
     */
    private int difficultyMin = 0;
    /**
     * rankMin ++
     */
    private int rankMin = 0;

    /**
     * 次数
     */
    private int count = 0;

}
