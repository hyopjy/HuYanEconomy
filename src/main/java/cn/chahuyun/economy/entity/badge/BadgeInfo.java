package cn.chahuyun.economy.entity.badge;

import cn.chahuyun.economy.entity.LotteryInfo;
import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.manager.BadgeInfoManager;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "BadgeInfo")
@Table
@NoArgsConstructor
public class BadgeInfo {
    @Id
    private Long id;

    private Long groupId;


    private Long qq;

    private String propCode;

    private Integer count;

    /**
     * 达成成就时间
     *  赛季成就 删除
     *  特殊成就 - 永久
     *  通用成就 - 永久
     */
    private LocalDateTime time;

    @Column(columnDefinition = "text")
    private String content;

    public BadgeInfo(Long id, Long groupId, Long qq, String propCode, Integer count, LocalDateTime time, String content) {
        this.id = id;
        this.groupId = groupId;
        this.qq = qq;
        this.propCode = propCode;
        this.count = count;
        this.time = time;
        this.content = content;
    }

    public static BadgeInfo getBadgeInfo(Long groupId, Long qq, String propCode, LocalDateTime time,String content) {
        Long nextId = IdUtil.getSnowflakeNextId();
        // int countNew = BadgeInfoManager.getCount(groupId, qq, propCode);
        return new BadgeInfo(nextId, groupId, qq, propCode, 1, time, content);
    }

    public BadgeInfo save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
