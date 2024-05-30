package cn.chahuyun.economy.entity.merchant;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 当前兑换列表
 */
@Entity(name = "MysteriousMerchantGoods")
@Table
@Getter
@Setter
public class MysteriousMerchantGoods implements Serializable {
    private static final long serialVersionUID = 3160787433977824925L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 设置id
     */
    private Long settingId;

    /**
     * 群聊id
     */
    private Long groupId;

    /**
     * 商品编码
     */
    private String goodCode;

    /**
     * 商品库存
     */
    private Integer goodStored;

    /**
     * 已出售
     */
    private Integer sold;

    /**
     * 开始小时数
     */
    private Integer hour;

    private Integer startMinutes;

    private Integer endMinutes;

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
