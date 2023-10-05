package cn.chahuyun.economy.entity.boss;

import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "WorldBossUserLog")
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorldBossUserLog implements Serializable {
    private static final long serialVersionUID = -8335258263249342598L;
    @Id
    private Long id;

    private Long groupId;

    private Long userId;

    private double size;

    private LocalDateTime dateTime;

    public WorldBossUserLog save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
