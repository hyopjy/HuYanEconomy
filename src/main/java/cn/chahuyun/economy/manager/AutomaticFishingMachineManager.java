package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.command.AutomaticFishTask;
import cn.chahuyun.economy.entity.fish.AutomaticFishUser;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.cron.CronUtil;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDateTime;
import java.util.List;

public class AutomaticFishingMachineManager {

    public static void init() {
        // 查询数据库
        List<AutomaticFishUser> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<AutomaticFishUser> query = builder.createQuery(AutomaticFishUser.class);
            JpaRoot<AutomaticFishUser> from = query.from(AutomaticFishUser.class);
            query.select(from);
            return session.createQuery(query).list();
        });
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            AutomaticFishUser automaticFishUser = list.get(i);
            if(!LocalDateTime.now().isBefore(automaticFishUser.getEndTime())){
                automaticFishUser.remove();
                continue;
            }

            Long groupId = automaticFishUser.getGroupId();
            Long userId = automaticFishUser.getFishUser();
            CacheUtils.addAutomaticFishBuff(groupId, userId, AutomaticFishTask.getAutomaticFishTaskId(groupId, userId));
            String autoTaskId =  AutomaticFishTask.getAutomaticFishTaskId(groupId, userId);
            CronUtil.remove(autoTaskId);
            AutomaticFishTask minutesTask = new AutomaticFishTask(autoTaskId, automaticFishUser.getEndTime(), groupId, userId);
            // CronUtil.setMatchSecond(true);
            CronUtil.schedule(autoTaskId, automaticFishUser.getCron(), minutesTask);
        }
    }
}
