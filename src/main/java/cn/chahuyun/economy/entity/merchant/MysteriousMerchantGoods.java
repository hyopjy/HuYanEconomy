package cn.chahuyun.economy.entity.merchant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 设置商品信息后 生成商品 余量 和 总量信息
 */
@Entity(name = "MysteriousMerchantGoods")
@Table
@Getter
@Setter
public class MysteriousMerchantGoods implements Serializable {
    private static final long serialVersionUID = 3160787433977824925L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long goodsId;

    /**
     * 设置id
     */
    private Long settingId;

    /**
     * 商品编码
     */
    private String goodCode;

    /**
     * 商品数量
     */
    private Integer goodStored;

    /**
     * 已出售
     */
    private Integer sold;

    /**
     * 限购次数
     */
    private Integer buyCount;

    /**
     * 开始小时数
     */
    private Integer hour;

    private Integer startMinutes;

    private Integer endMinutes;
}
