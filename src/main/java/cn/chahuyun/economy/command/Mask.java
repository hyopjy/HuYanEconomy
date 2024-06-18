package cn.chahuyun.economy.command;

import cn.chahuyun.economy.entity.team.Team;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.manager.TeamManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.chahuyun.economy.utils.RandomHelperUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 面罩
 */
public class Mask extends AbstractPropUsage {

    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String match = "使用 (" + propsCard.getName() + "|" + no + ")(\\[mirai:at:\\d+]( )*)";
        String code = event.getMessage().serializeToMiraiCode();
        if(!Pattern.matches(match, code)){
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "请输入正确的命令[使用 " + propsCard.getName() + "或者" + no + "@指定对象]"));
            return false;
        }
        // 校验使用次数是否超过限制
        if (CacheUtils.checkMaskCountKey(group.getId(), userInfo.getQq())) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "[面罩] 今日使用已达到限制"));
            return false;
        }

        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                this.target = at.getTarget();
            }
        }
        return true;
    }

    @Override
    public void excute() {
        User sender = event.getSender();
//        被打劫，队伍均摊损失；
//        打劫他人，两人获得同样的币币
//        抢劫失败罚款2000币币，如果有队友，则队友分摊一半 变成一人1000
        CacheUtils.addUserMaskCountKey(group.getId(), sender.getId());

        List<Team> teamList = TeamManager.listTeam(group.getId());
        Team senderTeam = null;
        Team targetTeam = null;
        // team 不为空
        if (CollectionUtils.isNotEmpty(teamList)) {
            // 打劫用户的team
            Optional<Team> senderTeamOption = teamList.stream().filter(team -> team.getTeamMember().equals(sender.getId()) || team.getTeamOwner().equals(sender.getId())).collect(Collectors.toList()).stream().findAny();
            if (senderTeamOption.isPresent()) {
                senderTeam = senderTeamOption.get();
            }
            // 被打劫用户 ‘
            Optional<Team> targetTeamOption = teamList.stream().filter(team -> team.getTeamMember().equals(target) || team.getTeamOwner().equals(target)).collect(Collectors.toList()).stream().findAny();
            if (targetTeamOption.isPresent()) {
                targetTeam = targetTeamOption.get();
            }
        }

        //被bobo正义执行，抢劫失败并且罚款2000币币
        if (RandomHelperUtil.checkRandomLuck1_20()) {
            if (Objects.isNull(senderTeam)) {
                EconomyUtil.minusMoneyToUser(sender, 5000);
                subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                        .append("被bobo正义执行，抢劫失败并且罚款5000币币").append("\r\n")
                        .build());
            } else {
                NormalMember memberOwner = group.get(senderTeam.getTeamOwner());
                EconomyUtil.minusMoneyToUser(memberOwner, 5000);
                NormalMember memberMember = group.get(senderTeam.getTeamMember());
                EconomyUtil.minusMoneyToUser(memberMember, 5000);
                subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                        .append("[抢劫失败]").append("\r\n")
                        .append(new At(senderTeam.getTeamOwner()).getDisplay(group)).append("\r\n")
                        .append(new At(senderTeam.getTeamMember()).getDisplay(group)).append("\r\n")
                        .append("被bobo正义执行，抢劫失败并且罚款5000币币").append("\r\n")
                        .build());
            }
        } else {
            // 20001-50000
            int money = RandomUtil.randomInt(20001, 50001);
            List<Long> plususerId = new ArrayList<>();
            List<Long> minuserId = new ArrayList<>();
            if (Objects.nonNull(senderTeam)) {
                // 加钱
                NormalMember memberOwner = group.get(senderTeam.getTeamOwner());
                EconomyUtil.plusMoneyToUser(memberOwner, money);

                NormalMember memberMember = group.get(senderTeam.getTeamMember());
                EconomyUtil.plusMoneyToUser(memberMember, money);

                plususerId.add(senderTeam.getTeamOwner());
                plususerId.add(senderTeam.getTeamMember());
            } else {
                // 自己获得
                EconomyUtil.plusMoneyToUser(sender, money);
                plususerId.add(sender.getId());
            }

            if(Objects.nonNull(targetTeam)){
                double targetMoney = NumberUtil.round(NumberUtil.div(money, 2), 2).doubleValue();
                NormalMember memberOwner = group.get(targetTeam.getTeamOwner());
                EconomyUtil.minusMoneyToUser(memberOwner, targetMoney);

                NormalMember memberMember = group.get(targetTeam.getTeamMember());
                EconomyUtil.minusMoneyToUser(memberMember, targetMoney);

                minuserId.add(targetTeam.getTeamOwner());
                minuserId.add(targetTeam.getTeamMember());
            }else {
                // 减去目标用户
                NormalMember member = group.get(target);
                EconomyUtil.minusMoneyToUser(member, money);

                minuserId.add(target);
            }

            StringBuilder plusBB = new StringBuilder("[共享" + money + "bb的用户]").append("\r\n");
            plususerId.forEach(userId->{
                plusBB.append(new At(userId).getDisplay(group)).append("\r\n");
            });

            StringBuilder minBB = new StringBuilder("[分摊" + money + "bb的用户]").append("\r\n");
            minuserId.stream().forEach(userId->{
                minBB.append(new At(userId).getDisplay(group)).append("\r\n");
            });

            subject.sendMessage(new MessageChainBuilder().append(new QuoteReply(event.getMessage()))
                    .append(propsCard.getName() + "使用成功")
                    .append("\r\n")
                    .append(plusBB.toString())
                    .append("-----").append("\r\n")
                    .append(minBB.toString())
                    .build());
        }


    }
}
