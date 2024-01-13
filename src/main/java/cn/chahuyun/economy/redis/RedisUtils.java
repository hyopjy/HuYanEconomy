package cn.chahuyun.economy.redis;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.RedisKeyConstant;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RedisUtils {
    // http://www.rply.cn/news/43007.html

    private static String SPECIAL_TITLE_ONE_DAY_QUEUE = "SpecialTitleOneDayQueue";

    public static void setKeyString(String key, String value, Long time, TimeUnit timeUnit) {
        RBucket<String> bucket = RedissonConfig.getRedisson().getBucket(key);
        bucket.set(value, time, timeUnit);
    }
    public static void setKeyObject(String key, Object value) {
        RBucket<Object> bucket = RedissonConfig.getRedisson().getBucket(key);
        bucket.set(value);
    }

    public static Object getKeyObject(String key) {
        return RedissonConfig.getRedisson().getBucket(key).get();
    }

    public static void deleteKeyString(String key) {
        RedissonConfig.getRedisson().getBucket(key).delete();
    }


    public static RBloomFilter<Long> getFishSignBloomFilter(Long groupId,String prop) {
        String key = "fish:sign:" + prop + RedisKeyConstant.COLON_SPILT + groupId;
        RBloomFilter<Long> rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        rBloomFilter.tryInit(500, 0.01);
        return rBloomFilter;

    }

    public static RBloomFilter<Long> initOneDayPropBloomFilter(Long groupId, String propKey) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = propKey + today + RedisKeyConstant.COLON_SPILT + groupId;
        RBloomFilter<Long> rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        rBloomFilter.tryInit(100, 0.01);
        rBloomFilter.expire(24, TimeUnit.HOURS);
        return rBloomFilter;
    }


    /**
     * 获取30分钟内发言的uesrid
     */
    public static List<Long> getSisterUserList(Long groupId) {
        String key = RedisKeyConstant.SISTER_USER + groupId + ":*";
        List<Long> userInfoList = new ArrayList<>();
        RKeys keys = RedissonConfig.getRedisson().getKeys();
        Iterable<String> keysByPattern = keys.getKeysByPattern(key);
        for (String s : keysByPattern) {
            Long value = (Long) RedissonConfig.getRedisson().getBucket(s).get();
            userInfoList.add(value);
        }
        return userInfoList;
    }

    /**
     * 设置30分钟内发言缓存
     */
    public static void setSisterUserList(Long groupId, Long userId) {
        String key = RedisKeyConstant.SISTER_USER + groupId + RedisKeyConstant.COLON_SPILT + userId;
        RBucket<Long> sisterDogRedis = RedissonConfig.getRedisson().getBucket(key);
        sisterDogRedis.set(userId, 15, TimeUnit.MINUTES);
    }

    /**
     * 加钓鱼锁
     */
    public static RLock getFishLock(Long groupId,Long userId){
       return RedissonConfig.getRedisson().getLock(RedisKeyConstant.FISH_LOCK_KEY + groupId + RedisKeyConstant.COLON_SPILT + userId);
    }


    public static void initDelay() {
        Thread thread = new Thread(() -> {
            initDelayedQueueTask();
        });
        thread.start();
    }

    public static void initDelayedQueueTask(){
        RBlockingDeque<String> blockingDeque = RedissonConfig.getRedisson().getBlockingDeque(SPECIAL_TITLE_ONE_DAY_QUEUE);
        // 注意虽然delayedQueue在这个方法里面没有用到，但是这行代码也是必不可少的。
        RDelayedQueue<String> delayedQueue = RedissonConfig.getRedisson().getDelayedQueue(blockingDeque);
        while (true) {
            String orderId = null;
            try {
                orderId = blockingDeque.take();
            } catch (Exception e) {
                Log.error("initDelayedQueueTask 发生异常！");
                e.printStackTrace();
                continue;
            }
            if (StrUtil.isBlank(orderId)) {
                continue;
            }
            Log.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "延时队列收到:" + orderId);
            String[] orderIds = orderId.split("-");
            Long groupId = Long.valueOf(orderIds[0]);
            Long userId  = Long.valueOf(orderIds[1]);
            Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
            if (bot == null) {
                Log.info("[延时队列收到-发生异常]-获取bot为空：" + groupId);
                return;
            }
            Group group = HuYanEconomy.INSTANCE.bot.getGroup(groupId);
            if(Objects.isNull(group)){
                continue;
            }
            NormalMember normalMember = group.get(userId);
            if(Objects.isNull(normalMember)){
                continue;
            }
            normalMember.setSpecialTitle("");
            Message m = new At(userId).plus("自定义一日title已过期");
            group.sendMessage(m);
        }

    }
    public static void addTaskToDelayQueue(String orderId, int delay,TimeUnit timeUnit) {
        // RBlockingDeque的实现类为:new RedissonBlockingDeque
        RBlockingDeque<String> blockingDeque = RedissonConfig.getRedisson().getBlockingDeque(SPECIAL_TITLE_ONE_DAY_QUEUE);
        // RDelayedQueue的实现类为:new RedissonDelayedQueue
        RDelayedQueue<String> delayedQueue = RedissonConfig.getRedisson().getDelayedQueue(blockingDeque);
        if(delayedQueue.contains(orderId)){
            blockingDeque.remove(orderId);
            delayedQueue.remove(orderId);
        }

        Log.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "添加任务到延时队列里面:" + orderId);
        delayedQueue.offer(orderId, delay, timeUnit);
    }

    public static Double getWditBBCount(long groupId, long userId) {
        String dateString  = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "wdit:bb:" + dateString + ":" + groupId + ":" + userId;
        Object value = getKeyObject(key);
        if (Objects.isNull(value)) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        } else {
            return 0.0;
        }
    }

    public static void setWditBBCount(long groupId, long userId, Double money) {
        String dateString  = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "wdit:bb:" + dateString + ":" + groupId + ":" + userId;
        setKeyObject(key, money);
    }

}
