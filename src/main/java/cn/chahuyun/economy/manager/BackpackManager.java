package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.Query;

/**
 * 背包管理
 *
 * @author Moyuyanli
 * @date 2022/11/15 10:00
 */
public class BackpackManager {
    /**
     * 清理钓鱼机器
     */

    public static void clearFishMachine(MessageEvent event) {
        int count = HibernateUtil.factory.fromTransaction(session -> {
            String rankhql = "delete from UserBackpack where propsCode = 'FISH-27'";
            Query rankQuery = session.createQuery(rankhql);
            int back27 = rankQuery.executeUpdate();
            Log.info("清理钓鱼机数量"+back27 );
            return back27;
        });
        event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"清理钓鱼机数量:%s",count));
    }
}
