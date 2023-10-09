package cn.chahuyun.economy.manager;


import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorldBossOpenTask implements Task {
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
        groupIdList.forEach(groupId->{
            bot.getGroup(groupId);
            Message message = new PlainText("钓鱼佬，你掉的是这个[道具1]，还是这个[道具2]？上钩[目标]斤鱼再告诉你！\r\n" +
                    "\r\n" +
                    "\uD83D\uDCE2BOSS战\uD83E\uDD96开启！今日击败世界boss即可获取[奖励金额]WDIT币币\uD83E\uDE99\r\n" +
                    "\r\n" +
                    "[什么等级的鱼竿也想钓我？]\r\n" +
                    "　　/\r\n" +
                    "(ˇωˇ ﾐэ)Э三三三三　乚").plus("\r\n");
            Objects.requireNonNull(bot.getGroup(groupId)).sendMessage(message);
        });

    }
}
