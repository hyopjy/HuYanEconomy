package cn.chahuyun.economy.dto;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Buff implements Serializable {

    private Long groupId;

    private Long qq;
    /**
     * buff名称
     */
    private String buffName = "";

    /**
     * 次数
     */
    private int count = 0;

    /**
     * bufftype
     */
    private Integer buffType;

    /**
     * buff属性
     */
    private List<BuffProperty> properties = new ArrayList();
}
