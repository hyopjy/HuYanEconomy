package cn.chahuyun.economy.entity.merchant;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
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
    private Long settingId;

    /**
     * 0 关闭
     * 1 开启
     */
    private Boolean status;


    /**
     * 限购次数
     */
    private Integer buyCount;


    /**
     * 开始小时数
     */
    private String hourStr;

    /**
     * 过多久消失
     */
    private Integer passMinute;

    /**
     * 概率
     */
    private Integer probability;

    /**
     * 上架商品列表
     */
    @Column(columnDefinition = "text")
    private String goodCodeStr;

    /**
     * 随机几种商品
     */
    private Integer randomGoodCount;

    // 最小随机库存数
    private Integer minStored;

    // 最大库存数
    private Integer maxStored;


    public boolean saveOrUpdate() {
        try {
            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        } catch (Exception e) {
            Log.error("神秘商人:更新", e);
            return false;
        }
        return true;
    }
}
