package cn.chahuyun.economy.commonEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.mamoe.mirai.event.AbstractEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserWinEvent  extends AbstractEvent {
    String action;
    public UserWinEvent(String action) {
        this.action = action;
    }

}


