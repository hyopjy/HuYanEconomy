package cn.chahuyun.economy.http.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BaseResultDto implements Serializable {
    private static final long serialVersionUID = -1800149428965325116L;

    private Integer code;

    private String msg;
}
