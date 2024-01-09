package cn.chahuyun.economy.entity;

import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Transaction")
@Table
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    private long id;

    /**
     * 交易 道具 3 币币 10086 @934415751
     * 交易 道具 2 雪花 10068 @934415751
     * 交易 道具 1 道具 4 @934415751
     */

    /**
     * 发起者
     */
    private long initiateUserId;


    /**
     * 需要的道具
     */
    private String initiatePropCode;

    /**
     * 需要的道具
     */
    private int initiatePropCount;

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
    private int transactionCount;

    /**
     * 交易的人
     */
    private long transactionUserId;

    /**
     * 交易状态  0 等待交易 1 完成交易-删除
     */
    private int transactionStatus;

    public Transaction(Long id,
                       Long initiateUserId,
                       String initiatePropCode,
                       Integer initiatePropCount,
                       String transactionCode,
                       Integer transactionCount,
                       Long transactionUserId,
                       Integer transactionStatus) {
        this.id = id;
        this.initiateUserId = initiateUserId;
        this.initiatePropCode = initiatePropCode;
        this.initiatePropCount = initiatePropCount;
        this.transactionCode = transactionCode;
        this.transactionCount = transactionCount;
        this.transactionUserId = transactionUserId;
        this.transactionStatus = transactionStatus;
    }

    public Transaction save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }

}
