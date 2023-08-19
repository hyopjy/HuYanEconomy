package cn.chahuyun.economy.aop;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.factory.PropFishUsageContext;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class PropUtils {
    public static void excute(PropsFishCard propsCard, UserInfo userInfo, MessageEvent event){
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        MessageChainBuilder messages = MessageUtil.quoteReply(message);
        // 校验是否正在使用道具
        if (checkUserUseCard(subject.getId(), userInfo.getQq(), messages, subject)) {
            return;
        }

        try{
            PropFishUsageContext service = new ByteBuddy()
                    // 动态生成Service类的子类
                    .subclass(PropFishUsageContext.class)
                    // 拦截所有方法
                    .method(ElementMatchers.any())
                    // 使用LoggerAdvisor类作为拦截器，Advice是AOP的概念，似乎一般翻译为「通知」？
                    .intercept(Advice.to(PropAdvisor.class))
                    // 作出
                    .make()
                    // 硬塞给ClassLoader
                    .load(PropFishUsageContext.class.getClassLoader())
                    // 拿到Class对象
                    .getLoaded()
                    // Class.newInstance() 在Java 9中被废弃了，是个很有意思的故事，有兴趣可以去了解一下
                    .getConstructor()
                    .newInstance();
            service.excute(propsCard,userInfo,event);
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static boolean checkUserUseCard(long groupId, long qq, MessageChainBuilder messages, Contact subject) {
        // 校验是否正在使用道具
        if (CacheUtils.checkUserUseCardKey(groupId, qq)) {
            Log.info("checkUserUseCardKey has used");
            subject.sendMessage(messages.append("你正在使用道具!").build());
            return true;
        }
        // 校验是否在使用或者被使用-年年有鱼
        if (CacheUtils.checkUserFishCountKey(groupId, qq)) {
            Log.info("checkUserFishCountKey has used");
            subject.sendMessage(messages.append("你正在使用道具!").build());
            return true;
        }

        return false;
    }

}
