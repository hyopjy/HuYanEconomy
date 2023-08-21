package cn.chahuyun.economy.command;

import cn.chahuyun.economy.dto.DifficultyBuffDto;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.MessageUtil;
import net.mamoe.mirai.contact.Contact;

import java.util.regex.Pattern;

/**
 * 年年有鱼 消耗品，使用后获得「年年有鱼」buff，之后的5次钓鱼都会额外增加difficultymin50，rankmin5
 */
public class FiveFlavoredFish extends AbstractPropUsage {

    @Override
    public boolean checkOrder() {
        this.isBuff = Boolean.TRUE;

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
        // 消耗品，使用后获得「年年有鱼」buff，之后的5次钓鱼都会额外增加difficultymin50，rankmin5
        DifficultyBuffDto difficultyBuffDto = new DifficultyBuffDto();
        difficultyBuffDto.setBuffName(propsCard.getName());
        difficultyBuffDto.setDifficultyMin(50);
        difficultyBuffDto.setRankMin(5);
        difficultyBuffDto.setCount(5);
        //
        CacheUtils.addFishCardKey(group.getId(), subject.getId(), propsCard.getName(), difficultyBuffDto);
        // 监听者模式 移除 BuffCacheValue  FishCardKey
        CacheUtils.addBuffCacheValue(group.getId(), subject.getId(), propsCard.getName());
    }

}
