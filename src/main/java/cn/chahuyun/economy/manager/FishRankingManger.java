package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.fish.FishRanking;
import cn.chahuyun.economy.utils.HibernateUtil;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

public class FishRankingManger {

    public static void clearFishRanking() {
        // https://www.coder.work/article/403891
        // https://vimsky.com/examples/detail/java-method-javax.persistence.criteria.CriteriaBuilder.createCriteriaDelete.html
         HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaDelete<FishRanking> query = builder.createCriteriaDelete(FishRanking.class);
            query.from(FishRanking.class);
            return session.createQuery(query).executeUpdate();
        });
    }
}
