package cn.chahuyun.economy.redis;

import org.apache.poi.ss.formula.functions.T;

public interface RedisDelayedQueueListener<T> {
    /**
     * 执行方法
     * @param t
     */
    void invoke(T t);
}
