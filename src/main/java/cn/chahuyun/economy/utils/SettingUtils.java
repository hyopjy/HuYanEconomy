package cn.chahuyun.economy.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SettingUtils {
    // 徽章setting
   // public static FishSignSetting FISH_SIGN_SETTING;

    public static ConcurrentHashMap<String, Set<Long>> FISH_SIGN_SETTING_MAP;

    public static final String FISH_SIGN_SETTING_STR = "config/fish.setting";

    public static final String fishSignGroupKey = "fish";

    public static final String fishSignKey = "fishSignMap";

    static {
        FISH_SIGN_SETTING_MAP = getFishSignConcurrentHashMap();
    }


    public static Setting loadSetting(String settingPath) {
        Setting setting = new Setting(settingPath);
        setting.autoLoad(true);
        return setting;
    }

    public static Setting loadFishSignSetting() {
        return loadSetting(FISH_SIGN_SETTING_STR);
    }

    private static ConcurrentHashMap<String, Set<Long>> getFishSignConcurrentHashMap() {
        String str = loadFishSignSetting()
                .getByGroup(fishSignGroupKey,fishSignKey);
        Map map = Optional.ofNullable(JSONUtil.toBean(str, Map.class)).orElse(new ConcurrentHashMap());
        return (ConcurrentHashMap<String, Set<Long>>) map;
    }

    private static void setFishSignConcurrentHashMap(ConcurrentHashMap<String, Set<Long>> map) {
        loadFishSignSetting().setByGroup(fishSignKey, fishSignGroupKey, JSONUtil.toJsonStr(map));
        loadFishSignSetting().store();
    }


    public static void setFishSignSettingMap(String propCode,Long groupId, Long userId){
        ConcurrentHashMap<String, Set<Long>> map = getFishSignConcurrentHashMap();
        String key = groupId + "-" + propCode;
        Set<Long> userIdList = map.get(key);
        if(CollectionUtil.isEmpty(userIdList)){
            userIdList = new CopyOnWriteArraySet<>();
        }
        userIdList.add(userId);
        map.put(key, userIdList);

        setFishSignConcurrentHashMap(map);
    }

    public static boolean checkUserExit(String propCode,Long groupId, Long userId){
        ConcurrentHashMap<String, Set<Long>> map = getFishSignConcurrentHashMap();
        String key = groupId + "-" + propCode;
        Set<Long> userIdList = map.get(key);
        if(CollectionUtil.isEmpty(userIdList)){
            userIdList = new CopyOnWriteArraySet<>();
        }
       return userIdList.contains(userId);
    }

}
