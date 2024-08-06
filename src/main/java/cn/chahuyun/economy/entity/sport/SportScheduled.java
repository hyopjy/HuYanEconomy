package cn.chahuyun.economy.entity.sport;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Sport")
@Table
@Getter
@Setter
public class SportScheduled {
    @Id
    private Long id;

    /**
     * 场次id
     */
    private Long sportId;

    private String sportTime;

    private String winner;

    private String loser;

}
