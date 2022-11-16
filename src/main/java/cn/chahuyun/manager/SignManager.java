package cn.chahuyun.manager;

import cn.chahuyun.entity.UserInfo;
import cn.chahuyun.plugin.PluginManager;
import cn.hutool.core.swing.RobotUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.poi.excel.RowUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

/**
 * 签到管理<p>
 *
 * @author Moyuyanli
 * @date 2022/11/14 12:25
 */
public class SignManager {

    private SignManager() {

    }


    /**
     * 签到
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/11/15 14:53
     */
    public static void sign(MessageEvent event) {
        User user = event.getSender();
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();

        UserInfo userInfo = UserManager.getUserInfo(user);

        MessageChain messages = MessageUtils.newChain(new QuoteReply(message));
        if (!userInfo.sign()) {
            messages.add(new PlainText("你今天已经签到过了哦!"));
            subject.sendMessage(messages);
        }

        double goldNumber;

        int randomNumber = RandomUtil.randomNumber();
        if (randomNumber > 7) {
            randomNumber = RandomUtil.randomNumber();
            if (randomNumber > 7) {
                goldNumber = RandomUtil.randomInt(200, 500);
            } else {
                goldNumber = RandomUtil.randomInt(100, 200);
            }
        } else {
            goldNumber =  RandomUtil.randomInt(50, 100);
        }

        PropsManager propsManager = PluginManager.getPropsManager();



    }


}
