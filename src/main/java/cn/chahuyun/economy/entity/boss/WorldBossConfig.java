package cn.chahuyun.economy.entity.boss;

import cn.chahuyun.economy.entity.LotteryInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.intellij.lang.annotations.Identifier;

import java.io.Serializable;

@Entity(name = "WorldBossConfig")
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorldBossConfig implements Serializable {
    private static final long serialVersionUID = 4582665508923919058L;

    @Id
    private int keyId;

    private String keyString;

    @Column(columnDefinition = "text")
    private String configInfo;


    public WorldBossConfig save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }
}
