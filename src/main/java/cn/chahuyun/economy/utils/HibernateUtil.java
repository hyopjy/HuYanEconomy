package cn.chahuyun.economy.utils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :hibernate
 * @Date 2022/7/30 22:47
 */
public class HibernateUtil {


    /**
     * 数据库连接前缀
     */
    private static final String SQL_PATH_PREFIX = "jdbc:h2:file:";

    /**
     * 会话工厂
     */
    public static SessionFactory factory = null;

    private HibernateUtil() {

    }

    /**
     * Hibernate初始化
     *
     * @param configuration Configuration
     * @author Moyuyanli
     * @date 2022/7/30 23:04
     */
    public static void init(MiraiHibernateConfiguration configuration) {
        String path = SQL_PATH_PREFIX + "./data/cn.chahuyun.HuYanEconomy/HuYanEconomy";
        configuration.setProperty("hibernate.connection.url", path);
        configuration.setProperty("hibernate.hikari.maximumPoolSize", "10");
        configuration.setProperty("hibernate.hikari.maximumPoolSize", "5");
        configuration.setProperty("hibernate.hikari.connectionTimeout","30000");
        configuration.setProperty("hibernate.hikari.idleTimeout","600000");
        configuration.setProperty("hibernate.hikari.maxLifetime","1800000");
        configuration.setProperty("hibernate.hikari.connectionTestQuery","SELECT 1");
        configuration.scan("cn.chahuyun.entity");
        try {
            factory = configuration.buildSessionFactory();

        } catch (HibernateException e) {
            Log.error("请删除data中的HuYanEconomy.mv.db后重新启动！", e);
            return;
        }
        Log.info("H2数据库初始化成功!");
    }


}
