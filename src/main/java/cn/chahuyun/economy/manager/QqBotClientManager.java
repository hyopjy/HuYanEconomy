package cn.chahuyun.economy.manager;
import cn.chahuyun.economy.http.QqBotClient;
import com.dtflys.forest.Forest;
import com.dtflys.forest.config.ForestConfiguration;

import java.util.Map;

public class QqBotClientManager {
    public static QqBotClient getQqBotClient(){
        // 获取 Forest 全局配置对象
        ForestConfiguration configuration = Forest.config();
        // 连接池最大连接数
        configuration.setMaxConnections(1000);
        // 连接超时时间，单位为毫秒
        configuration.setConnectTimeout(2000);
        // 数据读取超时时间，单位为毫秒
        configuration.setReadTimeout(2000);

        return Forest.client(QqBotClient.class);

    }

    public static String getSession(){
        String verifyKey = "INITKEYOHBbUoxm";
        Map<String, String> sessionMap = QqBotClientManager.getQqBotClient().getVerifySession(verifyKey);
        System.out.println(sessionMap.get("session"));
        return sessionMap.get("session");
    }
}
