package cn.chahuyun.economy.redis;

import cn.chahuyun.economy.constant.RedisKeyConstant;
import cn.chahuyun.economy.utils.Log;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * 设置购买姐狗布隆过滤器
     *
     * @param groupId
     * @param value
     */
    public static void setSisterPropBloomFilter(Long groupId, Long value) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = RedisKeyConstant.SISTER_PROP + today + RedisKeyConstant.COLON_SPILT + groupId;
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        rBloomFilter.tryInit(10000, 0.01);
        rBloomFilter.expire(24, TimeUnit.HOURS);
        rBloomFilter.add(value);
    }

    /**
     * 判断姐狗布隆过滤器是否存在
     *
     * @param groupId
     * @param value
     * @return
     */
    public static boolean checkSisterPropBloomFilter(Long groupId, Long value) {
        // 判断是否是姐狗
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = RedisKeyConstant.SISTER_PROP + today + RedisKeyConstant.COLON_SPILT + groupId;
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        try {
            return rBloomFilter.contains(value);
        }catch (IllegalStateException e){
            Log.error(e.getMessage());
            return false;
        }
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
}
