package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.dto.DifficultyBuffDto;

import java.util.Objects;
import java.util.Optional;

public class BuffUtils {
    /**
     * 获取正在使用的buff 名称
     *
     * @param groupId
     * @param qq
     * @return
     */
    public static String getBuffCacheValue(Long groupId, Long qq) {
        return CacheUtils.getBuffCacheValue(groupId,qq);
    }

    public static DifficultyBuffDto getBuffFishCard(Long groupId, Long qq, String cardName) {
        return Optional.ofNullable(CacheUtils.getFishCardKey(groupId, qq, cardName)).orElse(new DifficultyBuffDto());
    }

    public static synchronized void reduceBuffFishCard(long groupId, long qq, String cardName) {
        if(CacheUtils.checkUserFishCardKey(groupId, qq, cardName)){
            DifficultyBuffDto difficultyBuffDto = CacheUtils.getFishCardKey(groupId, qq, cardName);
            difficultyBuffDto.setCount(difficultyBuffDto.getCount() - 1);
            CacheUtils.addFishCardKey(groupId, qq, cardName,difficultyBuffDto);
        }else {
            CacheUtils.removeUserFishCardKey(groupId, qq, cardName);
        }
    }
}
