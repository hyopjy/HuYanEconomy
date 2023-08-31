package cn.chahuyun.economy.redis;

import cn.chahuyun.economy.dto.SpecialTitleDto;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

public class SpecialTitleOneDayExpirationListener<T> implements RedisDelayedQueueListener<T>{
    /**
     * 执行方法
     *
     * @param t
     */
    @Override
    public void invoke(T t) {
        if(t instanceof SpecialTitleDto){
            SpecialTitleDto dto = (SpecialTitleDto) t;
            NormalMember normalMember = dto.getGroup().get(dto.getUserId());
            assert normalMember != null;
            normalMember.setSpecialTitle("");
            Message m = new At(dto.getUserId()).plus("自定义title已失效");
            dto.getGroup().sendMessage(m);
        }
    }
}
