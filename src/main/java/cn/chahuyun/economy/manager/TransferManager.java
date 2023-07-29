package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.bank.Bank;
import cn.chahuyun.economy.entity.bank.action.Transfer;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;

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

        NormalMember member = group.get(qq);

        MessageChainBuilder chainBuilder = new MessageChainBuilder();
        if (EconomyUtil.turnUserToUser(user, member, money)) {
            assert member != null;
            String name = member.getNameCard();
            if(StrUtil.isBlank(name)){
                name = member.getNick();
            }
            chainBuilder.append(String.format("成功向%s转账%sWDIT币币", name, money));
            subject.sendMessage(chainBuilder.build());
        } else {
            subject.sendMessage("转账失败！请联系管理员!");
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
}
