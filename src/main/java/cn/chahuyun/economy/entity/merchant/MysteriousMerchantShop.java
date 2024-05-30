package cn.chahuyun.economy.entity.merchant;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 商店商品列表
 */
@Entity(name = "MysteriousMerchantProp")
@Table
@Getter
@Setter
public class MysteriousMerchantShop implements Serializable {
    private static final long serialVersionUID = 6753823309734807199L;

    @Id
    private String goodCode;

    private String prop1Code;

    private String prop2Code;

    private Integer prop2Count;

    private Double bbCount;

    private Double seasonMoney;

    /**
     * 兑换方式
     * 0 道具兑换
     * 1 bb兑换
     * 2 赛季币兑换
     */
    private Integer changeType;

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
