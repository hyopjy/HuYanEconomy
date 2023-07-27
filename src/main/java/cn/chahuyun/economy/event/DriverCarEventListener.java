package cn.chahuyun.economy.event;

import cn.chahuyun.economy.utils.CommandOperator;
import cn.chahuyun.economy.utils.Log;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MessageTooLargeException;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;

public class DriverCarEventListener extends SimpleListenerHost {
    CommandOperator operator = new CommandOperator();
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        if (exception instanceof EventCancelledException) {
            Log.error("发送消息被取消:", exception);
        } else if (exception instanceof BotIsBeingMutedException) {
            Log.error("你的机器人被禁言:", exception);
        } else if (exception instanceof MessageTooLargeException) {
            Log.error("发送消息过长:", exception);
        } else if (exception instanceof IllegalArgumentException) {
            Log.error("发送消息为空:", exception);
        }

        // 处理事件处理时抛出的异常
        Log.error(exception);
    }

    @EventHandler()
    public synchronized ListeningStatus onGroupMessage(GroupMessageEvent event) {
        Member sender = event.getSender();
        Group group = event.getGroup();
        String message = event.getMessage().serializeToMiraiCode();
        if (message.startsWith("开车")) {
            Message m;
            if (message.contains("?") || message.contains("帮助") || message.contains("help")) {
                m = operator.handleGetHelp(message, group, sender.getId());
            } else {
                m = operator.handleToCar(message, group, sender.getId());
            }
            if (m != null) group.sendMessage(m);
        }

        if (message.startsWith("上车")) {
            Message m = operator.handleToOnCar(message, group, sender.getId());
            if (m != null) group.sendMessage(m);
        }

        if (message.startsWith("散车")) {
            Message m = operator.handleToRemoveCar(message, group, sender.getId());
            if (m != null) group.sendMessage(m);
        }
        if (message.contains("查询开车列表")) {
            Message m = operator.handleQueryCar(message, group, sender.getId());
            if (m != null) group.sendMessage(m);
        }

        return ListeningStatus.LISTENING;
    }
}
