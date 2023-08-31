package cn.chahuyun.economy.command;
import cn.chahuyun.economy.dto.SpecialTitleDto;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.redis.RedissonConfig;
import cn.chahuyun.economy.redis.SpecialTitleOneDayExpirationListener;
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
        String match = "使用 (" + propsCard.getName() + "|" + no + ") \\S+";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + " 头衔描述]"));
            return false;
        }
        NormalMember normalMember = group.get(event.getSender().getId());
        if (normalMember == null) {
            subject.sendMessage("没有这个人");
            return false;
        }
        if (group.getBotPermission() != MemberPermission.OWNER) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"你的机器人不是群主，无法使用此功能！"));
            return false;
        }
        String[] s = code.split(" ");
        if(s[2].length() > 6){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"头衔描述不能超过6个字"));
            return false;
        }
        this.title = s[2];
        return true;
    }

    @Override
    public void excute() {
        User sender = event.getSender();
        NormalMember normalMember = group.get(sender.getId());
        normalMember.setSpecialTitle(title);
        // 延迟过期策略
        SpecialTitleDto dto = new SpecialTitleDto();
        dto.setGroup(group);
        dto.setUserId(sender.getId());
        // 删除已经存在的--
        // https://blog.csdn.net/qq_40250122/article/details/123111145
        RedisUtils.addQueueDays(dto, 1, SpecialTitleOneDayExpirationListener.class);

        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),"修改头衔成功！24小时后消失"));
    }
}
