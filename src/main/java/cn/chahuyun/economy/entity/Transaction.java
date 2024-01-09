package cn.chahuyun.economy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Transaction")
@Table
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 交易 道具 3 币币 10086 @934415751
     * 交易 道具 2 雪花 10068 @934415751
     * 交易 道具 1 道具 4 @934415751
     */

    /**
     * 发起者
     */
    private Long initiateUser;


    /**
     * 需要的道具
     */
    private String initiatePropCode;

    /**
     * 需要的道具
     */
    private String initiatePropCount;

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
    private String transactionCount;

    /**
     * 交易的数量
     */
    private String transactionUserId;

    /**
     * 交易状态  0 等待交易 1 完成交易-删除
     */
    private Integer transactionStatus;

}
