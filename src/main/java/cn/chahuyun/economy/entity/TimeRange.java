package cn.chahuyun.economy.entity;
import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "TimeRange")
@Table
@NoArgsConstructor
public class TimeRange {
    @Id
    private int weekDay;

    private String time;

    public TimeRange(int weekDay, String time) {
        this.weekDay = weekDay;
        this.time = time;
    }

    public TimeRange save() {
        return HibernateUtil.factory.fromTransaction(session -> session.merge(this));
    }

    public void remove() {
        HibernateUtil.factory.fromTransaction(session -> {
            session.remove(this);
            return null;
        });
    }

    public String getDesc(){
        return "星期：" + weekDay + "时间区间：" + time +"\r\n";
    }
}
