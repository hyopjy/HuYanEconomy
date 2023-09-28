package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.badge.BadgeInfo;
import cn.chahuyun.economy.entity.badge.CompetitionSeason;
import cn.chahuyun.economy.utils.DateUtil;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.pattern.CronPattern;
import cn.hutool.cron.task.Task;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import xyz.cssxsh.mirai.economy.service.EconomyAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.chahuyun.economy.manager.CompetitionSeasonManager.initCompetitionSeason;

/**
 * 赛季管理
 */
public class CompetitionSeasonManager {

    public static CompetitionSeason initCompetitionSeason(){
        CompetitionSeason competitionSeason;
        try {
            competitionSeason = HibernateUtil.factory.fromSession(session -> {
//                session.get(BadgeInfo.class, groupId)
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<CompetitionSeason> query = builder.createQuery(CompetitionSeason.class);
                JpaRoot<CompetitionSeason> from = query.from(CompetitionSeason.class);
                query.select(from);
                return session.createQuery(query).uniqueResult();
            });
            if (competitionSeason != null) {
                 return competitionSeason;
            }
        } catch (Exception ignored) {
        }
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMonths(3L);
        String cron = DateUtil.getCron(end);
        CompetitionSeason newBadgeInfo = new CompetitionSeason(1L, start, end, cron);
        return newBadgeInfo.save();
    }

    public static void seasonInit(){
        CompetitionSeason competitionSeason = initCompetitionSeason();
        CronUtil.remove("competition-season");
        CompetitionSeasonTask task = new CompetitionSeasonTask("competition-season");
        CronUtil.schedule("competition-season", competitionSeason.getCron(), task);
    }
}


class CompetitionSeasonTask implements Task {

    private final String id;

    public CompetitionSeasonTask(String id) {
        this.id = id;
    }
    /**
     * 执行作业
     * <p>
     * 作业的具体实现需考虑异常情况，默认情况下任务异常在监听中统一监听处理，如果不加入监听，异常会被忽略<br>
     * 因此最好自行捕获异常后处理
     */
    @Override
    public void execute() {
        CompetitionSeason competitionSeason = initCompetitionSeason();
        if(!LocalDateTime.now().isAfter(competitionSeason.getEndTime())){
            return;
        }
        // 清理枫叶
        Map<EconomyAccount, Double> accountByBank = EconomyUtil.getAccountByBank();
        for (Map.Entry<EconomyAccount, Double> entry : accountByBank.entrySet()) {
            UserInfo userInfo = UserManager.getUserInfo(entry.getKey());
            if (userInfo == null) {
                continue;
            }

            if (EconomyUtil.minusMoneyToBankEconomyAccount(entry.getKey(),EconomyUtil.getMoneyByBankEconomyAccount(entry.getKey()))) {
                Log.error("枫叶管理:清理成功");
            } else {
                Log.error("枫叶管理:清零失败");
            }
        }
        // 清理道具信息
        List<BadgeInfo> badgeInfoList = BadgeInfoManager.getBadgeList();
        badgeInfoList.stream().filter(badgeInfo -> Objects.nonNull(badgeInfo.getTime())
                && !LocalDateTime.now().isAfter(badgeInfo.getTime()))
                .forEach(BadgeInfo::remove);


        // 更新赛季
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMonths(3L);
        competitionSeason.setStartTime(start);
        competitionSeason.setEndTime(end);
        competitionSeason.setCron(DateUtil.getCron(end));
        competitionSeason.save();
        // 更新执行规则
        CronUtil.updatePattern(id, CronPattern.of(competitionSeason.getCron()));
    }
}