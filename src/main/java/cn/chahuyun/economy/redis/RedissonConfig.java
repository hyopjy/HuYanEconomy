package cn.chahuyun.economy.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonConfig {

    public static final RedissonClient REDISSON_CLIENT = redissonClient();

    public static RedissonClient redissonClient(){
        String host = "127.0.0.1:6739";
        int database = 1;
        Config config = new Config();
        config.useSingleServer().setAddress(host).setPassword("").setDatabase(database);
        return Redisson.create(config);
    }

}
