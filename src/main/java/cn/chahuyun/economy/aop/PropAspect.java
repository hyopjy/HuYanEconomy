package cn.chahuyun.economy.aop;
import cn.chahuyun.economy.factory.PropFishUsageContext;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.hutool.aop.aspects.Aspect;

import java.lang.reflect.Method;

/**
 * 增加和删除缓存
 */
public class PropAspect implements Aspect {

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        if(target instanceof PropFishUsageContext){
            PropFishUsageContext  abstractPropUsage =(PropFishUsageContext) target;
            CacheUtils.USER_USE_CARD.put(abstractPropUsage.getQq(),true);
            System.out.println("add cache");
        }
        return true;
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        if(target instanceof PropFishUsageContext){
            PropFishUsageContext  abstractPropUsage =(PropFishUsageContext) target;
            CacheUtils.USER_USE_CARD.remove(abstractPropUsage.getQq());
            System.out.println("delete cache");
        }
        return true;
    }

    @Override
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        return false;
    }
}
