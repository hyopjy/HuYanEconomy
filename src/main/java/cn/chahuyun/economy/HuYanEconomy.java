package cn.chahuyun.economy;

import cn.chahuyun.config.*;
import cn.chahuyun.economy.event.*;
import cn.chahuyun.economy.manager.BankManager;
import cn.chahuyun.economy.manager.LotteryManager;
import cn.chahuyun.economy.plugin.FishManager;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.power.PowerManager;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.FileUtils;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.CronUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.data.*;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public final class HuYanEconomy extends JavaPlugin {
    /**
     * 唯一实例
     */
    public static final HuYanEconomy INSTANCE = new HuYanEconomy();
    /**
     * 全局版本
     */
    public static final String version = "0.1.16";
    /**
     * 配置
     */
    public static EconomyConfig config;

    public static final ConcurrentHashMap<Integer, InputStream> INPUT_STREAM_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, InputStream> SIGN_STREAM_MAP = new ConcurrentHashMap<>();

    /**
     * 插件所属bot
     */
    public Bot bot;

    private HuYanEconomy() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanEconomy", version)
                .name("HuYanEconomy")
                .info("壶言经济")
                .author("Moyuyanli")
                //忽略依赖版本 true 可选依赖 false 必须依赖
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", false)
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-economy-core", false)
                .dependsOn("cn.chahuyun.HuYanSession", true)
                .build());
    }

    @Override
    public void onEnable() {
        EventChannel<Event> eventEventChannel = GlobalEventChannel.INSTANCE.parentScope(HuYanEconomy.INSTANCE);
        //加载前置
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(this);
        //初始化插件数据库
        HibernateUtil.init(configuration);
        //加载配置
        reloadPluginConfig(EconomyConfig.INSTANCE);
        reloadPluginConfig(EconomyPluginConfig.INSTANCE);
        reloadPluginConfig(EconomyEventConfig.INSTANCE);
        reloadPluginConfig(DriverCarEventConfig.INSTANCE);
        // 自动钓鱼机
        reloadPluginConfig(AutomaticFishConfig.INSTANCE);

        // 加载文件流
        for (int i = 1; i < 7; i++) {
            FileUtils.getInputStream(i);
        }
        FileUtils.getSignFishStream();
        config = EconomyConfig.INSTANCE;
        //插件功能初始化
        PluginManager.init();
        long configBot = config.getBot();
        if (configBot == 0) {
            Log.warning("插件管理机器人还没有配置，请尽快配置!");
        } else {
            EconomyUtil.init();
            LotteryManager.init(true);
            FishManager.init();
            BankManager.init();
            // 延迟队列init
           // RedisUtils.initDelayedQueueTask();
            eventEventChannel.registerListenerHost(new EconomyEventListener());
            eventEventChannel.registerListenerHost(new BotOnlineEventListener());
            eventEventChannel.registerListenerHost(new MessageEventListener());
            eventEventChannel.registerListenerHost(new DriverCarEventListener());
            eventEventChannel.registerListenerHost(new RandomMoneyListener());
            PowerManager.init(eventEventChannel);
            Log.info("事件已监听!");
        }
        EconomyPluginConfig.INSTANCE.setFirstStart(false);
        Log.info(String.format("HuYanEconomy已加载！当前版本 %s !", version));
    }

    /**
     * 插件关闭
     */
    @Override
    public void onDisable() {
        CronUtil.stop();
        Log.info("插件已卸载!");
    }
}
