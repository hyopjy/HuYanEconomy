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

@Entity(name = "WorldPropConfig")
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorldPropConfig implements Serializable {

    private static final long serialVersionUID = 387705847872980074L;

    @Id
    private Long id;

    private String propCode;

    private String type;

    private Integer configInfo;

    public WorldPropConfig save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
