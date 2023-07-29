package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.config.RegexConst;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.permission.*;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EconomyEventListener extends SimpleListenerHost {

    String EMOJI_REGEX = "(\u2764\ufe0f\u200d\ud83e\ude79|\ud83d\ude2e\u200d\ud83d\udca8|\ud83d\ude36\u200d\ud83c\udf2b\ufe0f|[\u2601-\u2b50]\ufe0f?|[\ud83c\udc04-\ud83c\udff9]\ufe0f?|[\ud83d\udc0c-\ud83d\udefc]\ufe0f?|[\ud83e\udd0d-\ud83e\udee7]){2,}";

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public ListeningStatus onGroupMsg(GroupMessageEvent event) {
        if (!EconomyEventConfig.INSTANCE.getEconomyCheckGroup().contains(event.getGroup().getId())
                || event.getBot().getId() == event.getSender().getId()) {
            return ListeningStatus.LISTENING;
        }

        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        // 权限判断的命令列表
        List<RegexConst> regexOrderConstList =  EconomyEventConfig.INSTANCE.getRegexOrderCheck();
        for(int i = 0; i < regexOrderConstList.size(); i++){
            RegexConst regexOrderConst = regexOrderConstList.get(i);
            // 获取正则表达式
            boolean isCheck = getCheck(regexOrderConst, code);
            if (isCheck) {
                CommandSender.from(event).getPermitteeId();
                if (PermissionService.hasPermission(CommandSender.from(event).getPermitteeId(), PermissionId.parseFromString(regexOrderConst.getPremission()))) {
                    // 校验用户金钱
                    checkUserMoney(event, subject, regexOrderConst);
                } else {
                    Log.info("插件无权限");
                }
            }
        }
        List<RegexConst> regexList =  EconomyEventConfig.INSTANCE.getRegexCheck();
        for(int i = 0; i < regexList.size();i++){
            RegexConst regexOrderConst = regexList.get(i);
            boolean isCheck = getCheck(regexOrderConst, code);
            if (isCheck) {
                checkUserMoney(event, subject, regexOrderConst);
            }
        }
//        if (event.getMessage().contains("开车")) {
//            event.intercept();
//            return ListeningStatus.LISTENING;
//        }
        return ListeningStatus.LISTENING;
    }

    private boolean getCheck(RegexConst regexOrderConst, String code) {
        String[] arr = regexOrderConst.getOrder().split(",");
        boolean isCheck = false;
        for (int j = 0; j < arr.length; j++) {
            if (isCheck) {
                break;
            }
            if(arr[j].equals("emoji")){
                Pattern pattern = Pattern.compile(EMOJI_REGEX);
                Matcher matcher = pattern.matcher(code);
                isCheck =  matcher.find();
            }else {
                Pattern pattern = Pattern.compile(arr[j]);
                Matcher matcher = pattern.matcher(code);
                isCheck =  matcher.find();
            }

        }
        return isCheck;
    }

    private void checkUserMoney(GroupMessageEvent event, Contact subject, RegexConst regexConst) {
        User user = event.getSender();
        double moneyByUser = EconomyUtil.getMoneyByUser(user);
        if (moneyByUser - regexConst.getConst() < 0) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "需要%s WDIT币币,没有足够的WDIT币币",regexConst.getConst()));
            event.intercept();
            return;
        }

        if (EconomyUtil.minusMoneyToUser(user, regexConst.getConst())) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "消费%s WDIT币币",regexConst.getConst()));
        } else {
            event.intercept();
            Log.error("游戏管理:失败!");
        }
    }
}