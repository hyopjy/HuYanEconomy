package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MysteriousMerchantEndTask implements Task {

    MysteriousMerchantSetting setting;

    private Integer hour;

    public MysteriousMerchantEndTask(MysteriousMerchantSetting setting, Integer hour) {
        this.setting = setting;
        this.hour = hour;
    }

    // 结束时-删除商品信息
    @Override
    public void execute() {
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("WorldBossOpenTask-end. bot为空");
            return;
        }

        List<Long> groupIdList = new ArrayList<>();
        groupIdList.add(758085692L);
        groupIdList.add(835186488L);
        groupIdList.add(878074795L);
        groupIdList.add(227265762L);
        groupIdList.forEach(groupId->{
            Group group =  bot.getGroup(groupId);
            if(Objects.isNull(group)){
                return;
            }
            taskRunByEndGroupId(groupId, bot, group);
        });
    }

    private void taskRunByEndGroupId(Long groupId, Bot bot, Group group) {
    }
}
