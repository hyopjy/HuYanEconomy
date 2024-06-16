package cn.chahuyun.economy.manager;

import cn.chahuyun.config.EconomyConfig;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.bank.Bank;
import cn.chahuyun.economy.entity.bank.action.Transfer;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 转账管理<p>
 * 转账|抢劫<p>
 *
 * @author Moyuyanli
 * @date 2022/11/14 12:27
 */
public class TransferManager {


    private TransferManager() {

    }

    /**
     * 用户转账给另一个用户操作<p>
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/9 21:06
     */
    public static void bankTobank(MessageEvent event) {
        Contact subject = event.getSubject();
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();
        Group group = null;

        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();

        String[] s = code.split(" ");
        long qq = 0;
        double money;
        if (s.length == 2) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
            money = Double.parseDouble(s[s.length - 1]);
        } else {
            qq = Long.parseLong(s[1]);
            money = Long.parseLong(s[2]);
        }

        if (0 > money || user.getId() == qq) {
            subject.sendMessage("\uD83E\uDD14嗯？");
            return;
        }

        if (subject instanceof Group) {
            group = (Group) subject;
        }

        if (group == null || qq == 0) {
            subject.sendMessage("转账失败！");
            return;
        }

        // new 转账用户当前赛季币
        turnMoneyBank(event, subject, user, group, money, qq);

       // turnMoneyUser(event, subject, user, group, money, member, amount);

    }

    private static void turnMoneyUser(MessageEvent event, Contact subject, User user, Group group, double money, Long qq) {
        NormalMember member = group.get(qq);
        // 获取转账金额 - money
        // 邮电费
        BigDecimal amountBig = NumberUtil.round(NumberUtil.mul(money, 0.0), 2);
        double amount = amountBig.doubleValue();

        // 转账用户当前金额
        double userMoney = EconomyUtil.getMoneyByUser(user);
        if (userMoney < money + amount) {
            userMoney = EconomyUtil.getMoneyByBank(user);
            if (userMoney < money + amount) {
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "余额不足！"));
                return;
            } else {
                // 银行有钱，但余额没钱
                // 将银行的钱转移到余额，在通过余额转给被转账用户
                EconomyUtil.turnBankToUser(user, money + amount);
            }
        }


        // 用户账号扣除 money + amount  102 - 98
        // 被转账用户收到  money - amount
        // 管理员收到 double amount
        double finalMoney = money - amount;
        if (EconomyUtil.turnUserToUser(user, member, finalMoney)) {
            // 给管理员转账
            NormalMember admin = group.get(EconomyConfig.INSTANCE.getOwner());
            if (Objects.nonNull(admin)) {
                if (EconomyUtil.turnUserToUser(user, admin, amount)) {
                    Log.info("转账管理:管理员转账成功");
                }
            }
            assert member != null;
            String name = member.getNameCard();
            if (StrUtil.isBlank(name)) {
                name = member.getNick();
            }
            // 成功转账100WDIT币币（额外向您收取2手续费），MM获得98（额外向她收取2手续费）
            // 成功转账%sWDIT币币（额外消耗%s邮电费），%s获得%s（额外收取%s手续费）
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), String.format("成功转账%sWDIT币币（额外消耗%s邮电费），%s获得%s（额外收取%s手续费)",
                    money, amount, name, finalMoney, amount)));
        } else {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "转账失败！请联系管理员!"));
            Log.error("转账管理:用户金币转移失败");
        }
    }

    private static void turnMoneyBank(MessageEvent event, Contact subject, User user, Group group, double money, Long qq) {
        NormalMember member = group.get(qq);
        // 获取转账金额 - money
        // 邮电费
        BigDecimal amountBig = NumberUtil.round(NumberUtil.mul(money,0.02),2);
        double amount = amountBig.doubleValue();
        double userMoney = EconomyUtil.getMoneyByBank(user);
        if (userMoney < money + amount) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "余额不足！"));
            return;
        }
        // 用户账号扣除 money + amount  102 - 98
        // 被转账用户收到  money - amount
        // 管理员收到 double amount
        double finalMoney = money - amount;
        if (EconomyUtil.turnBankToBank(user, member, finalMoney)) {
            // 给管理员转账
            NormalMember admin = group.get(EconomyConfig.INSTANCE.getOwner());
            if (Objects.nonNull(admin)) {
                if (EconomyUtil.turnBankToBank(user, admin, amount)) {
                    Log.info("转账管理:管理员转账成功");
                }
            }
            assert member != null;
            String name = member.getNameCard();
            if (StrUtil.isBlank(name)) {
                name = member.getNick();
            }
            // 成功转账100WDIT币币（额外向您收取2手续费），MM获得98（额外向她收取2手续费）
            // 成功转账%sWDIT币币（额外消耗%s邮电费），%s获得%s（额外收取%s手续费）
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), String.format("成功转账%s"+ SeasonCommonInfoManager.getSeasonMoney()+"（额外消耗%s邮电费），%s获得%s（额外收取%s手续费)",
                    money, amount, name, finalMoney, amount)));
        } else {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "转账失败！请联系管理员!"));
            Log.error("转账管理:用户金币转移失败");
        }
    }

    public static String transfer(User originUser, User toUser, int money) {
        try {
            Bank.INSTANCE.execute(new Transfer(originUser, toUser, money));
        } catch (Exception e) {
            return e.getMessage();
        }
        return "转帐成功";
    }

    public static void userToUser(MessageEvent event) {
        Contact subject = event.getSubject();
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();
        Group group = null;

        MessageChain message = event.getMessage();
        String code = message.serializeToMiraiCode();

        String[] s = code.split(" ");
        long qq = 0;
        double money;
        if (s.length == 2) {
            for (SingleMessage singleMessage : message) {
                if (singleMessage instanceof At) {
                    At at = (At) singleMessage;
                    qq = at.getTarget();
                }
            }
            money = Double.parseDouble(s[s.length - 1]);
        } else {
            qq = Long.parseLong(s[1]);
            money = Long.parseLong(s[2]);
        }

        if (0 > money || user.getId() == qq) {
            subject.sendMessage("\uD83E\uDD14嗯？");
            return;
        }

        if (subject instanceof Group) {
            group = (Group) subject;
        }

        if (group == null || qq == 0) {
            subject.sendMessage("转账失败！");
            return;
        }

         turnMoneyUser(event, subject, user, group, money, qq);

    }

    public static void subUseMoney(UserMessageEvent event, Long groupId, Long qq, Double bbCount) {
        Contact subject = event.getSubject();

        // 获取机器人所在群聊
        Group group = event.getBot().getGroup(groupId);
        if(Objects.isNull(group)){
            subject.sendMessage("机器人暂未加入该群聊：" + groupId);
            return;
        }
        // 指定用户
        NormalMember member = group.get(qq);
        if(Objects.isNull(member)){
            subject.sendMessage("指定用户不存在：" + qq);
            return;
        }
        // 扣减
        EconomyUtil.minusMoneyToUser(member, bbCount);
        subject.sendMessage( "扣减" + qq + "--"+ bbCount + "成功");
    }
}
