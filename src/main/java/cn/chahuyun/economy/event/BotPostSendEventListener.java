package cn.chahuyun.economy.event;


import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.manager.RodeoManager;

import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessagePostSendEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BotPostSendEventListener extends SimpleListenerHost {
    @EventHandler()
    public void onMessage(@NotNull MessagePostSendEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        // [轮盘]
        // [决斗]
        
        if(!(code.startsWith("[轮盘]") || code.startsWith("[决斗]"))){
            return;
        }
        List<Long> atUser = new ArrayList<>();
        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                atUser.add(at.getTarget());
            }
        }
        Rodeo redeo = RodeoManager.getCurrent(event.getTarget().getId(), atUser);
        if(Objects.isNull(redeo)){

        }

        // 查询当前所在的赛程

        // 决斗
        // [mirai:at:294253294] 😙了一口[mirai:at:952746839] 的【身体】，让对方被冲昏了1分40秒头脑。恭喜[mirai:at:294253294] 获得一分！
        // [mirai:at:952746839] 😙了一口[mirai:at:294253294] 的【脸蛋👧】，让对方被冲昏了3分0秒头脑。恭喜[mirai:at:952746839] 获得一分！
        // <target-win> 😙了一口<target-lose> 的【<position>】，让对方被冲昏了<mute-f>头脑。恭喜<target-win> 获得一分！

        // 轮盘
        // <target> 开了一枪，枪没响，还剩<remain-chamber>轮，幸运之神暂时眷顾于此。恭喜<target>获得一分！
        // <target> 开了一枪🔫，枪响了，被冲昏了<mute-f>头脑，并爽快地输掉了这局比赛。

        //


        System.out.println(code);
    }

//    private void messagePostSendEventListener() {
////        logger.info("MessagePostSendEventListener Running");
////        GlobalEventChannel.INSTANCE.subscribeAlways(MessagePostSendEvent.class, event -> {
////            logger.info("MessagePostSendEvent...");
////            // 这里新构造了一个带source的chain，存储到数据库中
////            MessageChain chain = Objects.requireNonNull(event.getReceipt()).getSource().plus(event.getMessage());
////            if (!chainService.insertMessageChain(new MessageChainData(chain)))
////                logger.warn("存储失败: " + chain.contentToString());
////        });
////    }
}
