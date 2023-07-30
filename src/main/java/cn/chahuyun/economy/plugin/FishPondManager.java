package cn.chahuyun.economy.plugin;

import cn.chahuyun.economy.entity.fish.Fish;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.event.events.UserMessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;


import java.util.List;
import java.util.Optional;

public class FishPondManager {
    public static void refresh(UserMessageEvent event) {

        List<Fish> fishList = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Fish> query = builder.createQuery(Fish.class);
            query.select(query.from(Fish.class));
            return session.createQuery(query).list();
        });

        int level = 6;
        if(Optional.of(fishList.stream().mapToInt(Fish::getLevel)).get().max().isPresent()){
            level = Optional.of(fishList.stream().mapToInt(Fish::getLevel)).get().max().getAsInt();
        }
        int finalLevel = level;
        Boolean status = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<FishPond> query = builder.createQuery(FishPond.class);
            JpaRoot<FishPond> from = query.from(FishPond.class);
            query.select(from);
            List<FishPond> list;
            try {
                list = session.createQuery(query).list();
            } catch (Exception e) {
                return false;
            }
            for (FishPond fishPond : list) {
                fishPond.setPondLevel(finalLevel);
                fishPond.setRebate(0.13);
                fishPond.setDescription("「纯天然湖泊，鱼情优秀，又大又多」，但据内部人士爆料，这是黑心土著挖的人工湖");
                session.merge(fishPond);
            }
            return true;
        });
        if (status) {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"鱼塘重置完成!"));
        } else {
            event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"鱼塘重置完成!"));
        }
    }
}
