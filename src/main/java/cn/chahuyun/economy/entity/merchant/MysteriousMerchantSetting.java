package cn.chahuyun.economy.entity.merchant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 设置商品数量
 */
@Entity(name = "MysteriousMerchantSetting")
@Table
@Getter
@Setter
public class MysteriousMerchantSetting implements Serializable {

    private static final long serialVersionUID = 6353309591300277475L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long settingId;

    /**
     * 开始小时数
     */
    private String hourStr;

    /**
     * 开始分钟数
     */
    private String minuteStr;

    /**
     * 概率
     */
    private Integer probability;

    /**
     * 过多久消失
     */
    private Integer passMinute;

    /**
     * 上架商品列表
     */
    private String goodCodeStr;

    /**
     * 上架商品限制数量
     */
    private Integer goodLimit;

    /**
     * 随机几种商品
     */
    private Integer randomGoodCount;

}
