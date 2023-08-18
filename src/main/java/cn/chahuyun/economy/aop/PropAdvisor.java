package cn.chahuyun.economy.aop;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.json.JSONUtil;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.Arrays;

public class PropAdvisor {
    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
        if (method.getAnnotation(Prop.class) != null) {
            for (Object obj : arguments) {
                if (obj instanceof UserInfo) {
                    UserInfo user = (UserInfo) obj;
                    CacheUtils.USER_USE_CARD.put(user.getQq(), true);
                }
            }
            Log.info("[PropAdvisor]-[Enter] method:" + method.getName() + " with arguments: " + Arrays.toString(arguments));
        }
    }


    @Advice.OnMethodExit
    public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments) {
        if (method.getAnnotation(Prop.class) != null) {
            for (Object obj : arguments) {
                if (obj instanceof UserInfo) {
                    UserInfo user = (UserInfo) obj;
                    CacheUtils.USER_USE_CARD.remove(user.getQq());
                }
            }
            Log.info("[PropAdvisor]-[Exit] method:" + method.getName() + " with arguments: " + Arrays.toString(arguments));
        }
    }

//    @Advice.OnMethodExit
//    public static void onMethodExit(@Advice.Origin Method method, @Advice.AllArguments Object[] arguments, @Advice.Return Object ret) {
//        if (method.getAnnotation(Log.class) != null) {
//            System.out.println("Exit " + method.getName() + " with arguments: " + Arrays.toString(arguments) + " return: " + ret);
//        }
//    }

}
