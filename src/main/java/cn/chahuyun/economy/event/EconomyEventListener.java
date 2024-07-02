package cn.chahuyun.economy.event;

import cn.chahuyun.config.EconomyEventConfig;
import cn.chahuyun.config.RegexConst;
import cn.chahuyun.economy.constant.DailyPropCode;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.manager.GamesManager;
import cn.chahuyun.economy.manager.UserManager;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.*;
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
import net.mamoe.mirai.message.data.MessageSource;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBloomFilter;

import java.util.List;
import java.util.Objects;
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
        // 经济命令设置的群
        if (!EconomyEventConfig.INSTANCE.getEconomyCheckGroup().contains(event.getGroup().getId())
                || event.getBot().getId() == event.getSender().getId()) {
            return ListeningStatus.LISTENING;
        }

        if(CacheUtils.checkTimeCacheKey(event.getGroup().getId(),event.getSender().getId())
                || CacheUtils.checkSchDingerFishKey(event.getGroup().getId(),event.getSender().getId())
        ){ try {
                MessageSource.recall(event.getSource());
            }catch (Exception e){
                e.printStackTrace();
            }
            event.intercept();
        }

        // 设置30分钟发言缓存
        SisterDogCommand.getInstance().fireClickEvent(event.getGroup().getId(), event.getSender().getId());

        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        if(("钓鱼".equals(code) || "抛竿".equals(code)) && event.getGroup().getId() == 758085692){
            // 周五22.00 周日 23.00
            if(DateUtil.checkDate()){
                subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "休渔期"));
                event.intercept();
                return ListeningStatus.LISTENING;
            }
        }

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
            GamesManager.userPay.put(user.getId(), regexConst.getConst());
            // subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "消费%s WDIT币币",regexConst.getConst()));
        } else {
            event.intercept();
            Log.error("游戏管理:失败!");
        }
    }
}
