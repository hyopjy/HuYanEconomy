package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantGoods;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.RandomHelperUtil;
import cn.hutool.cron.task.Task;
import jodd.util.StringPool;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MysteriousMerchantOpenTask implements Task {

   private MysteriousMerchantSetting setting;

    private Integer hour;
    public MysteriousMerchantOpenTask(MysteriousMerchantSetting setting, Integer hour) {
        this.setting = setting;
        this.hour = hour;
    }

    //1.每天14点、17点、21点都有15%概率刷新神秘商人
    //2.神秘商人会在商品83~92中（暂定十个）随机上架2~4种
    //3.神秘商人上架的每种商品各有随机1~3个库存
    //4.神秘商人会在出现后的10分钟消失
    //5.每个人在神秘商人处的限购数为每次每种商品数1
    //6.神秘商人的商品会有兑换品，
    // 即：
    // 可能有币币购买、
    // 可能有赛季币购买、
    // 可能有道具兑换
    // 神秘商人的商品会有打包商品，如：100币币作为一个打包品，每次可以使用100000赛季币购买一份
    //7.具体的商品列表、回复文案请等我！！！
    @Override
    public void execute() {
        // 是否开启神秘商人
        if(!setting.getStatus()){
            return;
        }
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("WorldBossOpenTask-end. bot为空");
            return;
        }
        // 判断是否可以出现神秘商人
        List<Long> groupIdList = new ArrayList<>();
        groupIdList.add(758085692L);
        groupIdList.add(835186488L);
        groupIdList.add(878074795L);
        groupIdList.add(227265762L);
        groupIdList.forEach(groupId->{
            Group group =  bot.getGroup(groupId);
            if(Objects.isNull(group)){
                return;
            }
            taskRunByStartGroupId(groupId, bot, group);
        });

    }

    private void taskRunByStartGroupId(Long groupId, Bot bot, Group group) {
        Integer prop = setting.getProbability();
        /**
         * 判断概率
         */
        Boolean appear = RandomHelperUtil.checkRandomByProp(prop);
        if(!appear){
            return;
        }
        // 查询神秘商人系列道具
        List<PropsFishCard> propsFishCards = new ArrayList<PropsFishCard>();

        Integer startMinutes = MysteriousMerchantManager.getStartMinutes();
        Integer endMinutes = MysteriousMerchantManager.getEndMinutes(setting);

        // 生成商品列表
        List<MysteriousMerchantGoods> goodsList = new ArrayList<>();
        // 随机-几种
        // 随机库存

        String hourPad = StringUtils.leftPad(hour+"", 2, StringPool.ZERO);
        String startMinutePad = StringUtils.leftPad(startMinutes+"", 2, StringPool.ZERO);
        String endMinutePad = StringUtils.leftPad(endMinutes+"", 2, StringPool.ZERO);

        StringBuilder message = new StringBuilder("神秘商人出现！\r\n");
        message.append("时间 (" + hourPad + ":" + startMinutePad + "~" + hourPad + ":" + endMinutePad + ") \r\n");
        message.append("限购次数:" + setting.getBuyCount() +"(次) \r\n");
        message.append("出现道具如下:\r\n");
        group.sendMessage(message.toString());

    }
}
