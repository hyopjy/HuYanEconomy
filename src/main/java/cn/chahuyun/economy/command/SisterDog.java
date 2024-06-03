package cn.chahuyun.economy.command;


import cn.chahuyun.economy.event.SisterDogCommand;
import cn.chahuyun.economy.event.SisterDogListener;
import cn.chahuyun.economy.event.SisterDogListenerImpl;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.compress.utils.Lists;


import java.time.LocalDateTime;
import java.util.regex.Pattern;


/**
 * 狗的姐姐
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
        User sender = event.getSender();
        // 判断是否正在使用
        String key = SisterDogCommand.getListenerKey(group.getId(), sender.getId());
        if (SisterDogCommand.LISTENER_CONCURRENT_HASHMAP.containsKey(key)) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在使用"));
            return false;
        }
        return true;
    }

    @Override
    public void excute() {
        // 消耗品，对指定目标使用，使目标失去自我3分钟，并获得目标的币币（随机100-800）
        User sender = event.getSender();
        Integer n = RandomUtil.randomInt(2, 67);
        subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                .append(propsCard.getName()).append("使用成功").append("\r\n")
                // .append(new At(sender.getId()).getDisplay(group)).append("搭讪的姐姐选择了幸运数字" + n)
                .build());
//        RedisUtils.addDogSisterCount(group.getId(), sender.getId());
        String key = SisterDogCommand.getListenerKey(group.getId(), sender.getId());
        SisterDogListener listener = SisterDogListenerImpl.builder()
                .groupId(group.getId())
                .expireDate(LocalDateTime.now().plusMinutes(15L))
                .userIdList(Lists.newArrayList())
                .sourceId(sender.getId())
                .n(n)
                .build();
        SisterDogCommand.addClickListener(key, listener);


    }
}
