package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.manager.*;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.strategy.RodeoFactory;
import cn.chahuyun.economy.strategy.RodeoStrategy;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.DateUtil;
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

import java.util.Objects;
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
                    SeasonManager.checkUserDailyWork(event, subject);
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

            // 工作日全天，所有购买道具和使用道具（27除外）指令都失效
            String buyPropRegex = "购买 (\\S+)( \\S+)?|buy (\\S+)( \\S+)?";
            if (Pattern.matches(buyPropRegex, code)) {
                Log.info("购买指令");
                try {
                    if (DateUtil.checkPropDate(code)) {
                        propsManager.buyPropFromStore(event);
                    } else {
                        Log.info("购买指令: 工作日失效");
                    }
                } catch (Exception e) {
                    Log.error("[购买指令]发生异常：" + e.getMessage());
                }
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
                propsManager.userProp(event);
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
            String bankTobankTransferRegex = "转账(\\[mirai:at:\\d+])? (\\d+(\\d+|\\.\\d)*)?";
            if (Pattern.matches(bankTobankTransferRegex, code)) {
                Log.info("转账-银行指令");
                TransferManager.bankTobank(event);
                return;
            }
            String userToUserTransferRegex = "转账币币(\\[mirai:at:\\d+])? (\\d+(\\d+|\\.\\d)*)?";
            if (Pattern.matches(userToUserTransferRegex, code)) {
                if(event.getSender().getId() == EconomyConfig.INSTANCE.getOwner()){
                    Log.info("转账币币指令");
                    TransferManager.userToUser(event);
                }

                return;
            }

            //String walletToBankRegex = "存款 (\\d+(\\d+|\\.\\d)*)?|deposit (\\d+(\\d+|\\.\\d)*)?";
//            String bankToWalletRegex = "取款 (\\d+(\\d+|\\.\\d)*)?|withdraw (\\d+(\\d+|\\.\\d)*)?";
           // if (Pattern.matches(walletToBankRegex, code)) {
//                Log.info("银行指令");
//                BankManager.deposit(event);
//                return;
//            } else
//            if (Pattern.matches(bankToWalletRegex, code)) {
//                Log.info("银行指令");
//                BankManager.withdrawal(event);
//                return;
//            }

            String setSpecialAchievements = "特殊成就(\\[mirai:at:\\d+])? (\\S+)?";
            if (EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())
                    && Pattern.matches(setSpecialAchievements, code)) {
                Log.info("特殊成就");
                BadgeInfoManager.setSpecialAchievements(event);
                return;
            }


            String createTeam = "组队(\\[mirai:at:\\d+]( )*)? (\\S+)?|team(\\[mirai:at:\\d+]( )*)? (\\S+)?";
            if (Pattern.matches(createTeam, code)) {
                Log.info("组队");
                TeamManager.createTeam(event);
                return;
            }
            String joinTeam = "确认组队(\\[mirai:at:\\d+]( )*)?|ok(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(joinTeam, code)) {
                Log.info("确认组队");
                TeamManager.joinTeam(event);
                return;
            }


            if (code.equals("组队列表") || code.equals("list")) {
                Log.info("组队列表");
                TeamManager.list(event);
                return;
            }

            String deleteTeam = "解散(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(deleteTeam, code)) {
                Log.info("解散");
                TeamManager.deleteTeam(event);
                return;
            }

            String leveTeam = "确认解散(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(leveTeam, code)) {
                Log.info("确认解散");
                TeamManager.leveTeam(event);
                return;
            }

//            交易 道具 3 币币 10086@934415751
//            交易 道具 2 雪花 10068@934415751
//            交易 道具 1 道具 4@934415751
            // https://mywulian.com/tool/regex
            String transactionProp = "交易 (\\S+) (\\S+) (\\S+) (\\S+)(\\[mirai:at:\\d+]( )*)?|deal (\\S+) (\\S+) (\\S+) (\\S+)(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(transactionProp, code)) {
                Log.info("交易道具");
                TransactionManager.transactionProp(event);
                return;
            }

            String confirmTransactionProp = "确认交易(\\[mirai:at:\\d+]( )*)?|confirm(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(confirmTransactionProp, code)) {
                Log.info("确认交易");
                TransactionManager.confirmTransaction(event);
                return;
            }
            String refuseTransactionProp = "拒绝交易(\\[mirai:at:\\d+]( )*)?|refuse(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(refuseTransactionProp, code)) {
                Log.info("拒绝交易");
                TransactionManager.refuseTransaction(event);
                return;
            }

            String cancelTransactionProp = "取消交易(\\[mirai:at:\\d+]( )*)?|cancel(\\[mirai:at:\\d+]( )*)?";
            if (Pattern.matches(cancelTransactionProp, code)) {
                Log.info("取消交易");
                TransactionManager.cancelTransaction(event);
                return;
            }
            String listTransactionProp = "交易列表|deal list";
            if (Pattern.matches(listTransactionProp, code)) {
                Log.info("查看交易列表");
                TransactionManager.listTransaction(event);
                return;
            }

            // 抢购 SS01
            String exchangeShopRegex = "抢购 (\\S+)?|buying (\\S+)?";
            if (Pattern.matches(exchangeShopRegex, code)) {
                Log.info("神秘商店兑换");
                MysteriousMerchantManager.exchange(event);
                return;
            }

            //    决斗
            // 开启决斗 groupId 场次名称 2024-08-23 15:18-14:38 934415751,952746839 5
            // 开启轮盘 groupId 场次名称 2024-08-23 15:18-14:38 934415751,952746839,123456,788522
            // 开启大乱斗 groupId 场次名称 2024-08-23 15:18-14:38 934415751,952746839,123456,788522
            if ((code.startsWith("开启决斗") || code.startsWith("开启轮盘") || code.startsWith("开启大乱斗"))
                    && EconomyEventConfig.INSTANCE.getEconomyLongByRandomAdmin().contains(sender.getId())) {
                String[] messageArr = code.split(" ");
                RodeoStrategy strategy = RodeoFactory.createRodeoDuelStrategy(messageArr[0]);
                if (Objects.isNull(strategy)) {
                    subject.sendMessage("请输入正确命令");
                    return;
                }
                // UserMessageEvent event
                Rodeo rodeo = strategy.checkOrderAndGetRodeo(event, messageArr);
                if (Objects.isNull(rodeo)) {
                    return;
                }
            }
        } catch (Exception exception) {
            Log.error("发生异常！！！:" + exception.getMessage());
        }

    }

}
