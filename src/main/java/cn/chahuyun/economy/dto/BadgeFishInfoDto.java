package cn.chahuyun.economy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BadgeFishInfoDto implements Serializable {

    private Long groupId;

    private Long qq;

}
