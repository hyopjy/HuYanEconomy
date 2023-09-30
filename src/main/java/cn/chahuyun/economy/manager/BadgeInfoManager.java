package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.badge.BadgeInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDateTime;
import java.util.List;

public class BadgeInfoManager {

    public static int getCount(Long groupId, Long qq, String propCode) {
        Long count = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<BadgeInfo> query = builder.createQuery(BadgeInfo.class);
            JpaRoot<BadgeInfo> from = query.from(BadgeInfo.class);
            query.select(from);
            query.where(builder.equal(from.get("groupId"), groupId), builder.equal(from.get("qq"), qq),
                    builder.equal(from.get("propCode"), propCode));
            return session.createQuery(query).stream().count();
        });
        return Math.toIntExact(count);
    }

    public static List<BadgeInfo> getBadgeList() {
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<BadgeInfo> query = builder.createQuery(BadgeInfo.class);
            JpaRoot<BadgeInfo> from = query.from(BadgeInfo.class);
            query.select(from);
            return session.createQuery(query).list();
        });
    }

    public static void updateOrInsertBadgeInfo(long groupId, long qq, String signCode, LocalDateTime time) {
        BadgeInfo badgeInfo;
        try {
            badgeInfo = HibernateUtil.factory.fromSession(session -> {
//                session.get(BadgeInfo.class, groupId)
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<BadgeInfo> query = builder.createQuery(BadgeInfo.class);
                JpaRoot<BadgeInfo> from = query.from(BadgeInfo.class);
                query.select(from);
                query.where(builder.equal(from.get("groupId"), groupId), builder.equal(from.get("qq"), qq),
                        builder.equal(from.get("propCode"), signCode));
                return session.createQuery(query).uniqueResult();
            });
            if (badgeInfo != null) {
                badgeInfo.setCount(badgeInfo.getCount() + 1);
                // return badgeInfo;
                badgeInfo.save();
                return;
            }
            BadgeInfo newBadgeInfo = BadgeInfo.getBadgeInfo(groupId, qq, signCode, time);
            newBadgeInfo.save();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

    }
}
