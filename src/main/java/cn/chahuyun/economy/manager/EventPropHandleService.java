package cn.chahuyun.economy.manager;
import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.props.PropsBase;
import cn.chahuyun.economy.entity.props.factory.PropsCardFactory;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.utils.Log;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import org.apache.commons.compress.utils.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventPropHandleService {

    private static final String LOG_PREFIX = "[道具服务] ";

    public static void addProp(Long groupId, List<Long> userIds, String propCode) {
        // 1. 参数有效性校验
        if (groupId == null || groupId <= 0) {
            Log.error(LOG_PREFIX + "群ID无效: " + groupId);
            return;
        }

        if (propCode == null || propCode.isEmpty()) {
            Log.error(LOG_PREFIX + "道具编码为空");
            return;
        }

        List<Long> userIdList = Optional.ofNullable(userIds).orElse(Lists.newArrayList());
        if (userIdList.isEmpty()) {
            Log.warning(LOG_PREFIX + "用户列表为空，跳过处理");
            return;
        }

        // 2. 获取机器人实例
        Bot botCurrent = HuYanEconomy.INSTANCE.getBotInstance();
        if (botCurrent == null) {
            Log.error(LOG_PREFIX + "机器人实例未初始化");
            return;
        }

        // 3. 获取群组并校验
        Group group = botCurrent.getGroup(groupId);
        if (group == null) {
            Log.error(LOG_PREFIX + "群组不存在: " + groupId);
            return;
        }

        // 4. 验证道具有效性
        PropsBase prop = PropsCardFactory.INSTANCE.getPropsBase(propCode);
        if (prop == null) {
            Log.error(LOG_PREFIX + "道具不存在: " + propCode);
            return;
        }

        // 5. 处理用户列表（记录失败情况）
        List<Long> failedUsers = new ArrayList<>();
        int successCount = 0;

        for (Long userId : userIdList) {
            try {
                Member member = group.get(userId);
                if (member == null) {
                    Log.warning(LOG_PREFIX + "成员不存在: " + userId);
                    failedUsers.add(userId);
                    continue;
                }

                // 6. 获取用户信息并添加道具
                UserInfo userInfo = UserManager.getUserInfo(member);
                PluginManager.getPropsManager().addProp(userInfo, prop);
                successCount++;

            } catch (Exception e) {
                Log.error(LOG_PREFIX + "用户道具添加异常: " + userId, e);
                failedUsers.add(userId);
            }
        }

        // 7. 结果日志记录
        if (!failedUsers.isEmpty()) {
            Log.warning(LOG_PREFIX + String.format(
                    "道具发放完成: 成功 %d 人, 失败 %d 人 [失败ID: %s]",
                    successCount, failedUsers.size(), failedUsers
            ));
        } else {
            Log.info(LOG_PREFIX + "道具发放成功: 群组 " + groupId + " | 道具 " + propCode);
        }
    }
}
