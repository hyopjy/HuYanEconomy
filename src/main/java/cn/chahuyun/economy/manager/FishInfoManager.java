package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.dto.BadgeFishInfoDto;
import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * 钓鱼用户管理
 */
public class FishInfoManager {

    private static List<FishInfo> getFishInfoList(){
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<FishInfo> query = builder.createQuery(FishInfo.class);
            JpaRoot<FishInfo> from = query.from(FishInfo.class);
            query.select(from);
            return session.createQuery(query).list();
        });
    }

    public static List<BadgeFishInfoDto> getBadgeFishInfoDto() {
        List<BadgeFishInfoDto> badgeFishInfoDtos = new ArrayList<>();
        List<FishInfo> fishInfos = getFishInfoList();
        fishInfos.stream().forEach(fishInfo->{
            if(!fishInfo.getDefaultFishPond().startsWith("g-")){
                return;
            }
            if(fishInfo.getRodLevel() < SeasonCommonInfoManager.getBadgeFishRodLevel()){
                return;
            }
            String fishPodStr = fishInfo.getDefaultFishPond().substring(2);
            Long groupId = Long.parseLong(fishPodStr);

            BadgeFishInfoDto dto = BadgeFishInfoDto.builder()
                    .groupId(groupId)
                    .qq(fishInfo.getQq())
                    .build();

            badgeFishInfoDtos.add(dto);
        });
        return badgeFishInfoDtos;
    }
}
