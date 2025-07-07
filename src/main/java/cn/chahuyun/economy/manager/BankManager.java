package cn.chahuyun.economy.manager;

import cn.chahuyun.config.EconomyPluginConfig;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.bank.BankInfo;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.HibernateUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import xyz.cssxsh.mirai.economy.service.EconomyAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 银行管理<p>
 * 存款|取款<p>
 *
 * @author Moyuyanli
 * @date 2022/11/14 12:26
 */
public class BankManager {

    private BankManager() {
    }

    /**
     * 初始化银行<p>
     * 应当开启银行利息定时器<p>
     *
     * @author Moyuyanli
     * @date 2022/12/21 11:03
     */
    public static void init() {
        if (EconomyPluginConfig.INSTANCE.getFirstStart()) {
            BankInfo bankInfo = new BankInfo("global", "主银行", "经济服务", HuYanEconomy.config.getOwner(), 0);
            bankInfo.setId(1);
            bankInfo.save();
        }
        List<BankInfo> bankInfos = null;
        try {
            bankInfos = HibernateUtil.factory.fromSession(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<BankInfo> query = builder.createQuery(BankInfo.class);
                query.select(query.from(BankInfo.class));
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            Log.error("银行管理:利息加载出错!", e);
        }
        CronUtil.remove("bank");
        BankInterestTask bankInterestTask = new BankInterestTask("bank", bankInfos);
        CronUtil.schedule("bank", "0 0 0 * * ?", bankInterestTask);
        // CronUtil.schedule("bank", "*/30 * * * * *", bankInterestTask);
    }



    /**
     * 存款<p>
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/21 11:04
     */
    public static void deposit(MessageEvent event) {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();

        Contact subject = event.getSubject();

        MessageChain message = event.getMessage();
        MessageChainBuilder singleMessages = MessageUtil.quoteReply(message);
        String code = message.serializeToMiraiCode();


        double money = Double.parseDouble(code.split(" ")[1]);
        double moneyByUser = EconomyUtil.getMoneyByUser(user);
        if (moneyByUser - money < 0) {
            singleMessages.append(String.format("你的币币不够%s了", money));
            subject.sendMessage(singleMessages.build());
            return;
        }

        if (EconomyUtil.turnUserToBank(user, money)) {
            singleMessages.append("存款成功!");
            subject.sendMessage(singleMessages.build());
        } else {
            singleMessages.append("存款失败!");
            subject.sendMessage(singleMessages.build());
            Log.error("银行管理:存款失败!");
        }
    }

    /**
     * 取款<p>
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/21 11:04
     */
    public static void withdrawal(MessageEvent event) {
        UserInfo userInfo = UserManager.getUserInfo(event.getSender());
        User user = userInfo.getUser();

        Contact subject = event.getSubject();

        MessageChain message = event.getMessage();
        MessageChainBuilder singleMessages = MessageUtil.quoteReply(message);
        String code = message.serializeToMiraiCode();


        double money = Double.parseDouble(code.split(" ")[1]);
        double moneyByBank = EconomyUtil.getMoneyByBank(user);
        if (moneyByBank - money < 0) {
            singleMessages.append(String.format("你的银行余额不够%s枚WDIT币币了", money));
            subject.sendMessage(singleMessages.build());
            return;
        }

        if (EconomyUtil.turnBankToUser(user, money)) {
            singleMessages.append("取款成功!");
            subject.sendMessage(singleMessages.build());
        } else {
            singleMessages.append("取款失败!");
            subject.sendMessage(singleMessages.build());
            Log.error("银行管理:取款失败!");
        }
    }

    /**
     * 查看利率
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/12/23 16:08
     */
    public static void viewBankInterest(MessageEvent event) {
        BankInfo bankInfo = HibernateUtil.factory.fromSession(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<BankInfo> query = builder.createQuery(BankInfo.class);
            JpaRoot<BankInfo> from = query.from(BankInfo.class);
            query.select(from);
            query.where(builder.equal(from.get("id"), 1));
            return session.createQuery(query).getSingleResult();
        });
        event.getSubject().sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "今日银行利率是%s%%", bankInfo.getInterest()));
    }

}


/**
 * 银行的利息管理
 *
 * @author Moyuyanli
 * @date 2022/12/23 9:22
 */
class BankInterestTask implements Task {


    private final String id;
    /**
     * 银行信息
     */
    private final List<BankInfo> bankList;

    public BankInterestTask(String id, List<BankInfo> bankList) {
        this.id = id;
        this.bankList = bankList;
    }

    /**
     * 执行作业
     * <p>
     * 作业的具体实现需考虑异常情况，默认情况下任务异常在监听中统一监听处理，如果不加入监听，异常会被忽略<br>
     * 因此最好自行捕获异常后处理
     */
    @Override
    public void execute() {
        for (BankInfo bankInfo : bankList) {
            if (bankInfo.isInterestSwitch()) {
                if (DateUtil.thisDayOfWeek() == 2) {
                    bankInfo.setInterest(RandomUtil.randomInt(2, 9));
                }
            }
            Log.info("bankInfo-id:"+ bankInfo.getId());
            if (bankInfo.getId() == 1) {
                int interest = bankInfo.getInterest();
                // 更改为获取余额
                // Map<EconomyAccount, Double> accountByBank = EconomyUtil.getAllAccount();
                // 枫叶账户
                Map<EconomyAccount, Double> accountByBank = EconomyUtil.getAccountByBank();
                for (Map.Entry<EconomyAccount, Double> entry : accountByBank.entrySet()) {
                    UserInfo userInfo = UserManager.getUserInfo(entry.getKey());
                    if (userInfo == null) {
                        continue;
                    }
                    double dd = interest / 100.00;
                    BigDecimal vB = NumberUtil.round(NumberUtil.mul(entry.getValue().doubleValue(),dd),2);
                    double v = vB.doubleValue();

//                    if (EconomyUtil.plusMoneyToBankForAccount(entry.getKey(), v)) {
                     // bank 是赛季币
                     // 余额是 wditBB
                     if (EconomyUtil.minusMoneyToBankForAccount(entry.getKey(), v)) {
                        Log.info("用户："+ userInfo.getQq() + "获得"+ SeasonCommonInfoManager.getSeasonMoney()+"：" + v);
                        userInfo.setBankEarnings(v);
                        userInfo.save();
                    } else {
                        Log.error(SeasonCommonInfoManager.getSeasonMoney() + "管理:" + id + "添加利息出错");
                    }
                }
            }
        }
    }
}

