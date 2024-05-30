package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantGoods;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantSetting;
import cn.chahuyun.economy.entity.merchant.MysteriousMerchantShop;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.RandomHelperUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.task.Task;
import jodd.util.StringPool;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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
            taskRunByStartGroupId(groupId, group);
        });

    }

    private void taskRunByStartGroupId(Long groupId, Group group) {
        Integer probability = setting.getProbability();
        /**
         * 判断概率
         */
        Boolean appear = RandomHelperUtil.checkRandomByProp(probability);
        if(!setting.getStatus() || !appear){
            return;
        }
        // 限购次数
        Integer buyCount = setting.getBuyCount();
        // 上架商品列表
        List<String> goodCodeList = Arrays.asList(setting.getGoodCodeStr().split(Constant.SPILT));
        // 随机商品数量
        Integer randomGoodCount = setting.getRandomGoodCount();
        // 随机库存最小
        Integer minStored = setting.getMinStored();
        // 随机库存最大
        Integer maxStored = setting.getMaxStored();

        Integer startMinutes = MysteriousMerchantManager.getStartMinutes();
        Integer endMinutes = MysteriousMerchantManager.getEndMinutes(setting);

        // 获取商店商品信息
        List<MysteriousMerchantShop> shopGoodsList = MysteriousMerchantManager.getMysteriousMerchantShopByGoodCodeList(goodCodeList);

        List<MysteriousMerchantShop> upshopGoodsList = new ArrayList<>();
        if(CollectionUtils.isEmpty(shopGoodsList)){
            return;
        }
        if(shopGoodsList.size() <= setting.getRandomGoodCount()){
            upshopGoodsList = shopGoodsList;
        }else {
            // 随机-几种
            // 首先打乱列表
            Collections.shuffle(shopGoodsList);
            // 然后从打乱后的列表中获取前 numberOfRandomElements 个元素
            upshopGoodsList = shopGoodsList.subList(0, randomGoodCount)
                   .stream().sorted(Comparator.comparing(MysteriousMerchantShop::getGoodCode))
                   .collect(Collectors.toList());
        }

        List<MysteriousMerchantGoods> goodUpList = new ArrayList<>(upshopGoodsList.size());
        upshopGoodsList.forEach(shopGood->{
            MysteriousMerchantGoods goods = new MysteriousMerchantGoods();
            goods.setSettingId(setting.getSettingId());
            goods.setGroupId(groupId);
            goods.setGoodCode(shopGood.getGoodCode());
            Integer store = RandomUtil.randomInt(minStored, maxStored + 1);
            goods.setGoodStored(store);
            goods.setSold(0);
            goods.setHour(hour);
            goods.setStartMinutes(startMinutes);
            goods.setEndMinutes(endMinutes);
            goodUpList.add(goods);
        });

        // 新增前 先删除 hour+minutes的配置
        MysteriousMerchantManager.deleteGoodBySettingId(setting.getSettingId(), groupId);
        // 保存商品信息
        MysteriousMerchantManager.saveGoodUpList(goodUpList);


        String hourPad = StringUtils.leftPad(hour+"", 2, StringPool.ZERO);
        String startMinutePad = StringUtils.leftPad(startMinutes+"", 2, StringPool.ZERO);
        String endMinutePad = StringUtils.leftPad(endMinutes+"", 2, StringPool.ZERO);

        StringBuilder message = new StringBuilder("神秘商人出现！\r\n");
        message.append("时间 (" + hourPad + ":" + startMinutePad + "~" + hourPad + ":" + endMinutePad + ") \r\n");
        message.append("限制兑换次数:" + buyCount +"(次) \r\n");
        message.append("出现可兑换道具如下:\r\n");
        List<MysteriousMerchantShop> finalUpshopGoodsList = upshopGoodsList;
        goodUpList.stream().forEach(good->{
            Optional<MysteriousMerchantShop> shopGoodOptional = finalUpshopGoodsList.stream()
                    .filter(shop-> good.getGoodCode().equals(shop.getGoodCode())).findFirst();
            if(!shopGoodOptional.isPresent()){
                return;
            }
            message.append("-----------------------\r\n");
            MysteriousMerchantShop shopGood = shopGoodOptional.get();
            message.append("商品信息:\r\n");
            message.append("编码： [" + shopGood.getGoodCode() + "] \r\n");
            PropsBase props1Base = PropsCardFactory.INSTANCE.getPropsBase(shopGood.getProp1Code());
            message.append("道具名：" + props1Base.getName() + "\r\n");
            message.append("兑换条件:\r\n");
            // 道具兑换
            if(shopGood.getChangeType().equals(MysteriousMerchantManager.CHANGE_TYPE_PROP)){
                PropsBase propsBase = PropsCardFactory.INSTANCE.getPropsBase(shopGood.getProp2Code());
                message.append(propsBase.getName() + "(" + shopGood.getProp2Count() + ") \r\n");
            }
            // bb兑换
            if(shopGood.getChangeType().equals(MysteriousMerchantManager.CHANGE_TYPE_BB)){
                message.append(SeasonCommonInfoManager.getBBMoney() + "(" + shopGood.getBbCount() + ") \r\n");
            }
            // 赛季币兑换
            if(shopGood.getChangeType().equals(MysteriousMerchantManager.CHANGE_TYPE_SEASON)){
                message.append(SeasonCommonInfoManager.getSeasonMoney() + "(" + shopGood.getSeasonMoney() + ") \r\n");
            }
            message.append("商品库存: " + good.getGoodStored() + "\r\n");
//            message.append("已售出: " + good.getSold()+ "\r\n");

            message.append("-----------------------\r\n");
        });
        group.sendMessage(message.toString());

    }
}
