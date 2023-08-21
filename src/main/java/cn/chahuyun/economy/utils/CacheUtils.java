package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.dto.DifficultyBuffDto;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CacheUtils {
    /**
     * 签到图片缓存
     */
    public static Cache<Long, InputStream> fifoCache = CacheUtil.newLFUCache(4, 1000 * 60 * 60);
    /**
     * 三分钟禁言 限制--姐姐的鱼 特效
     */
    public static TimedCache<String, Boolean> TIME_PROHIBITION = CacheUtil.newTimedCache(3 * 60 *1000);
    /**
     * 正在使用道具卡限制-- 消耗品
     */
    public static ConcurrentHashMap<String,Boolean> USER_USE_CARD = new ConcurrentHashMap<>();

    /**
     * 年年有鱼-增加钓鱼难度
     */
    public static Cache<String, DifficultyBuffDto> FISH_CARD_COUNT = CacheUtil.newLFUCache(200);

    /**
     * buff-正在占用的buff缓存 key - BUFF-groupID-QQid, BUFFNAME
     */
    public static Cache<String, String> BUFF_CACHE = CacheUtil.newLFUCache(200);

    /**
     * 面罩-每人每天限制3次
     */
    public static Cache<String, Integer> MASK_COUNT = CacheUtil.newLFUCache(200, 24 * 60 * 60 * 1000);

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

    /**
     * 3 分钟禁言key
     *
     * @param groupId
     * @param qq
     * @return
     */
    private static String timeCacheKey(Long groupId, Long qq) {
        return "Time-Cache-key:" + groupId + ":" + qq;
    }

    /**
     * 姐姐的狗 + 3分钟缓存限制
     *
     * @param groupId
     * @param qq
     */
    public static void addTimeCacheKey(Long groupId, Long qq) {
        CacheUtils.TIME_PROHIBITION.put(CacheUtils.timeCacheKey(groupId, qq), true);
    }

    /**
     * 姐姐的狗 + 3分钟缓存限制 -校验
     *
     * @param groupId
     * @param qq
     */
    public static boolean checkTimeCacheKey(Long groupId, Long qq) {
        return CacheUtils.TIME_PROHIBITION.containsKey(CacheUtils.timeCacheKey(groupId, qq));
    }


    /**
     * 用户 使用道具的缓存 key
     *
     * @param groupId
     * @param qq
     * @return
     */
    private static String userUseCardKey(Long groupId,Long qq){
        return "user-use-card-key:"+groupId+":"+qq;
    }

    /**
     *  增加 用户 使用道具的缓存 key
     * @param groupId
     * @param qq
     */
    public static void addUserUseCardKey(Long groupId, Long qq) {
        CacheUtils.USER_USE_CARD.put(CacheUtils.userUseCardKey(groupId,qq), true);
    }

    /**
     * 删除 用户 使用道具的缓存 key
     *
     * @param groupId
     * @param qq
     */
    public static void removeUserUseCardKey(Long groupId, Long qq) {
        CacheUtils.USER_USE_CARD.remove(CacheUtils.userUseCardKey(groupId, qq));
    }

    /**
     * 校验 用户 使用道具的缓存 key
     *
     * @param groupId
     * @param qq
     */
    public static boolean checkUserUseCardKey(Long groupId, Long qq) {
       return CacheUtils.USER_USE_CARD.containsKey(CacheUtils.userUseCardKey(groupId, qq));
    }

    /**
     * 年年有鱼
     * @param groupId
     * @param qq
     * @return
     */
    private static String getUserFishCardKey(Long groupId, Long qq, String cardName) {
        return "fish-count:" + cardName + ":" + groupId + ":" + qq;
    }

    /**
     * 增加 - 钓鱼困难度dto[年年有鱼buff]
     * @param groupId
     * @param qq
     * @param cardName
     * @return
     */
    public static void addFishCardKey(Long groupId, Long qq, String cardName, DifficultyBuffDto difficultyBuffDto) {
        CacheUtils.FISH_CARD_COUNT.put(getUserFishCardKey(groupId, qq, cardName), difficultyBuffDto);
    }

    public static boolean checkUserFishCardKey(Long groupId, Long qq, String cardName) {
        String key = CacheUtils.getUserFishCardKey(groupId, qq, cardName);
        if(CacheUtils.FISH_CARD_COUNT.containsKey(key)){
            DifficultyBuffDto difficultybuffDto =  CacheUtils.FISH_CARD_COUNT.get(key);
            if(Objects.nonNull(difficultybuffDto) && difficultybuffDto.getCount() > 0){
                return true;
            }
        }
        return false;
    }


    /**
     * mask - key MASK_COUNT
     */
    private static String userMaskCountKey(Long groupId, Long qq) {
        String dateToday = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "mask-count-key:" + dateToday + ":" + groupId + ":" + qq;
    }

    /**
     *  增加 用户 使用mask的缓存
     * @param groupId
     * @param qq
     */
    public static synchronized void addUserMaskCountKey(Long groupId, Long qq) {
        String key = CacheUtils.userMaskCountKey(groupId, qq);
        if (Objects.isNull(CacheUtils.MASK_COUNT.get(key))) {
            CacheUtils.MASK_COUNT.put(key, 1);
        } else {
            CacheUtils.MASK_COUNT.put(key, CacheUtils.MASK_COUNT.get(key) + 1);
        }
    }

    /**
     * 校验 面罩道具
     * @param groupId
     * @param qq
     * @return
     */
    public static boolean checkMaskCountKey(Long groupId, Long qq) {
        String key = CacheUtils.userMaskCountKey(groupId, qq);
        return Optional.ofNullable(CacheUtils.MASK_COUNT.get(key)).orElse(0) >= 3;
    }

    /**
     * BUFF道具的key 获取
     *
     */
    private static String getBuffCacheKey(Long groupId, Long qq) {
        return "buff:key:" + groupId + ":" + qq;
    }

    public static void addBuffCacheValue(Long groupId, Long qq, String value) {
        BUFF_CACHE.put(getBuffCacheKey(groupId, qq), value);
    }

    public static String getBuffCacheValue(Long groupId, Long qq) {
       return BUFF_CACHE.get(getBuffCacheKey(groupId, qq));
    }

    /**
     * 监听模式实现删除
     * @param groupId
     * @param qq
     * @param value
     */
    public static void removeBuffCacheValue(Long groupId, Long qq, String value) {
         BUFF_CACHE.remove(getBuffCacheKey(groupId, qq));
    }

    /**
     * 重置清理用户缓存
     */
    public static void clearCache(){
        TIME_PROHIBITION.clear();
        USER_USE_CARD.clear();
        FISH_CARD_COUNT.clear();
        MASK_COUNT.clear();
    }
}
