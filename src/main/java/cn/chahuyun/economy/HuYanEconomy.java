package cn.chahuyun.economy;

import cn.chahuyun.config.*;
import cn.chahuyun.economy.event.*;
import cn.chahuyun.economy.manager.*;
import cn.chahuyun.economy.mqtt.MqttClientStart;
import cn.chahuyun.economy.mqtt.MqttTopic;
import cn.chahuyun.economy.plugin.FishManager;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.power.PowerManager;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.FileUtils;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.CronUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import org.apache.commons.collections4.CollectionUtils;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    public static final String MQTT_SERVER_NAME = "Economy-MQTT-";

    public static final ConcurrentHashMap<Integer, InputStream> INPUT_STREAM_MAP = new ConcurrentHashMap<>(6);

    public static final ConcurrentHashMap<String, InputStream> SIGN_STREAM_MAP = new ConcurrentHashMap<>(10);


    public static InputStream BASE_INFO_STREAM = null;

    /**
     * 插件所属bot
     */
    public Bot bot;

    // 类成员变量
    private MqttTopic mqttTopic;


    public Bot getBotInstance() {
        if (bot == null) {
            List<Bot> botList = Bot.getInstances();
            if (CollectionUtils.isEmpty(botList)) {
                Log.info("getBotInstance 获取bot为空");
                return bot;
            }
            return botList.get(0);
        }
        return bot;
    }

    private HuYanEconomy() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanEconomy", version)
                .name("HuYanEconomy")
                .info("壶言经济")
                .author("Moyuyanli")
                //忽略依赖版本 true 可选依赖 false 必须依赖
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", true)
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-economy-core", true)
                .dependsOn("cn.chahuyun.HuYanSession", true)
                .dependsOn("net.mamoe.mirai-api-http", true)
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
        AutomaticFishingMachineManager.init();

        // 加载文件流
//        for (int i = 1; i < 7; i++) {
//            FileUtils.getInputStream(i);
//        }
        FileUtils.loadBaseInfoPNG();
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
            // 赛季信息停止定时任务
            // CompetitionSeasonManager.seasonInit();
            WorldBossConfigManager.init();
            MysteriousMerchantManager.init();
            // 延迟队列init
            RedisUtils.initDelay();
            eventEventChannel.registerListenerHost(new EconomyEventListener());
            eventEventChannel.registerListenerHost(new BotOnlineEventListener());
            eventEventChannel.registerListenerHost(new MessageEventListener());
            eventEventChannel.registerListenerHost(new DriverCarEventListener());
            eventEventChannel.registerListenerHost(new RandomMoneyListener());

//            eventEventChannel.registerListenerHost(new BotPostSendEventListener());
            PowerManager.init(eventEventChannel);

            Log.info("事件已监听!");
        }
        EconomyPluginConfig.INSTANCE.setFirstStart(false);

        // 启动MQTT状态监控线程
        startMqttStatusMonitor();

        // 初始化并启动MqttTopic线程，订阅 /economy/groupId 主题
        mqttTopic = new MqttTopic();
        Thread topicThread = new Thread(mqttTopic, "Economy-MQTT-Topic-Init");
        topicThread.setDaemon(true);
        topicThread.start();


        Log.info(String.format("HuYanEconomy已加载！当前版本 %s !", version));
    }

    /**
     * 启动MQTT状态监控线程
     */
    private void startMqttStatusMonitor() {
        Thread monitorThread = new Thread(() -> {
            MqttClientStart mqttClient = MqttClientStart.getInstance();
            int lastReconnectAttempts = 0;
            boolean lastMaxAttemptsReached = false;

            while (true) {
                try {
                    Thread.sleep(30000); // 每30秒检查一次

                    // 检查重连次数变化
                    int currentAttempts = mqttClient.getReconnectAttempts();
                    if (currentAttempts != lastReconnectAttempts) {
                        Log.info(MQTT_SERVER_NAME + "MQTT重连次数变化: " + lastReconnectAttempts + " -> " + currentAttempts);
                        lastReconnectAttempts = currentAttempts;
                    }

                    // 检查最大尝试次数状态变化
                    boolean currentMaxAttemptsReached = mqttClient.isMaxAttemptsReached();
                    if (currentMaxAttemptsReached != lastMaxAttemptsReached) {
                        if (currentMaxAttemptsReached) {
                            Log.info(MQTT_SERVER_NAME + "MQTT已达到最大重连次数，将等待60秒后重新尝试");
                        } else {
                            Log.info(MQTT_SERVER_NAME +"MQTT重置重连计数，重新开始连接尝试");
                        }
                        lastMaxAttemptsReached = currentMaxAttemptsReached;
                    }

                    // 如果重连次数较多但未达到最大次数，记录警告
                    if (currentAttempts >= mqttClient.getMaxReconnectAttempts() * 0.6 && !currentMaxAttemptsReached) {
                        Log.info(MQTT_SERVER_NAME +"MQTT重连次数较多: " + currentAttempts + "/" + mqttClient.getMaxReconnectAttempts());
                    }

                    // 记录当前连接状态
                    if (!mqttClient.isConnected()) {
                        Log.info(MQTT_SERVER_NAME +"MQTT当前状态: " + mqttClient.getConnectionStatus());
                    }

                } catch (InterruptedException e) {
                    Log.info(MQTT_SERVER_NAME +"MQTT状态监控线程被中断");
                    break;
                } catch (Exception e) {
                    Log.info(MQTT_SERVER_NAME +"MQTT状态监控异常: " + e.getMessage());
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("Economy-MQTT-Status-Monitor");
        monitorThread.start();
    }

    /**
     * 插件关闭
     */
    @Override
    public void onDisable() {
        CronUtil.stop();
        // 停止MQTT客户端
        MqttClientStart.getInstance().closed();
        // 优雅关闭MqttTopic线程
        if (mqttTopic != null) {
            mqttTopic.stop();
        }
        // 优雅关闭延时队列线程
        RedisUtils.stopDelayThread();
        Log.info("插件已卸载!");
    }
}
