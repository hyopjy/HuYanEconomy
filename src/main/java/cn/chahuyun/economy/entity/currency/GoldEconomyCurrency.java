package cn.chahuyun.economy.entity.currency;

import cn.hutool.core.util.NumberUtil;
import org.jetbrains.annotations.NotNull;
import xyz.cssxsh.mirai.economy.service.EconomyCurrency;

/**
 * 货币 [金币]
 *
 * @author Moyuyanli
 * @date 2022/11/9 14:55
 */
public class GoldEconomyCurrency implements EconomyCurrency {
    @NotNull
    @Override
    public String getDescription() {
        return "耀眼的WDIT币币";
    }

    @NotNull
    @Override
    public String getId() {
        return "hy-gold";
    }

    @NotNull
    @Override
    public String getName() {
        return "WDIT币币";
    }

    @NotNull
    @Override
    public String format(double amount) {
        return String.format("%s枚WDIT币币", NumberUtil.roundStr(amount, 0));
    }
}
