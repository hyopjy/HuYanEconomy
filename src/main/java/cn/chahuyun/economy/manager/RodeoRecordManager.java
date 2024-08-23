package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.boss.WorldBossUserLog;
import cn.chahuyun.economy.entity.rodeo.RodeoRecord;
import cn.chahuyun.economy.utils.HibernateUtil;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.Objects;

public class RodeoRecordManager {
    public static List<RodeoRecord> getRecordsByRodeoId(Long rodeoId) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<RodeoRecord> query = builder.createQuery(RodeoRecord.class);
            JpaRoot<RodeoRecord> from = query.from(RodeoRecord.class);
            query.where(builder.equal(from.get("rodeoId"), rodeoId));
            query.select(from);
            return session.createQuery(query).list();
        });
    }
}
