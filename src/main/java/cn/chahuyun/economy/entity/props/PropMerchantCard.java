package cn.chahuyun.economy.entity.props;

import jakarta.persistence.Column;

import java.io.Serializable;

/**
 * 神秘商店道具 列表
 */
public class PropMerchantCard extends PropsBase implements Serializable {


    // 可能有币币购买、
    // 可能有赛季币购买、
    // 可能有道具兑换
    // 神秘商人的商品会有打包商品，如：100币币作为一个打包品，每次可以使用100000赛季币购买一份

    // 道具类型 bb 赛季币 道具
    private String priceType;

    // 道具数量
    private double priceCount;

    @Column(columnDefinition = "text")
    private String priceDesc;

    // 是否可交易
    private Boolean tradable;

    // 是否下架
    private Boolean offShelf;

    // 是否可以兑换兑换 道具兑换
    private Boolean exchange;

    // 兑换道具列表
    private String exchangePropList;

    // 是否可以购买
    private Boolean buy;

    // 购买方式 币币购买 赛季币购买
    private String buyType;

    // 购买方式 币币购买 赛季币购买
    private double price;

}
