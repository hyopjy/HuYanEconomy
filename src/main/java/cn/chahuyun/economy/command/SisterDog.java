package cn.chahuyun.economy.command;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.UserManager;
import cn.chahuyun.economy.plugin.PropsType;
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
import xyz.cssxsh.mirai.economy.service.EconomyAccount;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        // 获取随机目标
        Map<EconomyAccount, Double> accountByBank = EconomyUtil.getAllAccount();
        List<EconomyAccount> economyAccount = accountByBank.keySet().stream()
                .filter(Objects::nonNull).collect(Collectors.toList());
        List<UserInfo> userInfoList = economyAccount.stream().map(UserManager::getUserInfo)
                .filter(user -> Objects.nonNull(user) && Objects.nonNull(group.get(user.getQq())))
                .collect(Collectors.toList());
        int userIndex = RandomUtil.randomInt(0, userInfoList.size());
        Log.info("[SisterDog] - userList：" + userInfoList.size()  + ",userIndex: " + userIndex);
        UserInfo user = userInfoList.get(userIndex);
        if(Objects.nonNull(user)){
            int money = RandomUtil.randomInt(100, 800);
            // 减去目标用户
            EconomyUtil.minusMoneyToUser(group.get(user.getQq()), money);
            // 自己获得
            User sender = event.getSender();
            EconomyUtil.plusMoneyToUser(sender, money);

            subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                    .append(propsCard.getName()).append("使用成功").append("\r\n")
                    .append(new At(sender.getId()).getDisplay(group)).append("成功获得" + money + "币币").append("\r\n")
                    .append(new At(user.getQq()).getDisplay(group)).append("失去自我3分钟").append("\r\n")
                    .build());
            // 失去自我的用户加入缓存
            CacheUtils.addTimeCacheKey(group.getId(), user.getQq());
        }else {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "随机用户为空！"));
        }
    }
}
