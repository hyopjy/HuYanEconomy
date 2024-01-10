package cn.chahuyun.economy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUserInfo {

    /**
     * 交易发起人
     */
    private Long initiateUserId;

    /**
     * 被交易
     */
    private Long transactionUserId;
}
