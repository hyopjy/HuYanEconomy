package cn.chahuyun.economy.command;

import cn.chahuyun.economy.entity.UserBackpack;
import cn.chahuyun.economy.entity.props.PropsFishCard;
import cn.chahuyun.economy.factory.AbstractPropUsage;
import cn.chahuyun.economy.plugin.PluginManager;
import cn.chahuyun.economy.plugin.PropsType;
import cn.chahuyun.economy.utils.MessageUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OmnipotentProps extends AbstractPropUsage {


    private String changePropCode;

    @Override
    public boolean checkOrder() {
        String no = PropsType.getNo(propsCard.getCode());
        String code = event.getMessage().serializeToMiraiCode().trim();  // 去除首尾空格

        // 核心优化：构建动态正则 + 分组提取目标道具 [7]
        String nameRegex = Pattern.quote(propsCard.getName());  // 转义特殊字符[1]
        String noRegex = Pattern.quote(no);

        // 正则解释:
        // ^使用\\s+ - 以"使用"开头接至少一个空格
        // (nameRegex|noRegex) - 匹配道具名或编码
        // \\s+(.+) - 匹配至少一个空格后捕获目标道具信息
        String regex = "^使用\\s+(" + nameRegex + "|" + noRegex + ")\\s+(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);

        // 优化点：单次正则匹配完成校验+提取
        if (!matcher.find()) {
            // 精准提示包含空格示例 [5][7]
            String errorMsg = "格式错误！正确示例:\n"
                    + "使用 " + propsCard.getName() + " 狗的姐姐\n"
                    + "或: 使用 " + no + " 2";
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), errorMsg));
            return false;
        }

        // 直接通过分组获取目标道具（避免arr.split）
        String changeNo = matcher.group(2).trim();  // 第2分组是目标道具
        String propCode = PropsType.getCode(changeNo);

        // ---- 原有业务逻辑（保持不变）----
        PropsFishCard propsInfo = (PropsFishCard) PropsType.getPropsInfo(propCode);
        if (Objects.isNull(propsInfo)) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(), "需要获得的道具不存在！"));
            return false;
        }
        if (!propsInfo.getBuy()) {
            subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                    "😣 [" + propsInfo.getName() + "] 是不可购买道具，不可兑换"));
            return false;
        }
        changePropCode = propCode;
        return true;
    }

    @Override
    public void excute() {
        List<UserBackpack> userBackpack = userInfo.getBackpacks();
        // 消耗的道具 propsCard
       List<UserBackpack> userBackpackConsume = userBackpack.stream().
                filter(user -> user.getPropsCode().equals(propsCard.getCode()))
                .collect(Collectors.toList());
       if(CollectionUtils.isEmpty(userBackpackConsume)){
           subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                   "😣你的背包似乎没有[ " + propsCard.getName() + " ]道具"));
           return;
       }
        // 获得的道具 changePropCode
        PropsFishCard propsInfo = (PropsFishCard) PropsType.getPropsInfo(changePropCode);
        PluginManager.getPropsManager().addProp(userInfo, propsInfo);
        subject.sendMessage(MessageUtil.formatMessageChain(event.getMessage(),
                "恭喜你获得 [ " + propsInfo.getName() + " ] 道具"));

    }
}
