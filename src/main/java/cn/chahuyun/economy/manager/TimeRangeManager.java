package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.LotteryInfo;
import cn.chahuyun.economy.entity.TimeRange;
import cn.chahuyun.economy.utils.HibernateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;

public class TimeRangeManager {

    public static TimeRange getByWeekDay(int weekDay) {
        List<TimeRange> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<TimeRange> query = builder.createQuery(TimeRange.class);
            JpaRoot<TimeRange> from = query.from(TimeRange.class);
            query.select(from);
            query.where(builder.equal(from.get("weekDay"), weekDay));
            return session.createQuery(query).list();
        });
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        return list.get(0);
    }

    public static List<TimeRange> getTimeRangeList() {
        List<TimeRange> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<TimeRange> query = builder.createQuery(TimeRange.class);
            JpaRoot<TimeRange> from = query.from(TimeRange.class);
            query.select(from);
            return session.createQuery(query).list();
        });
        return list;
    }
}
