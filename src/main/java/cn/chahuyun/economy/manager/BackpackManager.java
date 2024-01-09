package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.badge.BadgeInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;

/**
 * 背包管理
 *
 * @author Moyuyanli
 * @date 2022/11/15 10:00
 */
public class BackpackManager {
    /**
     * 清理背包鱼信息
     */

    public static void clearFishMachine(MessageEvent event) {
        int count = HibernateUtil.factory.fromTransaction(session -> {
            String rankhql = "delete from UserBackpack where propsCode = 'FISH-27'";
            Query rankQuery = session.createQuery(rankhql);
            int back27 = rankQuery.executeUpdate();
            Log.info("清理钓鱼机数量"+back27 );
            return back27;
        });
        event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"清理钓鱼机数量:%s",count));
    }

//    public static List<UserBackpack> listBackPackByUserIdAndFishType(String userId, String propCode) {
//        return HibernateUtil.factory.fromTransaction(session -> {
//            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
//            JpaCriteriaQuery<UserBackpack> query = builder.createQuery(UserBackpack.class);
//            JpaRoot<UserBackpack> from = query.from(UserBackpack.class);
//            query.select(from);
//            query.where(builder.equal(from.get("userId"), userId), builder.equal(from.get("propsCode"), propCode));
//            return session.createQuery(query).list();
//        });
//    }
}
