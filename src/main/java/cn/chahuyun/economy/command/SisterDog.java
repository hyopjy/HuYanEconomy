package cn.chahuyun.economy.command;


import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import org.redisson.api.RKeys;
import xyz.cssxsh.mirai.economy.service.EconomyAccount;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 姐姐的狗
 */
public class SisterDog extends AbstractPropUsage {

    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        // 消耗品，对指定目标使用，使目标失去自我3分钟，并获得目标的币币（随机100-800）
        Long userId;
        String key = "clicker:" + group.getId();
        Object clicker =  RedisUtils.getKeyObject(key);
        if(Objects.isNull(clicker)){
            // 获取随机目标
            List<Long> userInfoList = RedisUtils.getSisterUserList(group.getId());
            int userIndex = RandomUtil.randomInt(0, userInfoList.size());
            Log.info("[SisterDog] - userList：" + userInfoList.size()  + ",userIndex: " + userIndex);
            userId = userInfoList.get(userIndex);
        }else {
            userId = (Long) clicker;
            RedisUtils.deleteKeyString(key);
        }
        if(Objects.nonNull(userId)){
            int money = RandomUtil.randomInt(100, 800);
            // 减去目标用户
            EconomyUtil.minusMoneyToUser(group.get(userId), money);
            // 自己获得
            User sender = event.getSender();
            EconomyUtil.plusMoneyToUser(sender, money);

            subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                    .append(propsCard.getName()).append("使用成功").append("\r\n")
                    .append(new At(sender.getId()).getDisplay(group)).append("成功获得" + money + "币币").append("\r\n")
                    .append(new At(userId).getDisplay(group)).append("失去自我3分钟").append("\r\n")
                    .build());
            // 失去自我的用户加入缓存
            CacheUtils.addTimeCacheKey(group.getId(), userId);
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "随机用户为空！"));
        }
    }
}
