package cn.chahuyun.economy.aop;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.factory.PropFishUsageContext;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.hutool.aop.ProxyUtil;

public class PropUtils {
    public static void excute(){

        if(CacheUtils.USER_USE_CARD.containsKey(934415751L)){
            //

        }

        PropsFishCard propsCard = new PropsFishCard();
        propsCard.setName("玻璃珠");
        propsCard.setFishDesc("wwwwwwwwww");
        UserInfo userInfo = new UserInfo();
        userInfo.setQq(934415751L);

        /**
         * 每个方法都会执行切面
         */
        PropFishUsageContext context = ProxyUtil.proxy(new PropFishUsageContext(propsCard,userInfo, null), PropAspect.class);
        context.excute();

    }

    public static void main(String[] args) {
        PropUtils.excute();
    }
}
