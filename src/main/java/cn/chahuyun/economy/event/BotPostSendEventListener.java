package cn.chahuyun.economy.event;


import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessagePostSendEvent;
import org.jetbrains.annotations.NotNull;

public class BotPostSendEventListener extends SimpleListenerHost {
    @EventHandler()
    public void onMessage(@NotNull MessagePostSendEvent event) {

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
