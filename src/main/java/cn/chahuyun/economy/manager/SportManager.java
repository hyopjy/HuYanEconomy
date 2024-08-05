package cn.chahuyun.economy.manager;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events. MessagePostSendEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Objects;

@Slf4j
public class SportManager {

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
