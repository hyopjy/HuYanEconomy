package cn.chahuyun.economy.constant;

import cn.chahuyun.economy.entity.currency.GoldEconomyCurrency;
import xyz.cssxsh.mirai.economy.service.EconomyCurrency;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


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

    DateTimeFormatter FORMATTER_YYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    int BOSS_TYPE_INT = 1;

   int BOSS_TYPE_BOOLEAN = 0;

   int BOSS_TYPE_STRING = 2;

    int BOSS_TYPE_DOUBLE = 3;

    String BOSS_PROP_PROBABILITY_TYPE = "1";

    String BOSS_PROP_COUNT_TYPE = "2";

    List<String> FISH_NAME_BB_LIST = Arrays.asList(new String[]{"币币", "bb"});

    /**
     * wditbb编码
     */
    String FISH_CODE_BB = "FISH-bb";

    /**
     * 赛季币编码
     */
    String FISH_CODE_SEASON = "FISH-season";
}
