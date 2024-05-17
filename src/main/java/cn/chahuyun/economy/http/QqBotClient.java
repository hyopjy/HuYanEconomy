package cn.chahuyun.economy.http;

import cn.chahuyun.economy.http.dto.BaseResultDto;
import cn.chahuyun.economy.http.dto.VerifySessionDto;
import com.dtflys.forest.annotation.Address;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Request;

import java.util.Map;

// 整个接口下的所有方法请求都默认绑定该根地址
@Address(host = "127.0.0.1", port = "8080")
public interface QqBotClient {
    // https://forest.dtflyx.com/pages/1.5.36/build_interface/
    // https://gitee.com/igingo/graceful-response

    /**
     * 获取session信息
     *
     * @param verifyKey
     * @return
     */
    @Post("/verify")
    VerifySessionDto getVerifySession(@JSONBody("verifyKey") String verifyKey);

    /**
     * 绑定session 和 bot
     *
     * @param sessionKey
     * @return
     */
    @Post("/bind")
    BaseResultDto bindSessionWithBot(@JSONBody("sessionKey") String sessionKey, @JSONBody("qq") Long qq);

    /**
     * 使用此方式释放session及其相关资源（Bot不会被释放）
     * 不使用的Session应当被释放，长时间（30分钟）未使用的Session将自动释放，
     * 否则Session持续保存Bot收到的消息，将会导致内存泄露(开启websocket后将不会自动释放)
     */
    @Post("/release")
    BaseResultDto releaseSessionWithBot(@JSONBody("sessionKey") String sessionKey, @JSONBody("qq") Long qq);

//    查看文件列表
//[GET] /file/list
//    本接口为[GET]请求, 参数格式为url参数
//
//    通用接口定义: 查看文件列表
//
//#获取文件信息
//[GET] /file/info
//    本接口为[GET]请求, 参数格式为url参数
//
//    通用接口定义: 获取文件信息
//
//#创建文件夹
//[POST] /file/mkdir
//    本接口为[POST]请求, 参数格式为application/json
//
//    通用接口定义: 创建文件夹
//
//#删除文件
//[POST] /file/delete
//    本接口为[POST]请求, 参数格式为application/json
//
//    通用接口定义: 删除文件

}
