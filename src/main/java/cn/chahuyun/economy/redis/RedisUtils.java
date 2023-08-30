package cn.chahuyun.economy.redis;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;

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

    public static void setBloomFilter(String key, Long value) {
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        rBloomFilter.tryInit(10000, 0.01);
        rBloomFilter.expire(24, TimeUnit.HOURS);
        rBloomFilter.add(value);
    }

    public static boolean checkBloomFilter(String key, Long value) {
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        return rBloomFilter.contains(value);
    }

}
