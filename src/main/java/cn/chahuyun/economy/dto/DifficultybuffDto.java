package cn.chahuyun.economy.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DifficultybuffDto implements Serializable {

    private int difficultyMin = 0;

    private int rankMin = 0;

    private int count = 0;

}
