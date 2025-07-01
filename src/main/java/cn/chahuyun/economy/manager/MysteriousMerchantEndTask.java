package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantGoods;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantShop;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.cron.task.Task;
import jodd.util.StringPool;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MysteriousMerchantEndTask implements Task {

    MysteriousMerchantSetting setting;

    private Integer hour;

    public MysteriousMerchantEndTask(MysteriousMerchantSetting setting, Integer hour) {
        this.setting = setting;
        this.hour = hour;
    }

    // 结束时-删除商品信息
    @Override
    public void execute() {
        Bot bot = HuYanEconomy.INSTANCE.getBotInstance();
        if(Objects.isNull(bot)){
            Log.info("WorldBossOpenTask-end. bot为空");
            return;
        }

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
            taskRunByEndGroupId(groupId, bot, group);
        });
    }

    private void taskRunByEndGroupId(Long groupId, Bot bot, Group group) {
        List<MysteriousMerchantGoods> goodUpList = MysteriousMerchantManager.getGoodBySettingId(setting.getSettingId(), groupId);
        // 查询是否有商品信息
        if (!setting.getStatus() || CollectionUtils.isEmpty(goodUpList)) {
//            StringBuilder message = new StringBuilder("“球球来了！快跑！”\r\n" +
//                    "“什么啊，我这些都是取的，神秘商人的事，怎么能算偷呢？”\r\n");
            // 删除
            MysteriousMerchantManager.deleteGoodBySettingId(setting.getSettingId(), groupId);

//            group.sendMessage(message.toString());
            return;
        }
        // 限购次数
        Integer buyCount = setting.getBuyCount();

        Integer startMinutes = MysteriousMerchantManager.getStartMinutes();
        Integer endMinutes = MysteriousMerchantManager.getEndMinutes(setting);

        String hourPad = StringUtils.leftPad(hour+"", 2, StringPool.ZERO);
        String startMinutePad = StringUtils.leftPad(startMinutes+"", 2, StringPool.ZERO);
        String endMinutePad = StringUtils.leftPad(endMinutes+"", 2, StringPool.ZERO);

        List<String> goodCodeList = goodUpList.stream().map(MysteriousMerchantGoods::getGoodCode).collect(Collectors.toList());
        List<MysteriousMerchantShop> shopGoodsList = MysteriousMerchantManager.getMysteriousMerchantShopByGoodCodeList(goodCodeList);

        // 根据群聊播报-当前商品剩余量 及 兑换人列表
        StringBuilder message = new StringBuilder("“球球来了！快跑！”\r\n" +
                "“什么啊，我这些都是取的，神秘商人的事，怎么能算偷呢？”\r\n");
//        message.append("时间 (" + hourPad + ":" + startMinutePad + "~" + hourPad + ":" + endMinutePad + ") \r\n");
//        message.append("限制兑换次数:" + buyCount +"(次) \r\n");
//        message.append("兑换道具剩余情况如下:\r\n");
        goodUpList.stream().forEach(good->{
            Optional<MysteriousMerchantShop> shopGoodOptional = shopGoodsList.stream()
                    .filter(shop-> good.getGoodCode().equals(shop.getGoodCode())).findFirst();
            if(!shopGoodOptional.isPresent()){
                return;
            }
            message.append("-----------------------\r\n");
            MysteriousMerchantShop shopGood = shopGoodOptional.get();
            message.append("商品信息:\r\n");
            message.append("编码： [" + shopGood.getGoodCode() + "] \r\n");
            PropsBase props1Base = PropsCardFactory.INSTANCE.getPropsBase(shopGood.getProp1Code());
            message.append("道具名：" + props1Base.getName() + " x " +  shopGood.getProp1Count()  + "\r\n");
            message.append("商品库存: " + good.getGoodStored() + "\r\n");
            message.append("已兑换: " + good.getSold()+ "\r\n");
            // todo 兑换人列表
            message.append("-----------------------\r\n");
        });

        // 删除
        MysteriousMerchantManager.deleteGoodBySettingId(setting.getSettingId(), groupId);

        group.sendMessage(message.toString());
    }
}
