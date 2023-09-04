package cn.chahuyun.economy.command;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.dto.AutomaticFish;
import cn.chahuyun.economy.entity.fish.AutomaticFishUser;
import cn.chahuyun.economy.manager.GamesManager;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

import java.time.LocalDateTime;
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
        Group group = HuYanEconomy.INSTANCE.bot.getGroup(groupId);
        if (Objects.isNull(group)) {
            return;
        }
        User user = group.get(userId);
        if (Objects.isNull(user)) {
            return;
        }
        AutomaticFishUser automaticFishUser = AutomaticFishUser.getAutomaticFishUser(groupId, userId);
        if (Objects.isNull(automaticFishUser)) {
            return;
        }
        String fishAutoStr = Optional.of(automaticFishUser.getAutomaticFishStr()).orElse("");
        CopyOnWriteArrayList<AutomaticFish> automaticFishList = Optional.ofNullable(JSONUtil.toBean(fishAutoStr,
                        CopyOnWriteArrayList.class))
                .orElse(new CopyOnWriteArrayList());
        // 获取钓鱼的对象
        AutomaticFish automaticFish = GamesManager.getAutomaticFish(user, group);
        // 将鱼添加到list
        automaticFishList.add(automaticFish);
        // 设置str
        automaticFishUser.setAutomaticFishStr(JSONUtil.toJsonStr(automaticFishList));
        // 更新数据库
        automaticFishUser.saveOrUpdate();

        if (LocalDateTime.now().equals(endTime) || LocalDateTime.now().isAfter(endTime)) {
            // 输出鱼信息
            Message m = new At(user.getId()).plus("\r\n");
            m = m.plus("[岛岛全自动钓鱼机使用结束]").plus("\r\n");
            StringBuilder message = new StringBuilder();
            automaticFishList.forEach(f -> {
                message.append(f.getMessage() + "\r\n");
            });
            m = m.plus(message);
            group.sendMessage(m);
            // 删除缓存
            CacheUtils.removeAutomaticFishBuff(group.getId(), user.getId());
            // 删除存储的
            automaticFishUser.remove();
        }
        CronUtil.remove(id);
    }
}
