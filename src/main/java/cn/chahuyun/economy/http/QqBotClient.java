package cn.chahuyun.economy.http;

import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.Request;
// 整个接口下的所有方法请求都默认绑定该根地址
@Address(host = "127.0.0.1", port = "18620")
public interface QqBotClient {
    // https://forest.dtflyx.com/pages/1.5.36/build_interface/
    // https://gitee.com/igingo/graceful-response
    @Request("/hello")
    String simpleRequest();
}
