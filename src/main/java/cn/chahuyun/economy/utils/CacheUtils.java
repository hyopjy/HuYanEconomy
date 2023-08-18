package cn.chahuyun.economy.utils;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class CacheUtils {

    public static Cache<Long, InputStream> fifoCache = CacheUtil.newLFUCache(50, 1000 * 60 * 60);

    public static TimedCache<String, Boolean> TIME_PROHIBITION = CacheUtil.newTimedCache(3 * 60 *1000);

    public static ConcurrentHashMap<String,Boolean> USER_USE_CARD = new ConcurrentHashMap<>();

    // CountDownLatch countDownLatch= ThreadUtil.newCountDownLatch(8);

    public static Cache<String, CountDownLatch> FISH_COUNT = CacheUtil.newLFUCache(50);

    public static  InputStream getAvatarUrlInputStream(Long qq, String avatarUrl) {
        if (Objects.isNull(fifoCache.get(qq))) {
            URL url2g;
            HttpURLConnection urlCon2g;
            try {
                url2g = new URL(avatarUrl);
                //设置连接超时的时间
                urlCon2g = (HttpURLConnection) url2g.openConnection();
                urlCon2g.setConnectTimeout(3000);
                urlCon2g.setReadTimeout(5000);

                InputStream inputStream = null;
                inputStream = urlCon2g.getInputStream();
                if (Objects.nonNull(inputStream)) {
                    ByteArrayInputStream in = new ByteArrayInputStream(inputStream.readAllBytes());
                    fifoCache.put(qq, in);
                }
            } catch (IOException e) {
                Log.error("getAvatarUrlInputStream 发生异常");
                Log.error(e);
            }
        }
        return fifoCache.get(qq);


    }


    public static String timeCacheKey(Long groupId,Long qq){
        return "Time-Cache-key:"+groupId+":"+qq;
    }

    public static String userUseKey(Long groupId,Long qq){
        return "user-use-card-key:"+groupId+":"+qq;
    }


    public static String userFishCountKey(Long groupId,Long qq){
        return "user-fish-count-key:"+groupId+":"+qq;
    }

}
