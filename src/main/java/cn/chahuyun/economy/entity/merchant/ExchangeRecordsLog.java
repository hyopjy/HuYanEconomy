package cn.chahuyun.economy.entity.merchant;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户兑换记录表
 */
@Entity(name = "ExchangeRecordsLog")
@Table
@Getter
@Setter
public class ExchangeRecordsLog implements Serializable {


    private static final long serialVersionUID = 4488100555092286240L;

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
     *  用户id
     */
    private Long userId;

    /**
     * 商品编码
     */
    private String goodCode;


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
