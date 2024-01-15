package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.constant.FishSignConstant;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.entity.badge.BadgeInfo;
import cn.chahuyun.economy.redis.RedisUtils;
import cn.chahuyun.economy.utils.CacheUtils;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.*;

public class FbUserManager  {

    private FbUserManager() {
        super();
    }

    public static void getUserInfoImageFb(MessageEvent event) {
        Contact subject = event.getSubject();
        User sender = event.getSender();

        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }


        UserInfo userInfo = UserManager.getUserInfo(sender);
        double moneyByUser = EconomyUtil.getMoneyByUser(sender);

        MessageChainBuilder singleMessages = new MessageChainBuilder();

        if (userInfo == null) {
            subject.sendMessage("获取用户信息出错!");
            return;
        }

        singleMessages.append(userInfo.getString()).append(String.format("WDIT币币:%s", moneyByUser));

        BufferedImage userInfoImageBase = getUserInfoImageBaseFb(userInfo, group);
        if (userInfoImageBase == null) {
            subject.sendMessage(singleMessages.build());
            return;
        }

        Graphics2D graphics = userInfoImageBase.createGraphics();
        //图片与文字的抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.black);
        graphics.setFont(new Font("黑体", Font.PLAIN, 20));
        graphics.dispose();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(userInfoImageBase, "png", stream);
        } catch (IOException e) {
            Log.error("签到管理:签到图片发送错误!", e);
            subject.sendMessage(singleMessages.build());
            return;
        }
        Contact.sendImage(subject, new ByteArrayInputStream(stream.toByteArray()));
    }


    /**
     * 绘制个人信息基础信息<p>
     * 包含:<p>
     * 头像、名称、id、签到时间、连签次数<p>
     * 下次签到时间、称号<p>
     * 总金币、今日签到获得<p>
     *
     * @param userInfo 用户信息
     * @return java.awt.image.BufferedImage
     * @author Moyuyanli
     * @date 2022/12/5 16:11
     */
    public static BufferedImage getUserInfoImageBaseFb(UserInfo userInfo,Group group) {

        User user = userInfo.getUser();
        String groupUserName = "";
        if (Objects.nonNull(group)) {
            NormalMember normalMember = group.get(userInfo.getUser().getId());
            if (Objects.nonNull(normalMember)) {
                groupUserName = normalMember.getNameCard();
                if (groupUserName.length() > 11) {
                    groupUserName = groupUserName.substring(0, 11) + "...";
                }
            }
        }

        try {
            int index = RandomUtil.randomInt(6) +1;
            InputStream asStream = HuYanEconomy.INPUT_STREAM_MAP.get(index);
            //验证
            if (asStream == null) {
                Log.error("用户管理:个人信息图片底图获取错误!");
                return null;
            }
            //转图片处理
            BufferedImage image = ImageIO.read(asStream);
            asStream.reset();
            //创建画笔
            Graphics2D pen = image.createGraphics();
            //图片与文字的抗锯齿
            pen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //获取头像链接
            // String avatarUrl = user.getAvatarUrl(AvatarSpec.LARGE);
            InputStream userImageStream = CacheUtils.getAvatarUrlInputStream(user.getId(), user.getAvatarUrl(AvatarSpec.LARGE));
            if (Objects.nonNull(userImageStream)) {
                BufferedImage avatar = ImageIO.read(userImageStream);
                userImageStream.reset();
                BufferedImage avatarRounder = UserManager.makeRoundedCorner(avatar, 50);
                //写入头像
                pen.drawImage(avatarRounder, 195, 285, null);
            }

            String userInfoName = userInfo.getName();
            int fontSize;
            //如果是群 根据管理员信息改变颜色
            if (user instanceof Member) {
                MemberPermission permission = ((Member) user).getPermission();
                Log.info("uer:"+user.getId() +"userPermission:"+permission.getLevel());
                if (permission.getLevel() == MemberPermission.OWNER.getLevel()) {
                    pen.setColor(Color.YELLOW);
                } else if (permission.getLevel() == MemberPermission.ADMINISTRATOR.getLevel()) {
                    pen.setColor(Color.GREEN);
                } else {
                    pen.setColor(Color.WHITE);
                }
            }
            /*
             * WHITE(白色)、LIGHT_GRAY（浅灰色）、GRAY（灰色）、DARK_GRAY（深灰色）、
             * BLACK（黑色）、RED（红色）、PINK（粉红色）、ORANGE（橘黄色）、YELLOW（黄色）、
             * GREEN（绿色）、MAGENTA（紫红色）、CYAN（青色）、BLUE（蓝色）
             * 如果上面颜色都不满足你，或者你还想设置下字体透明度，你可以改为如下格式：
             * pen.setColor(new Color(179, 250, 233, 100));
             * Font.PLAIN（正常），Font.BOLD（粗体），Font.ITALIC（斜体）
             */
            //根据名字长度改变大小
            if(StrUtil.isNotBlank(groupUserName)){
                userInfoName = groupUserName;
            }
            if (userInfoName.length() > 6) {
                fontSize = 20;
                pen.setFont(new Font("黑体", Font.BOLD, fontSize));
                String arr1 = userInfoName.substring(0,6);
                pen.drawString(arr1, 174, 400);

                String arr2 = userInfoName.substring(6);
                pen.drawString(arr2, 174, 422);
            } else {
                fontSize = 30;
                pen.setFont(new Font("黑体", Font.BOLD, fontSize));
                pen.drawString(userInfoName, 174, 408);
            }
            // 设置画笔字体样式为黑体，粗体
            pen.setColor(Color.white);
            fontSize = 20;
            pen.setFont(new Font("黑体", Font.PLAIN, fontSize));
            //id
            String rgb = StringUtils.isBlank(userInfo.getRgb()) ? "" : userInfo.getRgb();
            pen.drawString(String.valueOf(userInfo.getQq() + "("+rgb+")"),195, 450);
            pen.setColor(Color.black);

//            String format;
//            if (userInfo.getSignTime() == null) {
//                format = "暂未签到";
//
//            } else {
//                format = DateUtil.format(userInfo.getSignTime(), "yyyy-MM-dd HH:mm:ss");
//            }
//            //签到时间
//            pen.setFont(new Font("黑体", Font.PLAIN, 18));
//            pen.drawString(format, 232, 825);
            //连签次数
            pen.drawString(String.valueOf(userInfo.getSignNumber()), 370, 724);
            //其他称号
//            pen.drawString("暂无", 172, 400);

            double money = EconomyUtil.getMoneyByUser(user);
            double bank = EconomyUtil.getMoneyByBank(user);
            //写入金币
            if (String.valueOf(money).length() > 5) {
                fontSize = 15;
            }
            pen.setFont(new Font("黑体", Font.PLAIN, fontSize));
            //写入总金币
            pen.drawString(String.valueOf(money), 100, 620);

            fontSize = 20;
            pen.setFont(new Font("黑体", Font.PLAIN, fontSize));
            //写入今日获得
            pen.drawString(String.valueOf(userInfo.getSignEarnings()), 81, 724);

            //写入银行
            if (String.valueOf(bank).length() > 5) {
                fontSize = 15;
            }
            pen.setFont(new Font("黑体", Font.PLAIN, fontSize));
            //写入银行金币
            pen.drawString(String.valueOf(bank), 320, 620);

            fontSize = 20;
            double bankEarnings = userInfo.getBankEarnings();
            //写入银行收益
            if (String.valueOf(bankEarnings).length() > 5) {
                fontSize = 15;
            }
            pen.setFont(new Font("黑体", Font.PLAIN, fontSize));
            //写入银行收益金币
            pen.drawString(String.valueOf(bankEarnings), 219, 724);

            // 设置徽章
            Long fish31 = Optional.ofNullable(userInfo.getBackpacks()).orElse(new ArrayList<>()).stream()
                    .filter(userBackpack -> Objects.nonNull(userBackpack) &&
                            FishSignConstant.FISH_31.equals(userBackpack.getPropsCode().toUpperCase(Locale.ROOT))).count();
            if (fish31 > 0) {
                drawFishSign(pen, FishSignConstant.FISH_31, fish31, 56, 849, 144, 911);
            } else {
                pen.setFont(new Font("黑体", Font.BOLD, 20));
                //写入数量
                pen.drawString(0 + "", 144, 911);
            }

            Long fish32 = Optional.ofNullable(userInfo.getBackpacks()).orElse(new ArrayList<>()).stream()
                    .filter(userBackpack -> Objects.nonNull(userBackpack) &&
                            FishSignConstant.FISH_32.equals(userBackpack.getPropsCode().toUpperCase(Locale.ROOT))).count();
            if (fish32 > 0) {
                drawFishSign(pen, FishSignConstant.FISH_32, fish32, 192, 846, 272, 909);
            } else {
                pen.setFont(new Font("黑体", Font.BOLD, 20));
                //写入数量
                pen.drawString(0 + "", 272, 909);
            }
            Long fish33 = Optional.ofNullable(userInfo.getBackpacks())
                    .orElse(new ArrayList<>()).stream()
                    .filter(userBackpack -> Objects.nonNull(userBackpack) &&
                            FishSignConstant.FISH_33.equals(userBackpack.getPropsCode().
                                    toUpperCase(Locale.ROOT))).count();
            if (fish33 > 0) {
                drawFishSign(pen, FishSignConstant.FISH_33, fish33, 319, 846, 396, 909);
            } else {
                pen.setFont(new Font("黑体", Font.BOLD, 20));
                //写入数量
                pen.drawString(0 + "", 396, 909);
            }

            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_SPECIAL) > 0) {
                BadgeInfo badgeInfo = BadgeInfoManager.getBadgeInfo(group.getId(), userInfo.getQq(),  FishSignConstant.FISH_SPECIAL);
                if(Objects.nonNull(badgeInfo)){
                    if(badgeInfo.getContent().length() > 10){
                        pen.setFont(new Font("黑体", Font.BOLD, 15));
                        pen.drawString(badgeInfo.getContent().substring(0,10) +"...", 190, 787);
                    }else {
                        pen.setFont(new Font("黑体", Font.BOLD, 20));
                        pen.drawString(badgeInfo.getContent(), 190, 787);
                    }
                }
            }

            // 90 85
            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_17) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_17, null, 55, 978, 0, 0);
            }

            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_15) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_15, null, 136, 967, 0, 0);
            }

            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_16) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_16, null, 237, 968, 0, 0);
            }

            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_20) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_20, null, 352, 984, 0, 0);
            }
            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_39) > 0) {
               drawFishSign(pen, FishSignConstant.FISH_39, null, 203, 1134, 0, 0);
            }
            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_49) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_49, null, 70, 1125, 0, 0);
            }
            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_62) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_62, null, 264, 1311, 0, 0);
            }
            if (BadgeInfoManager.getCount(group.getId(), userInfo.getQq(), FishSignConstant.FISH_ROD_LEVEL) > 0) {
                drawFishSign(pen, FishSignConstant.FISH_ROD_LEVEL, null, 80, 1321, 0, 0);
            }
            //关闭窗体，释放部分资源
            pen.dispose();
            return image;
        } catch (IOException exception) {
            Log.error("用户管理:个人信息基础信息绘图错误!", exception);
            return null;
        }
    }

    private static void drawFishSign(Graphics2D pen,String key, Long count, int mainX, int mainY,int countX,int countY) {
        InputStream fishSignStream = HuYanEconomy.SIGN_STREAM_MAP.get(key.toUpperCase(Locale.ROOT));
        //验证
        if (fishSignStream == null) {
            Log.error("徽章管理:获取sign错误!" + key);
            return;
        }
        try {
            BufferedImage fishImage = ImageIO.read(fishSignStream);
            fishSignStream.reset();
            //写入头像
            pen.drawImage(fishImage, mainX, mainY, null);
            if(Objects.nonNull(count)){
                pen.setFont(new Font("黑体", Font.BOLD, 20));
                //写入数量
                pen.drawString(count +"", countX, countY);
            }
        } catch (IOException e) {
            Log.error("徽章管理:获取sign错误!" + key);
            return;
        }

    }


}
