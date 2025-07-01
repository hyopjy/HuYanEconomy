package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.redis.RedisUtils;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 🫧泡泡标识
 * 🪙波币标识
 * 🎰梭哈标识
 * 💴万贯标识
 * 准点打工标识
 * 动物大使标识
 * 狗姐心选标识
 * 天选之子标识
 */
public class FishingCostume extends AbstractPropUsage {
    //    ˚.∘・◌・•°🫧・◦・🫧°•・◌・∘.˚
    //    AAA开始钓鱼
    //    鱼塘:日夜颠岛
    //    等级:6
    //    「纯天然湖泊，鱼情优秀，又大又多」
    //    .˚✧.˚✧.˚🫧˚✧.˚✧.˚🫧˚✧.˚✧.˚

    @Override
    public boolean checkOrder() {
        return this.checkOrderDefault();
    }

    @Override
    public void excute() {
        // 使用后顶掉之前使用的道具
//        String FISH_SIGN_1 = "\uD83E\uDEE7泡泡标识";
//        String FISH_SIGN_2 = "\uD83E\uDE99波币标识";
//        String FISH_SIGN_3 = "\uD83C\uDFB0梭哈标识";
//        String FISH_SIGN_4 = "\uD83D\uDCB4万贯标识";
//        String FISH_SIGN_5 = "准点打工标识";
//        String FISH_SIGN_6 = "动物大使标识";
//        String FISH_SIGN_7 = "狗姐心选标识";
//        String FISH_SIGN_8 = "天选之子标识";
        User sender = event.getSender();

        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName() + "使用成功").append("\r\n")
                .build());
        setRedisObject(sender.getId(), propsCard.getCode());

    }

    public static String getRedisObject(Long userId){
        return (String) RedisUtils.getKeyObject(getRedisKey(userId));
    }
    private static String getRedisKey(Long userId){
        return "FishingCostume" + "-" + userId;
    }

    public static void setRedisObject(Long userId, String propCode){
        RedisUtils.setKeyObject(getRedisKey(userId), propCode);
    }

    public static List<String> startAndEnd(String propCode){
        List<String> arr = new ArrayList<>(2);
        if("FISH-101".equals(propCode)){
            arr.add("˚.∘・◌・•°\uD83E\uDEE7・◦・\uD83E\uDEE7°•・◌・∘.˚");
            arr.add(".˚✧.˚✧.˚\uD83E\uDEE7˚✧.˚✧.˚\uD83E\uDEE7˚✧.˚✧.˚");
            return arr;
        }
        if("FISH-102".equals(propCode)){
            arr.add("⭒꙳✧\uD83D\uDCB0\uD83E\uDE99\uD83D\uDCB0⁂\uD83C\uDF1F\uD83E\uDE99\uD83C\uDF1F⁂\uD83D\uDCB0\uD83E\uDE99\uD83D\uDCB0✧꙳⭒");
            arr.add(".-.\uD83C\uDF1F.-.\uD83E\uDE99.-.\uD83D\uDCB0.-.\uD83E\uDE99.-.\uD83C\uDF1F.-.");
            return arr;
        }
        if("FISH-103".equals(propCode)){
            arr.add("\uD81A\uDC0C♦\uFE0F\uD83C\uDCCF♥♣\uFE0F\uD83C\uDFB2\uD83C\uDFB0\uD83C\uDFB2♣\uFE0F♥\uD83C\uDCCF♦\uFE0F\uD81A\uDC0C\n");
            arr.add("◢◤◢◤◢◤◢◤◢◤◢◤◢◤◢◤");
            return arr;
        }
        if("FISH-104".equals(propCode)){
            arr.add("～●○\uD83D\uDC51\uD83D\uDC8E\uD83D\uDCB0✨\uD83D\uDCB0\uD83D\uDC8E\uD83D\uDC51○●～");
            arr.add("～○～●～\uD83D\uDC8E～●～○～");
            return arr;
        }
        if("FISH-105".equals(propCode)){
            arr.add("▁ ▂ ▄ ⏰✅△\uD83D\uDCC7△✅⏰ ▄ ▂ ▁");
            arr.add("✎﹏﹏﹏﹏﹏﹏﹏");
            return arr;
        }
        if("FISH-106".equals(propCode)){
            arr.add(". ✧･ﾟʚ\uD83D\uDC2Cɞ˚ ₊✦. ⋆｡\uD83C\uDF0A｡⋆ ✦.₊ ˚ʚ\uD83E\uDEBCɞﾟ･✧ .");
            arr.add("*★*★*★*★*★*★*★*★*");
            return arr;
        }
        if("FISH-107".equals(propCode)){
            arr.add("✧･ﾟ:✧\uD83D\uDC69･ﾟ::\uD83D\uDC36･ﾟ\uD83D\uDC69:･ﾟ✧");
            arr.add("*\uD83D\uDC36*★*\uD83D\uDC36*★*\uD83D\uDC36*★*\uD83D\uDC36*");
            return arr;
        }
        if("FISH-108".equals(propCode)){
            arr.add("･ﾟ✧\uD83C\uDF40⋆･\uD83E\uDD1E\uD83E\uDD1E +°\uD83C\uDF40 ﾟ･.");
            arr.add(".˚✧.˚✧.˚\uD83C\uDF40˚✧.˚✧.˚\uD83C\uDF40˚✧.˚✧.˚");
            return arr;
        }

        return arr;
    }


}
