package cn.chahuyun.economy.command;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.Constant;
import cn.chahuyun.economy.dto.AutomaticFish;
import cn.chahuyun.economy.entity.fish.AutomaticFishUser;
import cn.chahuyun.economy.manager.GamesManager;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class AutomaticFishTask implements Task {
    String id;

    LocalDateTime endTime;

    Long groupId;

    Long userId;

    public AutomaticFishTask(String id, LocalDateTime endTime, Long groupId, Long userId) {
        this.id = id;
        this.endTime = endTime;
        this.groupId = groupId;
        this.userId = userId;
    }

    public static String getAutomaticFishTaskId(Long groupId,Long userId){
        return "machine:" + groupId + "-" + userId;
    }

    @Override
    public void execute() {
        LocalDateTime excuteTime = LocalDateTime.now();
        Log.info("[自动钓鱼机-开始执行]-任务id" + id + "执行时间:" + excuteTime.format(Constant.FORMATTER));

        Group group = HuYanEconomy.INSTANCE.bot.getGroup(groupId);
        if (Objects.isNull(group)) {
            Log.info("[自动钓鱼机-发生异常]-获取群组为空：" + groupId);
            return;
        }
        User user = group.get(userId);
        if (Objects.isNull(user)) {
            Log.info("[自动钓鱼机-发生异常]-获取用户为空：" + userId);
            return;
        }
        List<AutomaticFishUser> automaticFishUserList = AutomaticFishUser.getAutomaticFishUser(groupId, userId);
        if (CollectionUtils.isEmpty(automaticFishUserList)) {
            Log.info("[自动钓鱼机-发生异常]-查询不到用户钓鱼机数据：groupId：" + groupId + ",userId:" + userId);
            return;
        }
        AutomaticFishUser automaticFishUser = automaticFishUserList.get(0);
        String fishAutoStr = Optional.of(automaticFishUser.getAutomaticFishStr()).orElse("");
        List<AutomaticFish> automaticFishList = JSONArray.parseArray(fishAutoStr, AutomaticFish.class);
        Log.info("[自动钓鱼机-执行中] 获取当前的鱼数量：" + automaticFishList.size());
        // 获取钓鱼的对象
        AutomaticFish automaticFish = GamesManager.getAutomaticFish(user, group);
        // 将鱼添加到list
        automaticFishList.add(automaticFish);
        // 设置str
        automaticFishUser.setAutomaticFishStr(JSONUtil.toJsonStr(automaticFishList));
        // 更新数据库
        boolean ok = automaticFishUser.saveOrUpdate();
        Log.info("[自动钓鱼机-执行中] 更新钓鱼机信息：" + ok);

        Log.info("[自动钓鱼机-判断是否完成] 结束时间:" + endTime.format(Constant.FORMATTER) +
                ",判断是否完成：" + !excuteTime.isBefore(endTime));
        if (!excuteTime.isBefore(endTime)) {
            // 输出鱼信息
            Message m = new At(user.getId()).plus("\r\n");
            m = m.plus("[岛岛全自动钓鱼机使用结束]").plus("\r\n");
            m = m.plus("=============\r\n");
            StringBuilder message = new StringBuilder();
            automaticFishList.forEach(f -> {
                message.append(f.getMessage() + "\r\n");
                if(StrUtil.isNotBlank(f.getOtherMessage())){
                    message.append(f.getOtherMessage() + "\r\n");
                }
                message.append("-----------\r\n");
            });
            m = m.plus(message);
            group.sendMessage(m);
            // 删除缓存
            CacheUtils.removeAutomaticFishBuff(group.getId(), user.getId());
            // 删除存储的
            automaticFishUser.remove();
            // 删除定时任务
            CronUtil.remove(id);
        }
        Log.info("[自动钓鱼机-执行完成]");
    }
}
