package cn.chahuyun.economy.manager;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events. MessagePostSendEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Objects;

@Slf4j
public class SportManager {

    /**
     * 就是你只需要给我一个能够配置日期、时间段、选手、局数、玩法（决斗或轮盘）就可以了。
     * 然后根据配置的结果，在指定时间内指定的选手发送的指令是有效的，然后根据规则展示结果就行
     *
     *
     * ：在［配置的时间段］内，［配置的两个用户］获得了［配置局数］的游戏次数。
     * 只要在这个配置时间段内，这两个用户就可以开N场决斗/轮盘
     *
     东风吹，战鼓擂，决斗场上怕过谁！
     新的🏟️[比赛场次名]已确定于[14:00-17:00]开战！
     [@A ]与[@B ]正式展开[决斗/轮盘]的巅峰对决！⚔️[N]局比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？

     [比赛场次名]结束，@A与对手@B的比分为[1:1]🤺

     这个是当设置的局数为2时，结果的展示（2局比赛是小组赛的特殊情况，只得积分，不分输赢），替代上面那个[比赛场次名]结束，恭喜胜者

     *
     */
    private void messagePostSendEventListener() {
//        MessagePostSendEvent.receipt.source


//        log.info("MessagePostSendEventListener Running");
//        GlobalEventChannel.INSTANCE.subscribeAlways(MessagePostSendEvent.class, event -> {
//            log.info("MessagePostSendEvent...");
//            // 这里新构造了一个带source的chain，存储到数据库中
//            MessageChain chain = Objects.requireNonNull(event.getReceipt()).getSource().plus(event.getMessage());
//            if (!chainService.insertMessageChain(new MessageChainData(chain)))
//                logger.warn("存储失败: " + chain.contentToString());
//        });
    }
}
