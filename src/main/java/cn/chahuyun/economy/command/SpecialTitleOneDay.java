package cn.chahuyun.economy.command;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.redis.RedissonConfig;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SpecialTitleOneDay extends AbstractPropUsage {

    private String title;

    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        // todo 正则
        String match = "使用 (" + propsCard.getName() + "|" + no + ")()*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
            return false;
        }
        if (group.getBotPermission() != MemberPermission.ADMINISTRATOR) {
            subject.sendMessage("你的机器人不是群主，无法使用此功能！");
            return false;
        }
        this.title = "测试测试";
        return true;
    }

    @Override
    public void excute() {
        User sender = event.getSender();
        NormalMember normalMember = group.get(sender.getId());
        if (normalMember == null) {
            subject.sendMessage("没有这个人");
            return;
        }

        normalMember.setSpecialTitle(title);
        RSet<Long> setOneDayKey = RedissonConfig.getRedisson().getSet("special:title:one:user:set:" + group.getId());
        setOneDayKey.add(sender.getId());

        RBucket<String> bucket = RedissonConfig.getRedisson().getBucket("special:title:one:day:key" + sender.getId() + ":" + group.getId());
        bucket.set(title, 1, TimeUnit.DAYS);
    }
}
