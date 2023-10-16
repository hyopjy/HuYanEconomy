package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.TimeRange;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.team.Team;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 组队管理
 */
public class TeamManager {

    private static final Integer TEAM_STATUS_NO = 0;
    private static final Integer TEAM_STATUS_OK = 1;

    /**
     * 开启组队
     * @param event
     */
    public static void createTeam(MessageEvent event) {
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        // 判断当前用户是否组队或者已组队
        Long senderId = event.getSender().getId();
        if(checkUserInTeam(senderId)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "已加入队伍 无法组队！"));
            return;
        }
        String code = message.serializeToMiraiCode();
        String[] s = code.split(" ");
        long qq = 0;
        String teamName;
        if (s.length == 2) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
            teamName = s[s.length - 1];
        } else {
            qq = Long.parseLong(s[1]);
            teamName = s[2];
        }

        // 被艾特用户是否已经存在team
        if(checkUserInTeam(qq)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "邀请用户已加入队伍 无法组队！"));
            return;
        }

        if (StrUtil.isBlankIfStr(teamName)) {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入队伍名称"));
            return;
        }

        List<Long> teamMember = new ArrayList<>(1);
        teamMember.add(senderId);
        teamMember.add(qq);
        Team team = new Team(teamName, senderId, JSONUtil.toJsonStr(teamMember), null, TEAM_STATUS_NO);
        team.save();
    }

    private static boolean checkUserInTeam(Long qq) {
        List<Team> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Team> query = builder.createQuery(Team.class);
            JpaRoot<Team> from = query.from(Team.class);
            query.select(from);
            return session.createQuery(query).list();
        });
      return CollectionUtils.isNotEmpty(list.stream().filter(t-> t.getTeamOwner().equals(qq) || t.getTeamMember().contains(qq+""))
                .collect(Collectors.toList()));
    }

    private static Team getTeamOwnerInfo(Long qq) {
        List<Team> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Team> query = builder.createQuery(Team.class);
            JpaRoot<Team> from = query.from(Team.class);
            query.select(from);
            query.where(builder.equal(from.get("teamOwner"), qq));
            return session.createQuery(query).list();
        });

        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    /**
     * 确认组队
     *
     * @param event
     */
    public static void joinTeam(MessageEvent event) {
        Long senderId = event.getSender().getId();
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();
        String[] s = code.split(" ");
        long qq = 0;
        if (s.length == 2) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
        } else {
            qq = Long.parseLong(s[1]);
        }
        // 查询被艾特用户的team信息
        Team team = getTeamOwnerInfo(qq);
        if(Objects.isNull(team)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "被艾特用户没有队伍 无法加入"));
            return;
        }
        // 判断是否是被指定的
        if(!team.getTeamMember().contains(senderId+"")){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "不是当前队伍所指定的用户 无法加入"));
            return;
        }
        team.setTeamStatus(TEAM_STATUS_OK);
        team.save();


        // subject.sendMessage();


    }
}
