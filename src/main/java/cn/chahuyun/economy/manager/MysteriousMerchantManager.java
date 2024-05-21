package cn.chahuyun.economy.manager;

import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 神秘商人管理
 *
 */
public class MysteriousMerchantManager {

//1.每天14点、17点、21点都有15%概率刷新神秘商人
//2.神秘商人会在商品83~92中（暂定十个）随机上架2~4种
//3.神秘商人上架的每种商品各有随机1~3个库存
//4.神秘商人会在出现后的10分钟消失
//5.每个人在神秘商人处的限购数为每次每种商品数1
//6.神秘商人的商品会有兑换品，即：可能有币币购买、可能有赛季币购买、可能有道具兑换；神秘商人的商品会有打包商品，如：100币币作为一个打包品，每次可以使用100000赛季币购买一份
//7.具体的商品列表、回复文案请等我！！！

    /**
     * 开启神秘商人
     *
     * @param event
     */
    public static void open(MessageEvent event){
    //    开启神秘商人
    }

    /**
     * 关闭神秘商人
     *
     * @param event
     */
    public static void close(MessageEvent event){
    //    关闭神秘商人
    }

    /**
     * 设置神秘商人base
     *
     * @param event
     */
    public static void setting(MessageEvent event){
    //    设置神秘商人 14,17,21 15% 10(几分钟消失)  83-92(商品编码范围) 2(几种道具)  1-3(随机道具库存)

    }

    /**
     * 设置神秘商人
     *
     * @param event
     */
    public static void buyShop(MessageEvent event){
        //    神秘商人商店 (excel导入)
    }

    public static void exchangeShop(MessageEvent event){
        //    神秘商人兑换商店 (excel导入)
    }

    public static void buy(MessageEvent event){
        //    购买(想个新命令)
    }

    public static void exchange(MessageEvent event){
        //    兑换(想个新命令)
    }

    // 道具背包 展示神秘商店标志
}
