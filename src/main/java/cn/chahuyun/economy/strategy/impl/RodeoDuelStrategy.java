package cn.chahuyun.economy.strategy.impl;

import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.strategy.impl.RodeoAbstractStrategy;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;

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

    }

    @Override
    public void endGame(Rodeo rodeo) {


        // todo 关闭决斗权限
    }
}
