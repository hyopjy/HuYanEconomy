package cn.chahuyun.economy.command;

import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;

import java.util.regex.Pattern;

/**
 * 自定义永久头衔
 */
public class SpecialTitleAlawys extends AbstractPropUsage {

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
        this.title = "测试测试";
        return true;
    }

    @Override
    public void excute() {
        // todo check
        User sender = event.getSender();
        NormalMember normalMember = group.get(sender.getId());
        if (normalMember == null) {
            subject.sendMessage("没有这个人");
            return;
        }
        if (group.getBotPermission() != MemberPermission.ADMINISTRATOR) {
            subject.sendMessage("你的机器人不是群主，无法使用此功能！");
            return;
        }
        normalMember.setSpecialTitle(title);
    }
}
