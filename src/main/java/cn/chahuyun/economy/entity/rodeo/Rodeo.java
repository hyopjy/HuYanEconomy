package cn.chahuyun.economy.entity.rodeo;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 决斗、轮盘、大乱斗
 */
@Entity(name = "Rodeo")
@Table
@Getter
@Setter
public class Rodeo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 群组id
    private Long groupId;

    // 场次名称
    @Column(columnDefinition = "text")
    private String venue;

    // 选手 -- 按照逗号分割的多方
    @Column(columnDefinition = "text")
    private String players;

    // 配置日期   2024-08-23
    private String day;

    // 时间段        10:15
    private String startTime;

    // 时间段
    private String endTime;

    // 局数
    private int round;

    // 玩法（决斗、轮盘、大乱斗）
    private String playingMethod;


    public Rodeo() {
    }

    public Rodeo(Long groupId, String venue, String day, String startTime, String endTime, String players, int round, String playingMethod) {
        this.groupId = groupId;
        this.setVenue(venue);
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.players = players;
        this.round = round;
        this.playingMethod = playingMethod;
    }

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
