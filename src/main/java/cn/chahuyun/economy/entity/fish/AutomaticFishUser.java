package cn.chahuyun.economy.entity.fish;


import cn.chahuyun.economy.dto.AutomaticFish;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "AutomaticFishUser")
@Table
@Getter
@Setter
public class AutomaticFishUser {
    @Id
    private long id;

    private long groupId;

    private long fishUser;

    private LocalDateTime openTime;

    private LocalDateTime endTime;

    private String cron;

//    @SerialName("automaticFishList")
//    val automaticFishList: List<AutomaticFish>,

    @Column(columnDefinition = "text")
    private String automaticFishStr;

    public AutomaticFishUser() {
    }

    public AutomaticFishUser(long id, long groupId, long fishUser, LocalDateTime openTime, LocalDateTime endTime, String cron,
                             List<AutomaticFish> automaticFishStr) {
        this.id = id;
        this.groupId = groupId;
        this.fishUser = fishUser;
        this.openTime = openTime;
        this.endTime = endTime;
        this.cron = cron;
        this.automaticFishStr = JSONUtil.toJsonStr(automaticFishStr);
    }

    public AutomaticFishUser(long groupId, long fishUser, LocalDateTime openTime, LocalDateTime endTime, String cron,
                             List<AutomaticFish> automaticFishStr) {
        this(IdUtil.getSnowflakeNextId(),groupId,fishUser,openTime,endTime,cron,automaticFishStr);
    }

    public static  List<AutomaticFishUser>  getAutomaticFishUser(Long groupId, Long fishUser) {
        try {
            List<AutomaticFishUser> list = HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<AutomaticFishUser> query = builder.createQuery(AutomaticFishUser.class);
                JpaRoot<AutomaticFishUser> from = query.from(AutomaticFishUser.class);
                query.select(from);
                query.where(builder.equal(from.get("groupId"),groupId),
                        builder.equal(from.get("fishUser"), fishUser));
                return session.createQuery(query).list();
            });
            return list;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    public boolean saveOrUpdate() {
        try {
            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        } catch (Exception e) {
            Log.error("自动钓鱼机:保存自动钓鱼机出错", e);
            return false;
        }
        return true;
    }

    /**
     * 删除
     */
    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
