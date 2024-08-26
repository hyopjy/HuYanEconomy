package cn.chahuyun.economy.strategy.impl;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.entity.rodeo.RodeoRecord;
import cn.chahuyun.economy.manager.RodeoRecordManager;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 决斗
 */
public class RodeoDuelStrategy extends RodeoAbstractStrategy {
    @Override
    public void startGame(Rodeo rodeo) {
//        分配决斗[双方][规定时间段]内的比赛[局数]（按局数给权限）
        Group group = getBotGroup(rodeo.getGroupId());
        if(group == null){
            return;
        }
//        【
//        东风吹，战鼓擂，决斗场上怕过谁！
//        新的🏟[比赛场次名]已确定于[14:00-17:00]开战！
//        [@A ]与[@B ]正式展开决斗的巅峰对决！⚔[N]局比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
//        】

        String messageFormat1 = """
        东风吹，战鼓擂，决斗场上怕过谁！
        新的🏟[%s]已确定于[%s-%s]开战！
        """;
        String messageFormat2 = """
         
         正式展开决斗的巅峰对决！⚔[%s]局比赛，谁将笑傲鱼塘🤺，谁又将菜然神伤🥬？
        """;

        String[] players = rodeo.getPlayers().split(Constant.MM_SPILT);
        Long player1 = Long.parseLong(players[0]);
        Long player2 = Long.parseLong(players[1]);

        String message1 = String.format(messageFormat1, rodeo.getVenue(), rodeo.getStartTime(),
                rodeo.getEndTime());

        String message2 = String.format(messageFormat2, rodeo.getRound());

        Message m = new PlainText(message1);
        m = m.plus(new At(player1).getDisplay(group));
        m = m.plus(" VS ");
        m = m.plus(new At(player2).getDisplay(group));
        m.plus(message2);
        group.sendMessage(m);

        // todo 开始决斗权限

    }

    @Override
    public void record(Rodeo rodeo) {
        // 用户同一时间段 只能参加一场比赛
        // 每个时间段只有一场比赛
        // 存入输、赢家

    }

    @Override
    public void endGame(Rodeo rodeo) {

//            2.该场比赛结束后，统计双方的得分和总被禁言时长
//【
//        [比赛场次名]结束，恭喜胜者@B以[3:1]把对手@A鸡哔！🔫
//    @B共被禁言[秒]
//    @A共被禁言[秒]
//    菜！就！多！练！
//            】
        Group group = getBotGroup(rodeo.getGroupId());
        if(group == null){
            return;
        }

        Long rodeoId = rodeo.getId();
        String[] players = rodeo.getPlayers().split(Constant.MM_SPILT);
        Long player1 = Long.parseLong(players[0]);
        Long player2 = Long.parseLong(players[1]);

        List<RodeoRecord> records = RodeoRecordManager.getRecordsByRodeoId(rodeoId);
        if(CollectionUtils.isEmpty(records)){
            String messageFormat = """
                %s,%s,%s未进行任何比赛
            """;
            String message = String.format(messageFormat, rodeo.getVenue(),
                    new At(player1).getDisplay(group), new At(player2).getDisplay(group));
            group.sendMessage(new PlainText(message));
            return ;
        }

        List<RodeoRecord> winnerPlayers = new ArrayList<RodeoRecord>();
        List<RodeoRecord> losePlayers = new ArrayList<RodeoRecord>();
        // 局数
        Map<Integer, List<RodeoRecord>> recordsByTurns = records.stream()
                .collect(Collectors.groupingBy(RodeoRecord::getTurns));
        recordsByTurns.forEach((turns, recordList) -> {
            Optional<RodeoRecord> winnerOptional = recordList.stream().filter(r-> Objects.isNull(r.getForbiddenSpeech()) || r.getForbiddenSpeech().equals(0)).findAny();
            winnerOptional.ifPresent(winnerPlayers::add);

            Optional<RodeoRecord> loseOptional = recordList.stream().filter(r->  r.getForbiddenSpeech() > 0).findAny();
            loseOptional.ifPresent(losePlayers::add);
        });


        // 决斗存入赢+输的场次
        String messageFormat = """
                    %s结束，恭喜胜者%s以[%s:%s]把对手%s鸡哔！🔫
                    %s共被禁言%s
                    %s共被禁言%s
                    菜！就！多！练！
                """;
        Long winner = Long.parseLong(winnerPlayers.get(0).getPlayer());
        Long lose = Long.parseLong(losePlayers.get(0).getPlayer());
        Long winnerTimeSum = winnerPlayers.stream().mapToLong(obj -> Optional.ofNullable(obj.getForbiddenSpeech()).orElse(0)).sum();
        Long loseTimeSum = winnerPlayers.stream().mapToLong(obj -> Optional.ofNullable(obj.getForbiddenSpeech()).orElse(0)).sum();
        String message = String.format(messageFormat, rodeo.getVenue(), new At(winner).getDisplay(group),
                winnerPlayers.size(), losePlayers.size(), new At(lose).getDisplay(group), new At(winner).getDisplay(group),
                winnerTimeSum, new At(lose).getDisplay(group), loseTimeSum);
        group.sendMessage(new PlainText(message));

        // todo 关闭决斗权限
    }

}
