package cn.chahuyun.economy.event;


import cn.chahuyun.economy.dto.RodeoRecordGameInfoDto;
import cn.chahuyun.economy.entity.rodeo.Rodeo;
import cn.chahuyun.economy.entity.rodeo.RodeoRecord;
import cn.chahuyun.economy.manager.RodeoManager;

import cn.chahuyun.economy.strategy.RodeoFactory;
import cn.chahuyun.economy.strategy.RodeoStrategy;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessagePostSendEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.chahuyun.economy.strategy.RodeoFactory.DUEL;

public class BotPostSendEventListener extends SimpleListenerHost {
    @EventHandler()
    public void onMessage(@NotNull MessagePostSendEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        // [轮盘]
        // [决斗]
        
        if(!(code.startsWith("[轮盘]") || code.startsWith("[决斗]"))){
            return;
        }
        List<Long> atUser = new ArrayList<>();
        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                At at = (At) singleMessage;
                atUser.add(at.getTarget());
            }
        }
        Rodeo redeo = RodeoManager.getCurrent(event.getTarget().getId(), atUser);
        if(Objects.isNull(redeo)){
            // 如果用户没有正在进行的比赛
            return;
        }
        // 如果有 则记录

        // [mirai:at:294253294] 😙了一口[mirai:at:952746839] 的【身体】，让对方被冲昏了1分40秒头脑。恭喜[mirai:at:294253294] 获得一分！
        RodeoRecordGameInfoDto dto = new RodeoRecordGameInfoDto();
        if (DUEL.equals(redeo.getPlayingMethod())) {
            int totalDuration = 0;
            String winner = null;
            String loser = null;

            // 提取赢家和输家的信息
            if (code.contains("恭喜")) {
                String[] parts = code.split("恭喜");
                // 赢家
                winner = parts[0].substring(parts[0].indexOf('[') + 1, parts[0].indexOf(']'));
                // 输家
                loser = parts[1].substring(parts[1].indexOf('[') + 1, parts[1].indexOf(']'));
            }

            // 提取时长
            String durationStr = code.replaceAll(".*?让对方被冲昏了", "").split("头脑")[0];
            String[] timeParts = durationStr.split("分|秒");
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;

            totalDuration += minutes * 60 + seconds;

            // 输出结果（可选）
            System.out.println("赢家: " + winner);
            System.out.println("输家: " + loser);
            System.out.println("总时长: " + totalDuration + "秒");

            // 设置 DTO
            dto.setWinner(winner);
            dto.setLoser(loser);
            dto.setForbiddenSpeech(totalDuration); // 根据需要设置
            dto.setRodeoDesc(code);
        }else{
            // 比赛记录处理
            // "[mirai:at:294253294] 开了一枪🔫，枪响了，被冲昏了4分9秒头脑，并爽快地输掉了这局比赛。"
            // <target-win> 😙了一口<target-lose> 的【<position>】，让对方被冲昏了<mute-f>头脑。恭喜<target-win> 获得一分！
            String loser = null;
            int totalDuration = 0;
            // 提取输家信息
            if (code.contains("被冲昏了")) {
                loser =code.substring(code.indexOf('[') + 1, code.indexOf(']'));
            }

            // 提取时长
            String durationStr = code.replaceAll(".*?被冲昏了", "").split("头脑")[0];
            String[] timeParts = durationStr.split("分|秒");
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;

            totalDuration += minutes * 60 + seconds;

            // 设置 DTO
            dto.setLoser(loser);
            dto.setForbiddenSpeech(totalDuration); // 设定总时长（秒）
            dto.setRodeoDesc(code);

            // 输出结果（可选）
            System.out.println("输家: " + loser);
            System.out.println("总时长: " + totalDuration + "秒");
        }

        // 轮盘
        // [mirai:at:294253294] 开了一枪🔫，枪响了，被冲昏了4分9秒头脑，并爽快地输掉了这局比赛。
        RodeoStrategy strategy =  RodeoFactory.createRodeoDuelStrategy(redeo.getPlayingMethod());
        strategy.record(redeo, dto);
        System.out.println(code);
    }

//    private void messagePostSendEventListener() {
////        logger.info("MessagePostSendEventListener Running");
////        GlobalEventChannel.INSTANCE.subscribeAlways(MessagePostSendEvent.class, event -> {
////            logger.info("MessagePostSendEvent...");
////            // 这里新构造了一个带source的chain，存储到数据库中
////            MessageChain chain = Objects.requireNonNull(event.getReceipt()).getSource().plus(event.getMessage());
////            if (!chainService.insertMessageChain(new MessageChainData(chain)))
////                logger.warn("存储失败: " + chain.contentToString());
////        });
////    }
}
