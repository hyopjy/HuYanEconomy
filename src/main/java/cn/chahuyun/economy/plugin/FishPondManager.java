package cn.chahuyun.economy.plugin;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.fish.FishPond;
import cn.chahuyun.economy.utils.HibernateUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.UserMessageEvent;


import java.util.List;

public class FishPondManager {
    public static void refresh(UserMessageEvent event) {
        List<Long> fishGroup = EconomyConfig.INSTANCE.getFishGroup();
        fishGroup.forEach(groupId -> {
            Group botGroup = event.getBot().getGroup(groupId);
            //注册新鱼塘
            FishPond finalFishPond = new FishPond(1, groupId, HuYanEconomy.config.getOwner(), botGroup.getName() + "鱼塘", "「纯天然湖泊，鱼情优秀，又大又多」，但据内部人士爆料，这是黑心土著挖的人工湖");
            HibernateUtil.factory.fromTransaction(sessionUpdate -> sessionUpdate.merge(finalFishPond));
        });
    }
}
