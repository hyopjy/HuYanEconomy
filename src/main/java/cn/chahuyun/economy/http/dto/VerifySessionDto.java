package cn.chahuyun.economy.http.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifySessionDto extends BaseResultDto{
    private static final long serialVersionUID = -2900887144384972381L;

    private String session;
}
