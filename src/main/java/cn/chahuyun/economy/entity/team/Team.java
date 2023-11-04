package cn.chahuyun.economy.entity.team;

import cn.chahuyun.economy.entity.boss.WorldBossConfig;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 组队信息表
 */
@Entity(name = "Team")
@Table
@Getter
@Setter
@NoArgsConstructor
public class Team implements Serializable {

    @Id
    private Long id;

    @Column(columnDefinition = "text")
    private String teamName;

    // 所有者
    private Long teamOwner;


    private Long teamMember;

    private LocalDateTime successTime;

    /**
     * 0 -- 待确认
     * 1 -- 组队成功
     */
    private Integer teamStatus;

    private Long groupId;


    public Team(String teamName,Long teamOwner, Long teamMember, LocalDateTime successTime, Integer teamStatus,Long groupId) {
        this.id = IdUtil.getSnowflakeNextId();
        this.teamName = teamName;
        this.teamOwner = teamOwner;
        this.teamMember = teamMember;
        this.successTime = successTime;
        this.teamStatus = teamStatus;
        this.groupId = groupId;
    }

    public Team save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
