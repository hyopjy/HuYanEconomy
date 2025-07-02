package cn.chahuyun.economy.utils;

public class CurrencyConverter {
    private static final char[] DIGITS = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};
    private static final String[] SECTION_UNITS = {"", "万", "亿", "万亿", "亿亿", "万亿亿"};
    private static final String[] INNER_UNITS = {"千", "百", "十", ""};

    /**
     * 将double类型的金额转换为中文人民币单位
     * @param amount 金额（double类型）
     * @return 中文表示的人民币金额（不含"元"）
     */
    public static String convertToChinese(double amount) {
        // 处理小数部分（四舍五入到整数）
        long integerPart = Math.round(Math.abs(amount));
        String sign = amount < 0 ? "负" : "";

        if (integerPart == 0) {
            return "零";
        }

        return sign + convertLongToChinese(integerPart);
    }

    /**
     * 将double类型的金额转换为中文人民币单位（带小数处理）
     * @param amount 金额（double类型）
     * @return 中文表示的人民币金额（不含"元"）
     */
    public static String convertToChineseWithDecimal(double amount) {
        // 处理负数
        String sign = amount < 0 ? "负" : "";
        double absAmount = Math.abs(amount);

        // 分离整数和小数部分
        long integerPart = (long) Math.floor(absAmount);
        int decimalPart = (int) Math.round((absAmount - integerPart) * 100);

        String result = "";

        // 转换整数部分
        if (integerPart > 0 || decimalPart == 0) {
            result += convertLongToChinese(integerPart);
        }

        // 转换小数部分（角分）
        if (decimalPart > 0) {
            if (integerPart > 0) {
                result += "点";
            }

            // 处理小数部分（0-99）
            int jiao = decimalPart / 10;  // 角
            int fen = decimalPart % 10;   // 分

            if (jiao > 0) {
                result += DIGITS[jiao] + "角";
            }
            if (fen > 0) {
                result += DIGITS[fen] + "分";
            }
        }

        return sign + result;
    }

    /**
     * 将long类型的整数转换为中文表示
     */
    private static String convertLongToChinese(long number) {
        if (number == 0) return "零";

        String numStr = String.valueOf(number);
        int len = numStr.length();
        int sections = (len + 3) / 4;
        String[] sectionsArr = new String[sections];

        // 分割为4位一节的数组
        for (int i = 0; i < sections; i++) {
            int end = len - i * 4;
            int start = Math.max(0, end - 4);
            sectionsArr[i] = numStr.substring(start, end);
        }

        StringBuilder result = new StringBuilder();
        boolean needZero = false;

        // 从高位到低位处理每一节
        for (int i = sections - 1; i >= 0; i--) {
            // 补足4位
            String padded = String.format("%4s", sectionsArr[i]).replace(' ', '0');
            String sectionStr = convertSection(padded, i == sections - 1);

            if (!sectionStr.isEmpty()) {
                if (needZero) {
                    result.append('零');
                    needZero = false;
                }
                result.append(sectionStr);

                // 添加节单位（万/亿/万亿等）
                if (i < SECTION_UNITS.length) {
                    result.append(SECTION_UNITS[i]);
                } else {
                    // 超过预定义单位时使用组合单位
                    result.append(SECTION_UNITS[i % 2 + 2]).append(SECTION_UNITS[i / 2]);
                }
            } else if (result.length() > 0) {
                needZero = true;
            }
        }

        return result.toString();
    }

    /**
     * 将4位数字字符串转换为中文表示
     * @param section 4位数字字符串
     * @param isFirstSection 是否是最高节
     */
    private static String convertSection(String section, boolean isFirstSection) {
        StringBuilder sb = new StringBuilder();
        boolean hasNonZero = false;  // 是否遇到非零数字
        boolean lastIsZero = true;   // 上一位是否为零（初始为true避免开头补零）

        for (int j = 0; j < 4; j++) {
            char c = section.charAt(j);
            int digit = c - '0';

            if (digit == 0) {
                if (hasNonZero) {
                    lastIsZero = true;
                }
            } else {
                // 处理连续零
                if (lastIsZero && hasNonZero) {
                    sb.append('零');
                    lastIsZero = false;
                }

                // 处理十位的"一"省略规则
                if (digit == 1 && j == 2 && isFirstSection && !hasNonZero) {
                    sb.append(INNER_UNITS[j]);  // 省略"一"只加"十"
                } else {
                    sb.append(DIGITS[digit]);
                    sb.append(INNER_UNITS[j]);
                }

                hasNonZero = true;
                lastIsZero = false;
            }
        }

        return sb.toString();
    }

    // 测试代码
    public static void main(String[] args) {
        double[] testCases = {
                0,                          // 零
                0.45,                       // 四角五分
                10.1,                       // 一十点一角
                100.5,                      // 一百点五角
                1000.99,                    // 一千点九角九分
                10000,                      // 一万
                789000000.12,               // 七亿八千九百万点一角二分
                -123456789.45,              // 负一亿二千三百四十五万六千七百八十九点四角五分
                1.23e10,                    // 一百二十三亿
                9.87654e12,                 // 九万八千七百六十五亿四千万
                1.23456789e18,              // 十二万三千四百五十六亿七千八百九十万
                1234567890123456789.99       // 十二万三千四百五十六亿七千八百九十万一千二百三十四万五千六百七十八点九角九分
        };

        System.out.println("===== 整数部分转换 =====");
        for (double num : testCases) {
            System.out.printf("%.2f: %s\n", num, convertToChinese(num));
        }

        System.out.println("\n===== 含小数部分转换 =====");
        for (double num : testCases) {
            System.out.printf("%.2f: %s\n", num, convertToChineseWithDecimal(num));
        }
    }
}