package cn.chahuyun.economy.aop;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.json.JSONUtil;
import net.bytebuddy.asm.Advice;
import net.mamoe.mirai.event.events.MessageEvent;

import java.lang.reflect.Method;
import java.util.Arrays;

public class PropAdvisor {
    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
        if (method.getAnnotation(Prop.class) != null) {
            for (Object obj : arguments) {
                if (obj instanceof MessageEvent) {
                    MessageEvent event = (MessageEvent) obj;
                    CacheUtils.addUserUseCardKey(event.getSubject().getId(), event.getSubject().getId());
                }
            }
            Log.info("[PropAdvisor]-[Enter] method:" + method.getName());
        }
    }


    @Advice.OnMethodExit
    public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
        if (method.getAnnotation(Prop.class) != null) {
            for (Object obj : arguments) {
                if (obj instanceof MessageEvent) {
                    MessageEvent event = (MessageEvent) obj;
                    CacheUtils.removeUserUseCardKey(event.getSubject().getId(),event.getSubject().getId());
                }
            }
            Log.info("[PropAdvisor]-[Exit] method:" + method.getName());
        }
    }

//    @Advice.OnMethodExit
//    public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments, @Advice.Return Object ret) {
//        if (method.getAnnotation(Log.class) != null) {
//            System.out.println("Exit " + method.getName() + " with arguments: " + Arrays.toString(arguments) + " return: " + ret);
//        }
//    }

}
