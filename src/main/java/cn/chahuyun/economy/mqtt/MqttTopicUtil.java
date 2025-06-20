package cn.chahuyun.economy.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * MQTT主题工具类，统一管理消息收发和回调
 */
public class MqttTopicUtil {
    // 主题 -> 回调
    private static final Map<String, BiConsumer<String, String>> topicCallbackMap = new ConcurrentHashMap<>();

    /**
     * 订阅主题并注册回调
     * @param topic 主题
     * @param callback 收到消息时的回调 (topic, message)
     */
    public static void subscribe(String topic, BiConsumer<String, String> callback) {
        MqttClientStart.getInstance().subscribeTopic(topic);
        if (callback != null) {
            topicCallbackMap.put(topic, callback);
        }
    }

    /**
     * 取消订阅主题
     * @param topic 主题
     */
    public static void unsubscribe(String topic) {
        // 这里假设MqttClientStart有unsubscribeTopic方法，否则需补充
        // MqttClientStart.getInstance().unsubscribeTopic(topic);
        topicCallbackMap.remove(topic);
    }

    /**
     * 发送消息到主题
     * @param topic 主题
     * @param message 消息内容
     */
    public static void publish(String topic, String message) {
        MqttClientStart.getInstance().publishMessage(topic, message);
    }

    /**
     * 处理收到的消息（由MqttClientStart的messageArrived回调调用）
     */
    public static void onMessage(String topic, MqttMessage message) {
        BiConsumer<String, String> callback = topicCallbackMap.get(topic);
        if (callback != null) {
            callback.accept(topic, new String(message.getPayload()));
        }
    }

    /**
     * 判断主题是否已订阅
     */
    public static boolean isSubscribed(String topic) {
        return topicCallbackMap.containsKey(topic);
    }

//    // 订阅
//MqttTopicUtil.subscribe("economy/835186488", (topic, msg) -> {
//        System.out.println("收到消息: " + topic + " 内容: " + msg);
//    });
//
//// 发送
//MqttTopicUtil.publish("economy/835186488", "hello mqtt");
//
//// 取消订阅
//MqttTopicUtil.unsubscribe("economy/835186488");
} 