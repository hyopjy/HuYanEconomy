package cn.chahuyun.economy.event;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.RandomUtil;
import lombok.*;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 步骤3：实现监听器接口
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SisterDogListenerImpl implements SisterDogListener {
  //  private MessageEvent event;

    private Long groupId;

//    private Contact subject;

    private LocalDateTime expireDate;

    private List<Long> userIdList;

    private Long sourceId;

    private Integer n;

    @Override
    public void onCheckSisterDogMember(SisterDogEvent sisterDogEvent) {
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(bot == null){
            Log.info("[狗的姐姐]-获取bot为空：" + groupId);
            return;
        }
        Group group = bot.getGroup(groupId);
        if (Objects.isNull(group)) {
            Log.info("[狗的姐姐]-获取群组为空：" + groupId);
            return;
        }
        if(!sisterDogEvent.getGroupId().equals(groupId)){
            return;
        }

//        if(sisterDogEvent.getUserId().equals(sourceId)){
//            return;
//        }
        Long userId = sisterDogEvent.getUserId();
        userIdList.add(userId);
        if(!LocalDateTime.now().isBefore(expireDate)){
            // 获取最后一个发言人
            findPersonToCheck(group, userIdList, sourceId);
            SisterDogCommand.removeClickListener(SisterDogCommand.getListenerKey(groupId, sourceId));
            return;
        }
        if(CollectionUtils.isNotEmpty(userIdList) && userIdList.size() >= n){
            findPersonToCheck(group, userIdList, sourceId);
            SisterDogCommand.removeClickListener(SisterDogCommand.getListenerKey(groupId, sourceId));
            return;
        }
    }

    public void findPersonToCheck(Group group, List<Long> userIdList, Long sourceId){
        Long lastUserId =  userIdList.stream().reduce((first, second) -> second).orElse(null);
        if(Objects.isNull(lastUserId)){
            return;
        }
        // 1200-8500
        int money = RandomUtil.randomInt(1200, 8501);
        // 减去目标用户
        EconomyUtil.minusMoneyToUser(group.get(lastUserId), money);

        // 自己获得
        User sender = group.get(sourceId);
        EconomyUtil.plusMoneyToUser(sender, money);

        Message m = new PlainText("[狗的姐姐使用成功]").plus("\r\n");;
        m = m.plus(new At(sourceId).plus("搭讪的姐姐选择了幸运数字" + n).plus("\r\n"));
        m = m.plus(new At(lastUserId).getDisplay(group)).plus("被姐姐成功俘获，ATM姬自愿交出了" + money + "币币").plus("\r\n");
        group.sendMessage(m);

        CacheUtils.addTimeCacheKey(group.getId(), lastUserId);
        RedisUtils.addDogSisterCount(group.getId(), sourceId);
    }
}
