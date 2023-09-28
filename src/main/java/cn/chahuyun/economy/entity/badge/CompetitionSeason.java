package cn.chahuyun.economy.entity.badge;

import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "CompetitionSeason")
@Table
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionSeason {
    @Id
    private Long id;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     *  结束时间
     */
    private LocalDateTime endTime;

    /**
     * cron
     */
    private String cron;


    public CompetitionSeason save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
