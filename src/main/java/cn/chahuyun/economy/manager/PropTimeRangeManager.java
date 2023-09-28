package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.PropTimeRange;
import cn.chahuyun.economy.entity.TimeRange;
import cn.chahuyun.economy.utils.HibernateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PropTimeRangeManager {

    public static ConcurrentHashMap<Integer, PropTimeRange> PROP_TIME_RANGE_CACHE = new ConcurrentHashMap<>(7);

    public static PropTimeRange getByWeekDay(int weekDay) {
        if (Objects.nonNull(PROP_TIME_RANGE_CACHE.get(weekDay))) {
            return PROP_TIME_RANGE_CACHE.get(weekDay);
        }
        List<PropTimeRange> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PropTimeRange> query = builder.createQuery(PropTimeRange.class);
            JpaRoot<PropTimeRange> from = query.from(PropTimeRange.class);
            query.select(from);
            query.where(builder.equal(from.get("weekDay"), weekDay));
            return session.createQuery(query).list();
        });
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        PropTimeRange range = list.get(0);
        PROP_TIME_RANGE_CACHE.put(weekDay, range);
        return range;
    }

    public static List<PropTimeRange> getPropTimeRangeList() {
        List<PropTimeRange> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PropTimeRange> query = builder.createQuery(PropTimeRange.class);
            JpaRoot<PropTimeRange> from = query.from(PropTimeRange.class);
            query.select(from);
            return session.createQuery(query).list();
        });
        return list;
    }
}
