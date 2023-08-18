package cn.chahuyun.economy.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;

public class Test {
    // https://zhuanlan.zhihu.com/p/151843984
    // https://zhuanlan.zhihu.com/p/84514959
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Service service = new ByteBuddy()
                // 动态生成Service类的子类
                .subclass(Service.class)
                // 拦截所有方法
                .method(ElementMatchers.any())
                // 使用LoggerAdvisor类作为拦截器，Advice是AOP的概念，似乎一般翻译为「通知」？
                .intercept(Advice.to(LoggerAdvisor.class))
                // 作出
                .make()
                // 硬塞给ClassLoader
                .load(Service.class.getClassLoader())
                // 拿到Class对象
                .getLoaded()
                // Class.newInstance() 在Java 9中被废弃了，是个很有意思的故事，有兴趣可以去了解一下
                .getConstructor()
                .newInstance();
        service.bar(123);
        service.foo(456);
    }
}
