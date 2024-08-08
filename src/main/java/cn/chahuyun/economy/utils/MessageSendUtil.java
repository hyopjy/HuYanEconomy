package cn.chahuyun.economy.utils;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;

public class MessageSendUtil {

    public static void sendGroupMessage(Group group, Message message) {
        // 发送消息
        try {
            group.sendMessage(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
