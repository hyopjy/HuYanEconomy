package cn.chahuyun.economy.command;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.PermissionDeniedException;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 冠军奖杯
 */
public class ChampionshipTrophy extends AbstractPropUsage {
    @Override
    public boolean checkOrder() {
        if(!checkOrderDefault()){
            return false;
        }
        // check 每天使用一次
        String key = "ChampionshipTrophy" + "-" + event.getSender().getId() + LocalDate.now().format(Constant.FORMATTER_YYYY_MM_DD);

        Object obj = RedisUtils.getKeyObject(key);
        if(Objects.nonNull(obj)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                    propsCard.getName() + " 每天限使用一次"));
            return false;
        }
        this.deleteProp = Boolean.FALSE;
        return true;
    }

    /**
     * 最后获得本道具的用户可以使用，每天限使用一次，使用后bobo回复：
     * @全体成员
     * 🏆『叮！检测到一股冠军的气息扑面而来！』
     * ✨很难说，把编号100留给哪个道具。幸好你出现了，@使用者，你就是那唯一被选中的、所向无敌——冠军！
     * 🌟闪瞎众菜狗的冠军之光！让bo的服务器都开始冒烟！🔥
     */
    @Override
    public void excute() {
        User sender = event.getSender();

        MessageChain msg = new MessageChainBuilder()
                .append(AtAll.INSTANCE)  // ✅ 调用单例
                .append("\r\n🏆『叮！检测到一股冠军的气息扑面而来！』\r\n")
                .append("✨ 很难说，把编号100留给哪个道具。幸好你出现了，")
                .append(new At(sender.getId()))
                .append("，你就是那唯一被选中的、所向无敌——冠军！\r\n")
                .append("\uD83C\uDF1F闪瞎众菜狗的冠军之光！让bo的服务器都开始冒烟！\uD83D\uDD25 \r\n")
                .build();
        try {
            group.sendMessage(msg);
        } catch (PermissionDeniedException e) {
            group.sendMessage("🌟 冠军诞生！可惜机器人权限不足无法@大家\\~");
        }
        String key = "ChampionshipTrophy" + "-" + sender.getId() + LocalDate.now().format(Constant.FORMATTER_YYYY_MM_DD);
        RedisUtils.setKeyObject(key, sender.getId(), 1L, TimeUnit.DAYS);

    }
}
