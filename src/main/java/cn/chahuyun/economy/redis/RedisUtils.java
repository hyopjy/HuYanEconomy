package cn.chahuyun.economy.redis;

import cn.chahuyun.economy.utils.Log;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public static void setBloomFilter(Long groupId,Long value) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "sister:prop:" + today + ":" + groupId;
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        // 初始化预期插入的数据量为10000和期望误差率为0.01
        rBloomFilter.tryInit(10000, 0.01);
        rBloomFilter.expire(24, TimeUnit.HOURS);
        rBloomFilter.add(value);
    }

    public static boolean checkBloomFilter(Long groupId,Long value) {
        // 判断是否是姐狗
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = "sister:prop:" + today + ":"+ groupId;
        RBloomFilter rBloomFilter = RedissonConfig.getRedisson().getBloomFilter(key);
        try {
            return rBloomFilter.contains(value);
        }catch (IllegalStateException e){
            Log.error(e.getMessage());
            return false;
        }
    }
}
