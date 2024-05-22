package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.dto.BadgeFishInfoDto;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.fish.FishInfo;
import cn.chahuyun.economy.utils.HibernateUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.UserMessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

            // 更新鱼竿等级
            fishInfo.setRodLevel(SeasonCommonInfoManager.getBadgeFishRodLevel());
            fishInfo.save();
        });
        return badgeFishInfoDtos;
    }

    public static void updateUserRodLevel(UserMessageEvent event, Long groupId, Long qq, Integer rodLevel) {
        Contact subject = event.getSubject();
        // 获取机器人所在群聊
        Group group = event.getBot().getGroup(groupId);
        if(Objects.isNull(group)){
            subject.sendMessage("机器人暂未加入该群聊：" + groupId);
            return;
        }
        // 指定用户
        NormalMember member = group.get(qq);
        if(Objects.isNull(member)){
            subject.sendMessage("指定用户不存在：" + qq);
            return;
        }
        UserInfo userInfo = UserManager.getUserInfo(member);
        // 更新指定鱼竿等级
        FishInfo fishInfo = userInfo.getFishInfo();
        fishInfo.setRodLevel(rodLevel);
        fishInfo.save();
        subject.sendMessage("更新 " + groupId + "用户" + qq +   "鱼竿等级" + fishInfo.getRodLevel());

    }
}
