package cn.chahuyun.economy.redis;
import org.redisson.Redisson;
import org.redisson.config.Config;
public class RedissonConfig {
     private static Config config = new Config();

    //声明redisso对象
    private static Redisson redisson = null;
    static{
        config.useSingleServer()
                .setAddress("redis://8.140.194.225:6379")
                .setPassword("zyjy110.");
        //得到redisson对象
        redisson = (Redisson) Redisson.create(config);

    }
    //获取redisson对象的方法
    public static Redisson getRedisson(){
        return redisson;
    }

}
