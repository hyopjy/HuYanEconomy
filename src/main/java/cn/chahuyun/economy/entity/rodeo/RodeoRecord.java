package cn.chahuyun.economy.entity.rodeo;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "RodeoRecord")
@Table
@Getter
@Setter
public class RodeoRecord {
    @Id
    private Long id;

    /**
     * 场次id
     */
    private Long rodeoId;

    /**
     * 禁言人员
     */
    private String player;

    /**
     * 禁言时长
     */
    private Integer ForbiddenSpeech;

    /**
     * 第几局
     */
    private Integer turns;

    public boolean saveOrUpdate() {
        try {
            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        } catch (Exception e) {
            Log.error("神秘商人:更新", e);
            return false;
        }
        return true;
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
