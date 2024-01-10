package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.badge.BadgeInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.PlainText;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;
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
        List<UserBackpack> userBackpacks = listBackPackByFishType(offLinePropList);
        userBackpacks.stream().forEach(UserBackpack::remove);
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
}
