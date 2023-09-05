package cn.chahuyun.economy.constant;

import cn.chahuyun.economy.entity.currency.GoldEconomyCurrency;
import xyz.cssxsh.mirai.economy.service.EconomyCurrency;

import java.time.format.DateTimeFormatter;


/**
 * 固定常量
 *
 * @author Moyuyanli
 * @date 2022/11/14 12:43
 */
public interface Constant {

    /**
     * 货币 [金币]
     */
    EconomyCurrency CURRENCY_GOLD = new GoldEconomyCurrency();
    /**
     * 签到双倍金币卡 k - 卡， QD- 签到，2 - 2倍，01 - 一次性,
     */
    String SIGN_DOUBLE_SINGLE_CARD = "K-QD-2-01";

    /**
     * 前置buff
     */
    Integer BUFF_FRONT = 1;

    /**
     * 后置buff
     */
    Integer BUFF_BACK = 2;

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}
