package cn.chahuyun.economy.command;

import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.PropsManager;
import cn.chahuyun.economy.manager.UserManager;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 薛定谔的鱼
 */
public class SchDingerFish extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "@指定对象]"));
            return false;
        }
        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                this.target = at.getTarget();
            }
        }
        return true;
    }

    @Override
    public void excute() {
//        似乎有用又似乎没有用……对指定目标使用，背包内的随机可交易道具将以薛定谔的状态进入使用者的背包
//    ；并且目标获得2分钟「薛定谔」buff，发送的所有消息都会被撤回

        CacheUtils.addSchDingerFishKey(group.getId(), target);
        NormalMember targetMember = group.get(target);
        if(Objects.isNull(targetMember)){
            return;
        }
        UserInfo targetUserInfo = UserManager.getUserInfo(targetMember);
        if(Objects.isNull(targetUserInfo)){
            return;
        }
        PropsManager propsManager = PluginManager.getPropsManager();
        // 获取目标对象的背包可交易道具
        List<PropsFishCard> propsByUserFromCode =  propsManager.getPropsByUserFromCode(targetUserInfo, PropsFishCard.class);
        if(CollectionUtils.isEmpty(propsByUserFromCode)){
            return;
        }
        List<PropsFishCard> tradablePropCode = propsByUserFromCode.stream().filter(back-> Boolean.TRUE.equals(back.getTradable()))
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(tradablePropCode)){
            return;
        }

        // 不减去道具
//        PluginManager.getPropsManager().deleteProp(targetUserInfo, randomPropsFishCard);

        List<UserBackpack> targetPack  = Optional.ofNullable(targetUserInfo.getBackpacks()).orElse(Lists.newArrayList());
        int count = (int) targetPack.stream().map(UserBackpack::getPropsCode).distinct().count();
        if (count < 3) {
            subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                    .append(propsCard.getName()).append("使用成功").append("\r\n")
                    .append(new At(target).getDisplay(group)).append("获得「薛定谔」buff，发送的所有消息都会被撤回")
                    .append(new At(userInfo.getQq()).getDisplay(group)).append("目标背包里的东西太少了！啥也没获得")
                    .append("\r\n")
                    .build());
            return;
        }

        // 生成一个随机索引
        int randomIndex = RandomUtil.randomInt(tradablePropCode.size());
        // 获取随机元素
        PropsFishCard randomPropsFishCard = tradablePropCode.get(randomIndex);
        // 发送者获得道具
        PluginManager.getPropsManager().addProp(userInfo, randomPropsFishCard);


//        @目标获得「薛定谔」buff，2分钟内发送的所有消息都会被撤回；@用户获得了薛定谔状态的[道具名]×1
        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName()).append("使用成功").append("\r\n")
                .append(new At(target).getDisplay(group)).append("获得「薛定谔」buff，发送的所有消息都会被撤回")
                .append(new At(userInfo.getQq()).getDisplay(group)).append("获得了薛定谔状态的[" + randomPropsFishCard.getName() + "]×1")
                .append("\r\n")
                .build());
    }
}
