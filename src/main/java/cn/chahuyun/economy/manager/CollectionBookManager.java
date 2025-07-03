package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.utils.Log;
import cn.chahuyun.economy.utils.MessageUtil;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static cn.chahuyun.economy.constant.FishSignConstant.*;

public class CollectionBookManager {
    // 图片定位备注
    // 根据图片的 左边界为x  上边界为 y
    private static final String[][] IMAGE_TAG_ARR = {
            {"", "", "", "", "", FISH_125, "", ""},
            {"", "", FISH_ROD_LEVEL, FISH_20, "", "", "", FISH_39},
            {FISH_133, "", "", "", "", "", "", ""},
            {"", "", "", "", FISH_49, "", FISH_95, ""},
            {FISH_62, "", "", "", "", FISH_PAO_PAO, FISH_15, ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", FISH_17, FISH_16, "", "", "", ""},
            {"", "", "", "", "", "", "", ""}
    };

    private static final Map<String, Integer[]> IMAGE_TAG_MAP = new HashMap<>();
    static {
        IMAGE_TAG_MAP.put(FISH_125, new Integer[]{457, 23});
        IMAGE_TAG_MAP.put(FISH_ROD_LEVEL, new Integer[]{198, 110});

        IMAGE_TAG_MAP.put(FISH_20, new Integer[]{282, 122});
        IMAGE_TAG_MAP.put(FISH_39, new Integer[]{629, 131});
        IMAGE_TAG_MAP.put(FISH_133, new Integer[]{23, 210});
        IMAGE_TAG_MAP.put(FISH_49, new Integer[]{373, 282});
        IMAGE_TAG_MAP.put(FISH_95, new Integer[]{545, 288});
        IMAGE_TAG_MAP.put(FISH_62, new Integer[]{22, 375});
        IMAGE_TAG_MAP.put(FISH_15, new Integer[]{545, 373});

        IMAGE_TAG_MAP.put(FISH_PAO_PAO, new Integer[]{457, 371});
        IMAGE_TAG_MAP.put(FISH_17, new Integer[]{201, 548});
        IMAGE_TAG_MAP.put(FISH_16, new Integer[]{281, 548});
    }


    public static void drawImage(MessageEvent event){
        User user = event.getSender();
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();
        UserInfo userInfo = UserManager.getUserInfo(user);

        MessageChainBuilder messages = MessageUtil.quoteReply(message);

        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }
        if (userInfo == null) {
            subject.sendMessage("获取用户信息出错!");
            return;
        }

        BufferedImage userInfoImageBase = getCollectionBook(userInfo, group);
        if (userInfoImageBase == null) {
            return;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(userInfoImageBase, "png", stream);
        } catch (IOException e) {
            Log.error("签到管理:签到图片发送错误!", e);
            subject.sendMessage(messages.build());
            return;
        }
        Contact.sendImage(subject, new ByteArrayInputStream(stream.toByteArray()));
    }

    private static BufferedImage getCollectionBook(UserInfo userInfo, Group group) {
        try {
            InputStream asStream = HuYanEconomy.BASE_INFO_STREAM;
            if (asStream == null) {
                Log.error("用户管理:收集册图片底图获取错误!");
                return null;
            }
            BufferedImage image = ImageIO.read(asStream);
            asStream.reset();

            Graphics2D pen = image.createGraphics();
            pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 遍历 8×8 网格 → 用真实格子尺寸计算坐标
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    String key = IMAGE_TAG_ARR[row][col];
                    if (StringUtils.isNotBlank(key)) {
                        key = key.toUpperCase(Locale.ROOT);
                        if(getDefaultPropCode().contains(key)){
                            drawIcon(key, pen);
                        }else if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), key) > 0) {
                            drawIcon(key, pen);
                        }
                    }
                }
            }
            // 绘制用户信息（如果有需要，可补充日志）
            drawUserInfo(pen, image, userInfo, group);

            pen.dispose();
            return image;
        } catch (IOException exception) {
            Log.error("用户管理:个人信息基础信息绘图错误!", exception);
            return null;
        }
    }

    private static void drawIcon(String key, Graphics2D pen) throws IOException {
        InputStream fishSignStream = HuYanEconomy.SIGN_STREAM_MAP.get(key);
        if (fishSignStream != null) {
            BufferedImage fishImage = ImageIO.read(fishSignStream);
            fishSignStream.reset();
            // 5. 绘制图片
            Integer[] pos = IMAGE_TAG_MAP.get(key);
            pen.drawImage(fishImage,
                    pos[0],
                    pos[1],
                    fishImage.getWidth(), fishImage.getHeight(),
                    null
            );
        } else {
            Log.info(String.format("图标资源缺失: %s", key)); // 资源找不到时提示
        }
    }

    // 抽离用户信息绘制逻辑（可选，让主逻辑清晰）
    private static void drawUserInfo(Graphics2D pen, BufferedImage image, UserInfo userInfo, Group group) {
        User user = userInfo.getUser();
        String groupUserName = "";
        if (Objects.nonNull(group)) {
            NormalMember normalMember = group.get(userInfo.getUser().getId());
            if (Objects.nonNull(normalMember)) {
                groupUserName = normalMember.getNameCard();
                if (groupUserName.length() > 6) {
                    groupUserName = groupUserName.substring(0, 6) + "...";
                }
            }
        }
        String userInfoName = userInfo.getName();
        if (StrUtil.isNotBlank(groupUserName)) {
            userInfoName = groupUserName;
        } else if (userInfoName.length() > 6) {
            userInfoName = userInfoName.substring(0, 6) + "...";
        }

        // 设置权限颜色
        if (user instanceof Member) {
            MemberPermission permission = ((Member) user).getPermission();
            if (permission.getLevel() == MemberPermission.OWNER.getLevel()) {
                pen.setColor(Color.YELLOW);
            } else if (permission.getLevel() == MemberPermission.ADMINISTRATOR.getLevel()) {
                pen.setColor(Color.GREEN);
            } else {
                pen.setColor(Color.BLACK);
            }
        }


        // 绘制姓名
        int fontSize = 22;
        pen.setFont(new Font("黑体", Font.BOLD, fontSize));
        FontMetrics fm = pen.getFontMetrics();
        int textWidth = fm.stringWidth(userInfoName);
        int margin = 20;
        int textX = image.getWidth() - textWidth - margin;
        int textY = image.getHeight() - margin;
        pen.drawString(userInfoName, textX, textY);
    }



}
