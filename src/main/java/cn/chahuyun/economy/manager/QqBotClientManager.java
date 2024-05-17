package cn.chahuyun.economy.manager;
import cn.chahuyun.economy.http.QqBotClient;
import cn.chahuyun.economy.http.dto.BaseResultDto;
import cn.chahuyun.economy.http.dto.VerifySessionDto;
import com.dtflys.forest.Forest;
import com.dtflys.forest.config.ForestConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class QqBotClientManager {
    private static final  Integer SUCCESS_CODE = 0;

    public static ConcurrentHashMap<Long, String> sessionKeyMap = new ConcurrentHashMap();

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

    public static String getSession() {
        String verifyKey = "INITKEYOHBbUoxm";
        VerifySessionDto verifySession = QqBotClientManager.getQqBotClient().getVerifySession(verifyKey);
        if (Objects.nonNull(verifySession) && SUCCESS_CODE.equals(verifySession.getCode())) {
            return verifySession.getSession();
        }
        return "";
    }

    public static void bindSessionWithBot(Long botQq) {
        // 如果存在sessionKey 则释放
        if(StringUtils.isNotBlank(sessionKeyMap.get(botQq))){
            releaseSessionWithBot(sessionKeyMap.get(botQq), botQq);
        }
        String sessionKey = getSession();
        if(StringUtils.isBlank(sessionKey)){
            return;
        }
        BaseResultDto dto =  QqBotClientManager.getQqBotClient().bindSessionWithBot(sessionKey, botQq);

        if (Objects.nonNull(dto) && SUCCESS_CODE.equals(dto.getCode())) {
            sessionKeyMap.put(botQq, sessionKey);
        }
    }

    public static void releaseSessionWithBot(String sessionKey, Long botQQ) {
        QqBotClientManager.getQqBotClient().releaseSessionWithBot(sessionKey, botQQ);
    }
}
