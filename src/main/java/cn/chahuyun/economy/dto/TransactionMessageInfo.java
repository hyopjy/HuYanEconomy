package cn.chahuyun.economy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionMessageInfo {
    /**
     * 交易 道具 3 币币 10086 @934415751
     * 交易 道具 2 雪花 10068 @934415751
     * 交易 道具 1 道具 4 @934415751
     */

    /**
     * 发起者
     */
    private Long initiateUserId;


    /**
     * 需要的道具
     */
    private String initiatePropCode;

    /**
     * 需要的道具数量
     */
    private Integer initiatePropCount;

    /**
     * 交易的编码
     * - FISH_CODE_BB
     * - FISH_CODE_SEASON
     * - FISH_xxx
     */
    private String transactionCode;

    /**
     * 交易的数量
     */
    private Integer transactionCount;

    /**
     * 交易的人
     */
    private Long transactionUserId;

}
