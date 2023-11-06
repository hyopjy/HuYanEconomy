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
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.time.LocalDateTime;
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
        if(checkUserInTeam(senderId, subject.getId())){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "已加入队伍 无法组队！"));
            return;
        }
        String code = message.serializeToMiraiCode();
        String[] s = code.split(" ");
        if(s.length<2){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入队伍名！"));
            return;
        }
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
        if(checkUserInTeam(qq, subject.getId())){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "邀请用户已加入队伍 无法组队！"));
            return;
        }

        if (StrUtil.isBlankIfStr(teamName)) {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请输入队伍名称"));
            return;
        }

        Team team = new Team(teamName, senderId, qq, null, TEAM_STATUS_NO, subject.getId());
        team.save();
    }

    private static boolean checkUserInTeam(Long qq, Long groupId) {
        List<Team> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Team> query = builder.createQuery(Team.class);
            JpaRoot<Team> from = query.from(Team.class);
            query.select(from);
            query.where(builder.equal(from.get("groupId"), groupId));
            return session.createQuery(query).list();
        });
      return CollectionUtils.isNotEmpty(list.stream()
              .filter(t-> t.getTeamOwner().equals(qq) || t.getTeamMember().equals(qq)).collect(Collectors.toList()));
    }

    private static Team getTeamOwnerInfo(Long qq,Long groupId) {
        List<Team> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Team> query = builder.createQuery(Team.class);
            JpaRoot<Team> from = query.from(Team.class);
            query.select(from);
            query.where(builder.equal(from.get("teamOwner"), qq), builder.equal(from.get("groupId"), groupId));
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
        if (s.length == 1) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "格式有误"));
            return;
        }
        // 查询被艾特用户的team信息
        Team team = getTeamOwnerInfo(qq, subject.getId());
        if(Objects.isNull(team)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "被艾特用户没有队伍 无法加入"));
            return;
        }
        // 判断是否是被指定的
        if(!team.getTeamMember().equals(senderId)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "不是当前队伍所指定的用户 无法加入"));
            return;
        }
        if(team.getTeamStatus().equals(TEAM_STATUS_OK)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "队伍已组建成功！无需重复加入"));
            return;
        }
        team.setTeamStatus(TEAM_STATUS_OK);
        team.setSuccessTime(LocalDateTime.now());
        team.save();

        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }

        Message sss = new PlainText("["+team.getTeamName()+"]组队成功").plus("\r\n")
                .plus(new At(team.getTeamOwner()).getDisplay(group)).plus("✖\uFE0F").plus(new At(team.getTeamMember()).getDisplay(group)).plus("\r\n");
        subject.sendMessage(sss);
    }

    public static void list(MessageEvent event) {
        Contact subject = event.getSubject();
        Long groupId = subject.getId();
        List<Team> list = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Team> query = builder.createQuery(Team.class);
            JpaRoot<Team> from = query.from(Team.class);
            query.select(from);
            query.where(builder.equal(from.get("groupId"), groupId));
            return session.createQuery(query).list();
        });
        Group group = (Group) subject;
        Message sss = new PlainText("当前队伍信息有：").plus("\r\n");
        StringBuilder sb =new StringBuilder();
        list.stream().forEach(t->{
            String teamStatus = t.getTeamStatus().equals(TEAM_STATUS_OK) ? " [已组队]" : " [待确认]";
            sb.append("["+t.getTeamName() +"]").append(new At(t.getTeamOwner()).getDisplay(group)).append("✖\uFE0F")
                    .append(new At(t.getTeamMember()).getDisplay(group)).append(teamStatus).append("\r\n");
        });
        sss = sss.plus(sb.toString());
        subject.sendMessage(sss);
    }
    public static List<Team> listTeam(Long groupId) {
        return HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Team> query = builder.createQuery(Team.class);
            JpaRoot<Team> from = query.from(Team.class);
            query.select(from);
            query.where(builder.equal(from.get("groupId"), groupId), builder.equal(from.get("teamStatus"), TEAM_STATUS_OK));
            return session.createQuery(query).list();
        });
    }

    /**
     * 解散
     * @param event
     */
    public static void deleteTeam(MessageEvent event) {
        Long senderId = event.getSender().getId();
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();
        String[] s = code.split(" ");
        long qq = 0;
        if (s.length == 1) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "格式有误"));
            return;
        }
        // 当前发送人是否是队伍组建人员
        Team team = getTeamOwnerInfo(senderId, subject.getId());
        if(Objects.isNull(team)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "你不是队伍组建人,无法解散！"));
            return;
        }
        // 判断被艾特用户是否是队伍人员
        if(!team.getTeamMember().equals(qq)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "不是当前队伍所指定的用户，无法解散"));
            return;
        }

        if(team.getTeamStatus().equals(TEAM_STATUS_NO)){
            team.remove();
            subject.sendMessage(MessageUtil.formatMessageChain(message, "队伍解散成功"));
            return;
        }else{
            // 判断队伍创建时间是否超过48小时
            LocalDateTime successTime = team.getSuccessTime();
            if(Objects.isNull(successTime)){
                team.setSuccessTime(LocalDateTime.now());
                team.save();
                subject.sendMessage(MessageUtil.formatMessageChain(message, "队伍缺失创建时间,已自动补全，请48小时后进行解散"));
                return;
            }
            // 创建时间在
            LocalDateTime twoDayAfter = successTime.plusDays(2L);
            if(twoDayAfter.isAfter(LocalDateTime.now())){
                subject.sendMessage(MessageUtil.formatMessageChain(message, "请48小时后发起解散"));
                return;
            }
            team.setTeamStatus(TEAM_STATUS_NO);
            team.save();
        }
    }

    /**
     * 确认解散
     * @param event
     */
    public static void leveTeam(MessageEvent event) {
        Long senderId = event.getSender().getId();
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();
        String[] s = code.split(" ");
        long qq = 0;
        if (s.length == 1) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(message, "格式有误"));
            return;
        }
        // 当前被艾特用户是否是队长
        Team team = getTeamOwnerInfo(qq, subject.getId());
        if(Objects.isNull(team)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "被艾特用户没有队伍、无法解散"));
            return;
        }
        // 判断被艾特用户是否是队伍人员
        if(!team.getTeamMember().equals(senderId)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "你不是当前队伍成员"));
            return;
        }
        if(team.getTeamStatus().equals(TEAM_STATUS_OK)){
            subject.sendMessage(MessageUtil.formatMessageChain(message, "请队伍创建人先发起解散"));
            return;
        }else {
            team.remove();
            subject.sendMessage(MessageUtil.formatMessageChain(message, "队伍解散成功"));
            return;
        }
    }
}
