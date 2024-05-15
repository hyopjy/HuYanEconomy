package cn.chahuyun.economy.http;

import com.dtflys.forest.annotation.Request;

public interface QqBotClient {
    // https://forest.dtflyx.com/pages/1.5.36/build_interface/
    // https://gitee.com/igingo/graceful-response
    @Request("http://localhost:8080/hello")
    String simpleRequest();
}
