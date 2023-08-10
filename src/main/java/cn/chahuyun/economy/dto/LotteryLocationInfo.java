package cn.chahuyun.economy.dto;

import cn.chahuyun.economy.entity.LotteryInfo;
import lombok.Data;

@Data
public class LotteryLocationInfo {
    private LotteryInfo lotteryInfo;

    private int location;
}
