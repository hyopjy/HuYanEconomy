package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.SingleMessage;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class GroupAdminManager {
    public static void giveCup(GroupMessageEvent event) {
        Contact subject = event.getSubject();
        List<Long> atUser = new ArrayList<>();
        for (SingleMessage msg : event.getMessage()) {
            if (msg instanceof At) {
                long target = ((At) msg).getTarget();
                atUser.add(target);
            }
        }
        if(CollectionUtils.isEmpty(atUser)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "颁发失败，未检测艾特的人员"));
            return;
        }
//        FISH-100
        for(int i=0; i< atUser.size(); i++){
            Long userId = atUser.get(i);
            giveCupProp(event.getGroup().get(userId));
        }
        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "颁发成功"));
    }

    public static String giveCupProp(User user){
        return giveProp("FISH-100", user);
    }

    public static String giveTheChosenOneProp( User user){
        return giveProp("FISH-118", user);
    }

    public static String giveAllIn(User user){
        return giveProp("FISH-113", user);
    }

    public static String giveProp(String propCode, User user){
        if(Objects.isNull(user)){
            Log.info("给道具失败 用户为空");
            return " 用户不存在";
        }
        PropsBase propsInfo = PropsType.getPropsInfo(propCode);
        if(Objects.isNull(propsInfo)){
            return "   道具不存在";
        }
        UserInfo newUserInfo = UserManager.getUserInfo(user);
        UserBackpack newBackpackItem = new UserBackpack(newUserInfo, propsInfo);
        newUserInfo.addPropToBackpack(newBackpackItem);
        return propsInfo.getName();
    }
}


