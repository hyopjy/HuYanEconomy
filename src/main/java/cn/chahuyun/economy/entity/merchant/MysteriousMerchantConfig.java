package cn.chahuyun.economy.entity.merchant;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 开启或者关闭
 */
@Entity(name = "MysteriousMerchantConfig")
@Table
@Getter
@Setter
public class MysteriousMerchantConfig implements Serializable {

    private static final long serialVersionUID = 982053508122807405L;

    @Id
    private Long configId;

    // 开启/关闭
    // 神秘商人 14,17,21 15% 10(几分钟消失)  83-92(商品编码范围) 2(几种道具)  1-3(随机道具库存)

    /**
     * 0 关闭
     * 1 开启
     */
    private Boolean status;


    /**
     * 限购次数
     */
    private Integer buyCount;

    public boolean saveOrUpdate() {
        try {
            this.configId = 1L;
            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        } catch (Exception e) {
            Log.error("神秘商人:更新", e);
            return false;
        }
        return true;
    }

}
