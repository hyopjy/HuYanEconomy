package cn.chahuyun.economy.redis;

import cn.chahuyun.economy.dto.SpecialTitleDto;
import net.mamoe.mirai.contact.NormalMember;

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
        }
    }
}
