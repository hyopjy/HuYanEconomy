package cn.chahuyun.economy.factory;

import cn.chahuyun.economy.constant.PropConstant;
import cn.chahuyun.economy.dto.Buff;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
@Setter
public abstract class AbstractPropUsage implements IPropUsage{
    protected PropsFishCard propsCard;

    protected UserInfo userInfo;

    protected MessageEvent event;

    protected Group group;

    protected Contact subject;

    protected Long target;

    protected Boolean isBuff = false;

    protected Boolean deleteProp = true;
    @Override
    public abstract boolean checkOrder();


    @Override
    public abstract void excute();

    @Override
    public boolean checkBuff(){
        // 不是buff不限制
        if(!isBuff){
            return true;
        }
        // 校验是否正在使用BUff -- 执行结束后删除
        // 判断命令执行者是否在使用其他buff
        User sender = event.getSender();
        if (PropConstant.AUTOMATIC_FISH.equals(propsCard.getName())) {
            if (CacheUtils.checkAutomaticFishBuff(group.getId(), sender.getId())) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在使用[" + propsCard.getName() + "]buff"));
                return false;
            }
        } else {
            Buff buff = CacheUtils.getBuff(group.getId(), sender.getId());
            if (isBuff && Objects.nonNull(buff)) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "正在使用[" + buff.getBuffName() + "]buff"));
                return false;
            }
        }
        return true;
    }

    /**
     * 单纯校验 使用
     * @return
     */
    protected boolean checkOrderDefault(){
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")( )*";
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        if (!Pattern.matches(match, code)) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                    "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "]"));
            return false;
        }
        return true;
    }
}
