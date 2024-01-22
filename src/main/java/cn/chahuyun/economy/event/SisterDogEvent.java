package cn.chahuyun.economy.event;

import lombok.*;

/**
 * // 步骤1：创建事件类
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SisterDogEvent {
    Long groupId;
    Long userId;
}
