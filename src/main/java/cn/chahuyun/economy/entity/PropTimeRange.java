package cn.chahuyun.economy.entity;

import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "PropTimeRange")
@Table
@NoArgsConstructor
public class PropTimeRange {
    @Id
    private int weekDay;

    private String time;

    public PropTimeRange(int weekDay, String time) {
        this.weekDay = weekDay;
        this.time = time;
    }

    public PropTimeRange save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }

    public String getDesc(){
        return "Prop-[星期：" + weekDay + "时间区间：" + time +"] \r\n";
    }
}
