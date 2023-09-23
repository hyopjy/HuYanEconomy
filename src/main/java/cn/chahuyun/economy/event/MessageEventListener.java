package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.manager.*;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import org.redisson.api.RLock;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :消息检测
 * @Date 2022/7/9 18:11
 */
public class MessageEventListener extends SimpleListenerHost {


    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        if (exception instanceof EventCancelledException) {
            Log.error("发送消息被取消:", exception);
        } else if (exception instanceof BotIsBeingMutedException) {
            Log.error("你的机器人被禁言:", exception);
        } else if (exception instanceof MessageTooLargeException) {
            Log.error("发送消息过长:", exception);
        } else if (exception instanceof IllegalArgumentException) {
            Log.error("发送消息为空:", exception);
        }

        // 处理事件处理时抛出的异常
        Log.error(exception);
    }

    /**
     * 消息入口
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/11/14 12:34
     */
    @EventHandler()
    public void onMessage(@NotNull MessageEvent event) {
        try {
            EconomyConfig config = HuYanEconomy.INSTANCE.config;
            User sender = event.getSender();
            //主人
            boolean owner = config.getOwner() == sender.getId();
            Contact subject = event.getSubject();
            Group group = null;
            if (subject instanceof Group) {
                group = (Group) subject;
            }

            String code = event.getMessage().serializeToMiraiCode();
            PropsManager propsManager = PluginManager.getPropsManager();

            switch (code) {
                case "测试":
                    return;
                case "签到":
                case "打卡":
                case "sign":
                    Log.info("签到指令");
                    SignManager.sign(event);
                    return;
                case "个人信息":
                case "info":
                    Log.info("个人信息指令");
                    FbUserManager.getUserInfoImageFb(event);
                    return;
                case "背包":
                case "backpack":
                    Log.info("背包指令");
                    propsManager.viewUserBackpack(event);
                    return;
                case "道具商店":
                case "shops":
                    Log.info("道具商店指令");
                    propsManager.propStore(event);
                    return;
                case "开启 猜签":
                    if (owner) {
                        Log.info("管理指令");
                        if (group != null && !config.getLotteryGroup().contains(group.getId())) {
                            EconomyConfig.INSTANCE.getLotteryGroup().add(group.getId());
                        }
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的猜签功能已开启!"));
                    }
                    return;
                case "关闭 猜签":
                    if (owner) {
                        Log.info("管理指令");
                        if (group != null && config.getLotteryGroup().contains(group.getId())) {
                            EconomyConfig.INSTANCE.getLotteryGroup().remove(group.getId());
                        }
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的猜签功能已关闭!"));
                    }
                    return;
                case "开启 钓鱼":
                    if (owner) {
                        Log.info("管理指令");
                        if (group != null && !config.getFishGroup().contains(group.getId())) {
                            EconomyConfig.INSTANCE.getFishGroup().add(group.getId());
                        }
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的钓鱼功能已开启!"));
                    }
                    return;
                case "关闭 钓鱼":
                    if (owner) {
                        Log.info("管理指令");
                        if (group != null && config.getFishGroup().contains(group.getId())) {
                            EconomyConfig.INSTANCE.getFishGroup().remove(group.getId());
                        }
                        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "本群的钓鱼功能已关闭!"));
                    }
                    return;
                case "购买鱼竿":
                    Log.info("游戏指令");
                    GamesManager.buyFishRod(event);
                    return;
                case "钓鱼":
                case "抛竿":
                    Log.info("游戏指令");
                    if (group != null && config.getFishGroup().contains(group.getId())) {
                        if(CacheUtils.checkAutomaticFishBuff(group.getId(),sender.getId())){
                            Double constMoney = GamesManager.userPay.get(sender.getId());
                            Boolean checkUser = GamesManager.checkUserPay(event.getSender());
                            if (checkUser) {
                                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "岛岛全自动钓鱼机生效中，手动钓鱼失效！," +
                                        "还你%s💰", Optional.ofNullable(constMoney).orElse(0.0)));
                            } else {
                                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "岛岛全自动钓鱼机生效中，手动钓鱼失效！"));
                            }
                            return;
                        }
                        RLock lock = RedisUtils.getFishLock(group.getId(), sender.getId());
                        boolean b = lock.tryLock(3, 60 * 60, TimeUnit.SECONDS);
                        try {
                            if (b) {
                                GamesManager.fishing(event);
                            } else {
                                Double constMoney = GamesManager.userPay.get(sender.getId());
                                Boolean checkUser = GamesManager.checkUserPay(event.getSender());
                                if (checkUser) {
                                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了," +
                                            "还你%s💰", Optional.ofNullable(constMoney).orElse(0.0)));
                                } else {
                                    subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "你已经在钓鱼了！"));
                                }
                            }
                        } catch (Exception e) {
                            Log.error("游戏指令-钓鱼error:" + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // 解锁前检查当前线程是否持有该锁
                            if (lock != null && lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        }
                    }
                    return;
                case "升级鱼竿":
                    Log.info("游戏指令");
                    GamesManager.upFishRod(event);
                    return;
                case "钓鱼排行榜":
                case "钓鱼排行":
                case "钓鱼榜":
                    Log.info("游戏指令");
                    GamesManager.fishTop(event);
                    return;
                case "鱼竿等级":
                    Log.info("游戏指令");
                    GamesManager.viewFishLevel(event);
                    return;
                case "刷新钓鱼":
                    Log.info("游戏指令");
                    if (owner) {
                        Log.info("owner");
                        GamesManager.refresh(event);
                    } else {
                        Log.info("sender");
                        GamesManager.refresh(event, sender.getId());
                    }
                    return;
                case "银行利率":
                    Log.info("银行指令");
                    BankManager.viewBankInterest(event);
                    return;

            }

            String buyPropRegex = "购买 (\\S+)( \\S+)?|buy (\\S+)( \\S+)?";
            if (Pattern.matches(buyPropRegex, code)) {
                Log.info("购买指令");
                propsManager.buyPropFromStore(event);
                return;
            }

            String exchangePropRegex = "兑换 (\\S+)( \\S+)?|buy (\\S+)( \\S+)?";
            if (Pattern.matches(exchangePropRegex, code)) {
                Log.info("兑换指令");
                propsManager.exchangePropFromStore(event);
                return;
            }

            String userPropRegex = "使用 (\\S+)(( \\S+)|(\\[mirai:at:\\d+]( )*))?|use (\\S+)(( \\S+)|(\\[mirai:at:\\d+]))?";
            if (Pattern.matches(userPropRegex, code)) {
                Log.info("使用指令");
                try {
                    propsManager.userProp(event);
                }catch (Exception e){
                    Log.error("[使用指令]发生异常：" + e.getMessage());
                }

                return;
            }

            String sellPropRegex = "出售 (\\S+)( \\S+)?|sell (\\S+)( \\S+)?";
            if (Pattern.matches(sellPropRegex, code)) {
                Log.info("出售指令");
                propsManager.sellPropFromStore(event);
                return;
            }

            String buyLotteryRegex = "猜签 (\\d+|\\S+)( \\d+)|lottery (\\d+|\\S+)( \\d+)";
            if (Pattern.matches(buyLotteryRegex, code)) {
                Log.info("彩票指令");
                if (group != null && config.getLotteryGroup().contains(group.getId())) {
                    LotteryManager.addLottery(event);
                }
                return;
            }
            String userToUserTransferRegex = "转账(\\[mirai:at:\\d+])? (\\d+(\\d+|\\.\\d)*)?";
            if (Pattern.matches(userToUserTransferRegex, code)) {
                Log.info("转账指令");
                TransferManager.userToUser(event);
                return;
            }

            //String walletToBankRegex = "存款 (\\d+(\\d+|\\.\\d)*)?|deposit (\\d+(\\d+|\\.\\d)*)?";
            String bankToWalletRegex = "取款 (\\d+(\\d+|\\.\\d)*)?|withdraw (\\d+(\\d+|\\.\\d)*)?";
           // if (Pattern.matches(walletToBankRegex, code)) {
//                Log.info("银行指令");
//                BankManager.deposit(event);
//                return;
//            } else
            if (Pattern.matches(bankToWalletRegex, code)) {
                Log.info("银行指令");
                BankManager.withdrawal(event);
                return;
            }


        } catch (Exception exception) {
            Log.error("发生异常！！！:" + exception.getMessage());
        }
//        {
//            if (group == null) {
//                return;
//            }
//            String regex = "转账\\s+(@?\\d+)\\s+(\\d+)";
//            //  String s = "转账    2482065472    12";
//            Matcher matcher = Pattern.compile(regex).matcher(event.getMessage().contentToString());
//            System.out.println(event.getMessage().contentToString());
//            MessageChainBuilder messages = new MessageChainBuilder();
//            if (matcher.matches()) {
//                int money = Integer.parseInt(matcher.group(2));
//                long toId = Long.parseLong(matcher.group(1).replaceAll("@", ""));
//                messages.append(TransferManager.transfer(event.getSender(), group.get(toId), money));
//                event.getSubject().sendMessage(messages.build());
//            }
//            return;
//        }

    }

}
