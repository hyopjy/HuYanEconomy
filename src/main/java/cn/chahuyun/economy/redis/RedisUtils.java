package cn.chahuyun.economy.redis;

import cn.chahuyun.economy.constant.RedisKeyConstant;
import cn.chahuyun.economy.dto.SpecialTitleDto;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.redisson.api.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RedisUtils {
    // http://www.rply.cn/news/43007.html
    public static void setKeyString(String key, String value) {
        RBucket<String> bucket = RedissonConfig.getRedisson().getBucket(key);
        bucket.set(value);
    }

    public static String getKeyString(String key) {
        return (String) RedissonConfig.getRedisson().getBucket(key).get();
    }

    public static RBloomFilter<Long> getFishSignBloomFilter(Long groupId,String prop) {
        String key = "fish:sign:" + prop + RedisKeyConstant.COLON_SPILT + groupId;
        RBloomFilter<Long> rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        rBloomFilter.tryInit(100, 0.01);
        return rBloomFilter;
    }

    public static RBloomFilter initSisterPropBloomFilter(Long groupId){
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = RedisKeyConstant.SISTER_PROP + today + RedisKeyConstant.COLON_SPILT + groupId;
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
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
        sisterDogRedis.set(userId, 30, TimeUnit.MINUTES);
    }

    /**
     * 加钓鱼锁
     */
    public static RLock getFishLock(Long groupId,Long userId){
       return RedissonConfig.getRedisson().getLock(RedisKeyConstant.FISH_LOCK_KEY + groupId + RedisKeyConstant.COLON_SPILT + userId);
    }

    /**
     * 添加对象进延时队列
     * @param putInData 添加数据
     * @param delay     延时时间
     * @param timeUnit  时间单位
     * @param queueName 队列名称
     * @param <T>
     */
    private static  <T>  void addQueue(T putInData,long delay, TimeUnit timeUnit, String queueName){
        Log.info("添加延迟队列,监听名称:"+queueName+",时间:"+delay+",时间单位:"+timeUnit+",内容:"+putInData+"");
        RBlockingQueue<T> blockingFairQueue = RedissonConfig.getRedisson().getBlockingQueue(queueName);
        RDelayedQueue<T> delayedQueue = RedissonConfig.getRedisson().getDelayedQueue(blockingFairQueue);
        delayedQueue.offer(putInData, delay, timeUnit);
    }

    /**
     * 添加队列-秒
     *
     * @param t     DTO传输类
     * @param delay 时间数量
     * @param <T>   泛型
     */
    public static  <T> void addQueueSeconds(T t, long delay, Class<? extends RedisDelayedQueueListener> clazz) {
        addQueue(t, delay, TimeUnit.SECONDS, clazz.getName().replace(".", RedisKeyConstant.COLON_SPILT));
    }

    /**
     * 添加队列-分
     *
     * @param t     DTO传输类
     * @param delay 时间数量
     * @param <T>   泛型
     */
    public static  <T> void addQueueMinutes(T t, long delay, Class<? extends RedisDelayedQueueListener> clazz) {
        addQueue(t, delay, TimeUnit.MINUTES, clazz.getName().replace(".", RedisKeyConstant.COLON_SPILT));
    }

    /**
     * 添加队列-时
     *
     * @param t     DTO传输类
     * @param delay 时间数量
     * @param <T>   泛型
     */
    public static  <T> void addQueueHours(T t, long delay, Class<? extends RedisDelayedQueueListener> clazz) {
        addQueue(t, delay, TimeUnit.HOURS, clazz.getName().replace(".", RedisKeyConstant.COLON_SPILT));
    }
    /**
     * 添加队列-天
     *
     * @param t     DTO传输类
     * @param delay 时间数量
     * @param <T>   泛型
     */
    public static  <T> void addQueueDays(T t, long delay, Class<? extends RedisDelayedQueueListener> clazz) {
        addQueue(t, delay, TimeUnit.DAYS, clazz.getName().replace(".", RedisKeyConstant.COLON_SPILT));
    }

    /**
     * 删除指定队列中的消息
     *
     */
    public static boolean removeDelayedQueue(Object putInData, String queueName) {
        if (StrUtil.isBlank(queueName) || Objects.isNull(putInData)) {
            return false;
        }
        RBlockingDeque<Object> blockingDeque = RedissonConfig.getRedisson().getBlockingDeque(queueName);
        RDelayedQueue<Object> delayedQueue = RedissonConfig.getRedisson().getDelayedQueue(blockingDeque);
        boolean flag = delayedQueue.remove(putInData);
        //delayedQueue.destroy();
        return flag;
    }


    public static void initDelayedQueueTask(){
        // https://www.dianjilingqu.com/635557.html
//        Map<String, RedisDelayedQueueListener> map = applicationContext.getBeansOfType(RedisDelayedQueueListener.class);
//        for (Map.Entry<String, RedisDelayedQueueListener> taskEventListenerEntry : map.entrySet()) {
//            String listenerName = taskEventListenerEntry.getValue().getClass().getName();
//            startThread(listenerName, taskEventListenerEntry.getValue());
//        }

        String listenerName = SpecialTitleOneDayExpirationListener.class.getName();
        listenerName = listenerName.replace(".",RedisKeyConstant.COLON_SPILT);
        startThread(listenerName, new SpecialTitleOneDayExpirationListener<SpecialTitleDto>());
    }

    /**
     * 启动线程获取队列
     * @param queueName 队列名称
     * @param redisDelayedQueueListener 任务回调监听
     */
    private static <T> void startThread(String queueName, RedisDelayedQueueListener redisDelayedQueueListener) {
        RBlockingQueue<T> blockingFairQueue = RedissonConfig.getRedisson().getBlockingQueue(queueName);
        //由于此线程需要常驻，可以新建线程，不用交给线程池管理
        Thread thread = new Thread(() -> {
            Log.info("启动监听队列线程" + queueName);
            while (true) {
                try {
                    T t = blockingFairQueue.take();
                    Log.info("监听队列线程" + queueName + ",获取到值:" + JSONUtil.toJsonStr(t) + "");
                    redisDelayedQueueListener.invoke(t);
                } catch (Exception e) {
                    Log.error("监听队列线程错误," + e);
                }
            }
        });
        thread.setName(queueName);
        thread.start();
    }
}
