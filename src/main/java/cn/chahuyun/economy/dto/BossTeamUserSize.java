package cn.chahuyun.economy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BossTeamUserSize {
    /**
     * 根据type判断是 teamid 还是 userid
     */
    private Long id;

    Integer fishSize;

    /**
     * type =1 team  type = 0 user
      */
    private Integer type;


    /**
     * team 模式下
     */
    Long teamOwner;
    Long teamMember;

    String teamName;
}
