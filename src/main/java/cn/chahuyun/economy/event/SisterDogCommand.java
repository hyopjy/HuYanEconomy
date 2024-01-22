package cn.chahuyun.economy.event;

import cn.hutool.core.lang.Singleton;

import java.util.concurrent.ConcurrentHashMap;

// 步骤4：注册监听器
public class SisterDogCommand {

    private SisterDogCommand() {
        // 私有化构造函数，防止外部实例化
    }

    public static SisterDogCommand getInstance() {
        return Singleton.get(SisterDogCommand.class);
    }

    public static ConcurrentHashMap<String, SisterDogListener> LISTENER_CONCURRENT_HASHMAP = new ConcurrentHashMap<>();

    public static void addClickListener(String key, SisterDogListener listener) {
        LISTENER_CONCURRENT_HASHMAP.put(key, listener);
    }

    public static void removeClickListener(String key) {
        LISTENER_CONCURRENT_HASHMAP.remove(key);
    }

    public synchronized void fireClickEvent(Long groupId, Long userId) {
        SisterDogEvent event = SisterDogEvent.builder()
                .groupId(groupId)
                .userId(userId)
                .build();
        // 使用迭代器遍历并处理监听器
        for (SisterDogListener listener : LISTENER_CONCURRENT_HASHMAP.values()) {
            listener.onCheckSisterDogMember(event);
        }
    }

    public static String getListenerKey(Long groupId, Long userId){
        return "LISTENER-MAP" + groupId +"," + userId;
    }



}
