package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.HibernateUtil;
import jakarta.persistence.criteria.Root;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.UserMessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    public static void clearOffLineProp() {
        List<String> offLinePropList = getOfflinePropCodeList();
//        List<UserBackpack> userBackpacks = listBackPackByFishType(offLinePropList);
//        userBackpacks.stream().forEach(UserBackpack::remove);
        deleteUserPackByCode(offLinePropList);
    }

    private static Integer deleteUserPackByCode(List<String> offLinePropList) {
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaDelete<UserBackpack> delete = builder.createCriteriaDelete(UserBackpack.class);
            Root<UserBackpack> e = delete.from(UserBackpack.class);
            delete.where(builder.in(e.get("propsCode")).value(offLinePropList));
            return session.createQuery(delete).executeUpdate();
        });
    }


    private static List<String> getOfflinePropCodeList() {
        List<String> cardList = new ArrayList<>();
        Set<String> strings = PropsType.getProps().keySet();
        for (String string : strings) {
            if (string.startsWith("FISH-")) {
                if(PropsType.getPropsInfo(string) instanceof PropsFishCard){
                    PropsFishCard propsFishCard =(PropsFishCard)PropsType.getPropsInfo(string);
                    if(propsFishCard.getOffShelf()){
                        cardList.add(string);
                    }
                }
            }
        }
        return cardList;
    }


    public static List<UserBackpack> listBackPackByFishType(List<String> propCode) {
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<UserBackpack> query = builder.createQuery(UserBackpack.class);
            JpaRoot<UserBackpack> from = query.from(UserBackpack.class);
            query.select(from);
            query.where(builder.in(from.get("propsCode"), propCode));
            return session.createQuery(query).list();
        });
    }

    public static void updateUserPropForGroup(UserMessageEvent event, Long groupId, Long qq, String prop, Integer num, String type) {
        Contact subject = event.getSubject();

        // 获取机器人所在群聊
        Group group = event.getBot().getGroup(groupId);
        if(Objects.isNull(group)){
            subject.sendMessage("机器人暂未加入该群聊：" + groupId);
            return;
        }
        // 道具
        String propCode = PropsType.getCode(prop);
        if (Objects.isNull(propCode)) {
            subject.sendMessage("道具为空：" + prop);
            return;
        }
        // 指定用户
        NormalMember member = group.get(qq);
        if(Objects.isNull(member)){
            subject.sendMessage("指定用户不存在：" + qq);
            return;
        }
        UserInfo userInfo = UserManager.getUserInfo(member);
        PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(propCode);
        int number = num;
        while (number != 0) {
            if("ap".equals(type)){
                PluginManager.getPropsManager().addProp(userInfo, propsBase);
            }
            if("rp".equals(type)){
                PluginManager.getPropsManager().deleteProp(userInfo, propsBase);
            }
            number--;
        }
        String typeStr = "rp".equals(type) ? "删除" : "添加";
        subject.sendMessage(typeStr + " 道具成功");
    }

}
