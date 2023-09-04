package cn.chahuyun.economy.command;

import cn.chahuyun.economy.HuYanEconomy;
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
        Group group = HuYanEconomy.INSTANCE.bot.getGroup(groupId);
        if (Objects.isNull(group)) {
            return;
        }
        User user = group.get(userId);
        if (Objects.isNull(user)) {
            return;
        }
        List<AutomaticFishUser> automaticFishUserList = AutomaticFishUser.getAutomaticFishUser(groupId, userId);
        if (CollectionUtils.isEmpty(automaticFishUserList)) {
            return;
        }
        AutomaticFishUser automaticFishUser = automaticFishUserList.get(0);
        String fishAutoStr = Optional.of(automaticFishUser.getAutomaticFishStr()).orElse("");

        List<AutomaticFish> automaticFishList = JSONArray.parseArray(fishAutoStr ,AutomaticFish.class);
        // 获取钓鱼的对象
        AutomaticFish automaticFish = GamesManager.getAutomaticFish(user, group);
        // 将鱼添加到list
        automaticFishList.add(automaticFish);
        // 设置str
        automaticFishUser.setAutomaticFishStr(JSONUtil.toJsonStr(automaticFishList));
        // 更新数据库
        automaticFishUser.saveOrUpdate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (excuteTime.format(formatter).equals(endTime.format(formatter)) || excuteTime.isAfter(endTime) ) {
            // 输出鱼信息
            Message m = new At(user.getId()).plus("\r\n");
            m = m.plus("[岛岛全自动钓鱼机使用结束]").plus("\r\n");
            m = m.plus("-----🐟-----\r\n");
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

    }
}
