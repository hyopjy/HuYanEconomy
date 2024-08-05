package cn.chahuyun.economy.utils;


import cn.chahuyun.economy.dto.Buff;
import cn.chahuyun.economy.dto.BuffProperty;

import java.util.Objects;
import java.util.Optional;

public class BuffUtils {

    public static int getIntegerPropValue(Buff buff, String propKey) {
        Optional<BuffProperty> property = buff.getProperties().stream()
                .filter(prop -> propKey.equals(prop.getPropertyKey())).findAny();
        if(property.isPresent()){
            Object p = property.get().getPropertyValue();
            if(p instanceof Integer){
                return (int) p;
            }
        }
        return 0;
    }

    public static double getDoublePropValue(Buff buff, String propKey) {
        Optional<BuffProperty> property = buff.getProperties().stream()
                .filter(prop -> propKey.equals(prop.getPropertyKey())).findAny();
        if(property.isPresent()){
            Object p = property.get().getPropertyValue();
            if(p instanceof Integer){
                return (double) p;
            }
        }
        return 0;
    }

    public static synchronized void reduceBuffCount(long id, long qq) {
        Buff buff = CacheUtils.getBuff(id, qq);
        if (Objects.nonNull(buff)) {
            int count = buff.getCount();
            buff.setCount(count - 1);
            CacheUtils.addBuff(id, qq, buff);
        }
    }

    public static String getBooleanPropType(Buff buffBack, String propKey) {
        Optional<BuffProperty> property = buffBack.getProperties().stream()
                .filter(prop -> propKey.equals(prop.getPropertyKey())).findAny();
        if(property.isPresent()){
            Object p = property.get().getPropertyValue();
            if(p instanceof String){
                return (String) p;
            }
        }
        return "";
    }
}
