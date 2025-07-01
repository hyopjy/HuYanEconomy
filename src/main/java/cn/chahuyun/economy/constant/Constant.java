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

    DateTimeFormatter FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

    List<String> RGB_LIST = Arrays.asList(new String[]{"R", "G","B"});

    String SPILT = "-";

    String SPILT2 = "|";

    String MM_SPILT = ",";

    String MM_PROP_START = "SS-";
    // String EXCEL_URL = "/Users/zhangyi/Project/github/HuYanEconomy/fish.xlsx";

     String EXCEL_URL = getExcelUrl();

    static String getExcelUrl() {
        String osName = System.getProperty("os.name").toLowerCase();

        // Windows 系统 (添加驱动器路径检测)
        if (osName.contains("win")) {
            return "D:\\project\\private\\HuYanEconomy\\fish.xlsx";
        }
        // Linux 系统 (增加容器环境兼容)
        else if (osName.contains("linux")) {
            return "/www/wwwroot/fish.xlsx";
        }
        // macOS 系统 (适配Unix文件系统)
        else if (osName.contains("mac")) {
            return "/Users/zhangyi/Project/github/HuYanEconomy/fish.xlsx";
        }

        // 未知系统使用相对路径保底
        return "fish.xlsx";
    }
}
