package cn.chahuyun.economy.mqtt;

import cn.chahuyun.economy.HuYanEconomy;
import net.mamoe.mirai.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MqttTopic implements Runnable{

    private volatile boolean running = true; // 使用volatile确保可见性
    private volatile boolean initialized = false; // 添加初始化标志
    private final List<Long> pendingSubscriptions = new ArrayList<>(); // 待订阅的主题列表

    public void addGroupTopic(){
        List<Long> initGroupList = new ArrayList<>(3);
        initGroupList.add(835186488L);
        initGroupList.add(227265762L);
        initGroupList.add(758085692L);
        initialized = true;
        // 初始化时就加到待订阅
        pendingSubscriptions.clear();
        pendingSubscriptions.addAll(initGroupList);
        // 设置MQTT重连成功回调
        MqttClientStart.getInstance().setOnReconnectSuccessCallback(() -> {
            System.out.println("MQTT重连成功，重新尝试订阅主题...");
            // 重新填充待订阅主题
            pendingSubscriptions.clear();
            pendingSubscriptions.addAll(initGroupList);
            attemptSubscribeTopics();
        });
    }
    /**
     * 尝试订阅所有待订阅的主题
     */
    private void attemptSubscribeTopics() {
        if (pendingSubscriptions.isEmpty()) {
            return;
        }
        MqttClientStart mqttClient = MqttClientStart.getInstance();
        if (!mqttClient.isConnected()) {
            System.out.println("MQTT未连接，等待连接后订阅主题...");
            return;
        }
        if (!mqttClient.isConnectionHealthy()) {
            System.out.println("MQTT连接质量不佳，等待连接稳定后订阅主题...");
            return;
        }
        System.out.println("MQTT已连接且连接健康，开始订阅主题...");
        List<Long> subscribedGroups = new ArrayList<>();
        for (Long groupId : pendingSubscriptions) {
            try {
                String topic = "economy/" + groupId;
                // 使用工具类订阅并注册回调
                MqttTopicUtil.subscribe(topic, (t, msg) -> {
                    System.out.println("收到消息: " + t + " 内容: " + msg);
                });
                subscribedGroups.add(groupId);
                System.out.println("成功订阅主题: " + topic);
                // 验证订阅是否成功
                if (MqttTopicUtil.isSubscribed(topic)) {
                    System.out.println("验证订阅成功: " + topic);
                } else {
                    System.err.println("订阅验证失败: " + topic);
                }
                // 订阅成功后立即发送一条测试消息验证订阅
//                MqttTopicUtil.publish(topic, "MqttTopic订阅测试消息");
            } catch (Exception e) {
                System.err.println("订阅主题失败: topic/" + groupId + ", 错误: " + e.getMessage());
            }
        }
        // 从待订阅列表中移除已订阅的主题
        pendingSubscriptions.removeAll(subscribedGroups);
        if (!pendingSubscriptions.isEmpty()) {
            System.out.println("还有 " + pendingSubscriptions.size() + " 个主题待订阅");
        } else {
            System.out.println("所有主题订阅完成");
            System.out.println(mqttClient.getSubscriptionStatus());
        }
    }
    // 新增：取消订阅方法，使用工具类
    public void unsubscribeTopic(String topic) {
        MqttTopicUtil.unsubscribe(topic);
        System.out.println("取消订阅主题: " + topic);
    }
    // 新增：发送消息方法，使用工具类
    public void sendMessage(String topic, String message) {
        MqttTopicUtil.publish(topic, message);
        System.out.println("发送消息到主题: " + topic + " 内容: " + message);
    }

    @Override
    public void run() {
        while (running) { // 仅依赖 running 标志
            Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
            if (bot == null) {
                // 处理 bot 未就绪的情况 (如延迟重试)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (!running) {
                        break; // 如果是正常停止，退出循环
                    }
                    throw new RuntimeException(e);
                }
                continue;
            } else {
                // 加载群组信息（只执行一次）
                if (!initialized) {
                    addGroupTopic();
                }

                // 尝试订阅主题
                attemptSubscribeTopics();

                // 如果还有待订阅的主题，继续等待
                if (!pendingSubscriptions.isEmpty()) {
                    try {
                        Thread.sleep(2000); // 减少等待时间到2秒
                        continue;
                    } catch (InterruptedException e) {
                        if (!running) {
                            break;
                        }
                    }
                }

                // 初始化完成且所有主题订阅完成，线程可以退出
                if (initialized && pendingSubscriptions.isEmpty()) {
                    System.out.println("MqttTopic初始化完成，所有主题订阅成功");
                    break;
                }
            }
        }
    }

    // 提供停止方法
    public void stop() {
        running = false;
        Thread.currentThread().interrupt(); // 中断线程
    }
}
