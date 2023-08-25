package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.dto.Buff;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;

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
    public static Cache<Long, InputStream> fifoCache = CacheUtil.newLFUCache(4);
    /**
     * 三分钟禁言 限制--姐姐的鱼 特效
     */
    public static TimedCache<String, Boolean> TIME_PROHIBITION = CacheUtil.newTimedCache(3 * 60 *1000);
    /**
     * 正在使用道具卡限制-- 消耗品
     */
    public static ConcurrentHashMap<String,Boolean> USER_USE_CARD = new ConcurrentHashMap<>();

    /**
     * 面罩-每人每天限制3次
     */
    public static Cache<String, Integer> MASK_COUNT = CacheUtil.newLFUCache(200, 24 * 60 * 60 * 1000);

    /**
     * 薛定谔的鱼
     */
    public static TimedCache<String, Boolean> SCH_DINGER_FISH = CacheUtil.newTimedCache(2 * 60 *1000);

    /**
     * buff道具缓存信息
     */
    public static Cache<String, Buff> BUFF_CACHE = CacheUtil.newLFUCache(250);

    public static Cache<String, String> AUTOMATIC_FISH_USER = CacheUtil.newLFUCache(250,8 * 60 * 60 * 1000);

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


    private static String getSchDingerFishKey(Long groupId, Long qq) {
        return "sch-dinger-fish-Cache-key:" + groupId + ":" + qq;
    }
    /**
     * 薛定谔的鱼 + 2分钟缓存限制
     *
     * @param groupId
     * @param qq
     */
    public static void addSchDingerFishKey(Long groupId, Long qq) {
        CacheUtils.SCH_DINGER_FISH.put(CacheUtils.getSchDingerFishKey(groupId, qq), true);
    }

    /**
     * 薛定谔的鱼 + 2分钟缓存限制
     *
     * @param groupId
     * @param qq
     */
    public static boolean checkSchDingerFishKey(Long groupId, Long qq) {
        return CacheUtils.SCH_DINGER_FISH.containsKey(CacheUtils.getSchDingerFishKey(groupId, qq));
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
     * buff key
     *   监听模式移除缓存 --事件+监听
     */
    private static String getBuffKey(Long groupId, Long qq) {
        return "buff:key:" + groupId + ":" + qq;
    }

    /**
     * 增加buff
     * @param groupId
     * @param qq
     * @return
     */
    public static void addBuff(Long groupId, Long qq, Buff buff) {
        String buffKey = getBuffKey(groupId, qq);
        BUFF_CACHE.put(buffKey, buff);
    }

    /**
     * 获取buff
     *
     */
    public static Buff getBuff(Long groupId, Long qq) {
        String buffKey = getBuffKey(groupId, qq);
        Buff buff = BUFF_CACHE.get(buffKey);
        if (Objects.nonNull(buff) && buff.getCount() <= 0) {
            BUFF_CACHE.remove(buffKey);
        }
        return BUFF_CACHE.get(buffKey);
    }

    private static String getAutomaticFishKey(Long groupId, Long qq) {
        return "automatic:fish:" + groupId + ":" + qq;
    }
    public static void addAutomaticFishBuff(Long groupId, Long qq, String value) {
        String automaticFishKey = getAutomaticFishKey(groupId, qq);
        AUTOMATIC_FISH_USER.put(automaticFishKey, value);
    }

    public static String getAutomaticFishBuff(Long groupId, Long qq) {
        String buffKey = getAutomaticFishKey(groupId, qq);
        return AUTOMATIC_FISH_USER.get(buffKey);
    }

    public static Boolean checkAutomaticFishBuff(Long groupId, Long qq) {
        String buffKey = getAutomaticFishKey(groupId, qq);
        return !StrUtil.isBlank(AUTOMATIC_FISH_USER.get(buffKey));
    }

    /**
     * 重置清理用户缓存
     */
    public static void clearCache(){
        TIME_PROHIBITION.clear();
        USER_USE_CARD.clear();
        MASK_COUNT.clear();
    }

    public static void removeAutomaticFishBuff(long groupId, long qq) {
        String buffKey = getAutomaticFishKey(groupId, qq);
        AUTOMATIC_FISH_USER.remove(buffKey);
    }
}
