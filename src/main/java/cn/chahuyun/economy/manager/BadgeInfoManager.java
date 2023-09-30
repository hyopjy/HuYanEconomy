package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.constant.FishSignConstant;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.badge.BadgeInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;
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

    public static BadgeInfo getBadgeInfo(long groupId, long qq, String signCode) {
       return HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<BadgeInfo> query = builder.createQuery(BadgeInfo.class);
                JpaRoot<BadgeInfo> from = query.from(BadgeInfo.class);
                query.select(from);
                query.where(builder.equal(from.get("groupId"), groupId), builder.equal(from.get("qq"), qq), builder.equal(from.get("propCode"), signCode));
                return session.createQuery(query).uniqueResult();
            });
    }
    public static void updateOrInsertBadgeInfo(long groupId, long qq, String signCode, LocalDateTime time, String content) {
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
                badgeInfo.setContent(content);
                // return badgeInfo;
                badgeInfo.save();
                return;
            }
            BadgeInfo newBadgeInfo = BadgeInfo.getBadgeInfo(groupId, qq, signCode, time,content);
            newBadgeInfo.save();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

    }

    /**
     * 设置特殊成就
     * @param event
     */
    public static void setSpecialAchievements(MessageEvent event) {
        Contact subject = event.getSubject();
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());

        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();

        String[] s = code.split(" ");
        long qq = 0;
        String specialAchievements;
        if (s.length == 2) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
            specialAchievements = s[s.length - 1];
        } else {
            qq = Long.parseLong(s[1]);
            specialAchievements = s[2];
        }

        if (StrUtil.isBlankIfStr(specialAchievements)) {
            subject.sendMessage("请输入特殊成就");
            return;
        }

        BadgeInfoManager.updateOrInsertBadgeInfo(subject.getId(), qq, FishSignConstant.FISH_SPECIAL, null, specialAchievements);

        event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "设置成功"));
    }
}
