package cn.chahuyun.economy.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContentUserWinEventDTO {

    private String messageType;

    private Long groupId;

    private List<Long> userIds;

    private String propCode;
}
