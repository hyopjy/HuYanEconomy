package cn.chahuyun.economy.manager;

import cn.chahuyun.economy.HuYanEconomy;
import cn.chahuyun.economy.entity.UserInfo;
import cn.chahuyun.economy.utils.EconomyUtil;
import cn.chahuyun.economy.utils.Log;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

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

        try {
            Image image = Contact.uploadImage(subject, new URL(sender.getAvatarUrl(AvatarSpec.SMALL)).openConnection().getInputStream());
            singleMessages.append(image);
        } catch (IOException e) {
            Log.error("用户管理:查询个人信息上传图片出错!", e);
        }
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
        HuYanEconomy instance = HuYanEconomy.INSTANCE;
        User user = userInfo.getUser();
        String special = "";
        String groupUserName = "";
        if (Objects.nonNull(group)) {
            NormalMember normalMember = group.get(userInfo.getUser().getId());
            if (Objects.nonNull(normalMember)) {
                special = normalMember.getSpecialTitle();
                groupUserName = normalMember.getNameCard();
                if (groupUserName.length() > 11) {
                    groupUserName = groupUserName.substring(0, 11) + "...";
                }
            }
        }

        try {
            int index = RandomUtil.randomInt(4) +1;
            InputStream asStream = instance.getResourceAsStream("sign" + index + ".png");

            //验证
            if (asStream == null) {
                Log.error("用户管理:个人信息图片底图获取错误!");
                return null;
            }
            //转图片处理
            BufferedImage image = ImageIO.read(asStream);
            //创建画笔
            Graphics2D pen = image.createGraphics();
            //图片与文字的抗锯齿
            pen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //获取头像链接
            String avatarUrl = user.getAvatarUrl(AvatarSpec.LARGE);
            BufferedImage avatar = ImageIO.read(new URL(avatarUrl));
            BufferedImage avatarRounder = UserManager.makeRoundedCorner(avatar, 50);
            //写入头像
            pen.drawImage(avatarRounder, 195, 285, null);

            String userInfoName = userInfo.getName();
            int fontSize;
            //如果是群 根据管理员信息改变颜色
            if (user instanceof Member) {
                MemberPermission permission = ((Member) user).getPermission();
                if (permission == MemberPermission.OWNER) {
                    pen.setColor(Color.YELLOW);
                } else if (permission == MemberPermission.ADMINISTRATOR) {
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


//            pen.setColor(); todo 称号预留

            pen.setColor(Color.white);
            fontSize = 20;
            pen.setFont(new Font("黑体", Font.PLAIN, fontSize));
            //id
            pen.drawString(String.valueOf(userInfo.getQq()),195, 450);
            pen.setColor(Color.black);

            String format;
            if (userInfo.getSignTime() == null) {
                format = "暂未签到";

            } else {
                format = DateUtil.format(userInfo.getSignTime(), "yyyy-MM-dd HH:mm:ss");
            }
            //签到时间
            pen.setFont(new Font("黑体", Font.PLAIN, 18));
            pen.drawString(format, 232, 825);
            //连签次数
            pen.drawString(String.valueOf(userInfo.getSignNumber()), 181, 875);
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
            pen.drawString(String.valueOf(userInfo.getSignEarnings()), 100, 740);

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
            pen.drawString(String.valueOf(bankEarnings), 320, 740);

//            fontSize = 15;
//            pen.setColor(new Color(255, 255, 255, 230));
//            pen.setFont(new Font("黑体", Font.ITALIC, fontSize));
//
//            pen.drawString("by Mirai & HuYanEconomy(壶言经济) " + HuYanEconomy.version, 540, 525);

            // 设置特殊称号
            // special
            if(!StrUtil.isBlank(special)){
                fontSize = 20;
                pen.setFont(new Font("黑体", Font.BOLD, fontSize));
                pen.drawString(special, 220, 957);
            }

            //关闭窗体，释放部分资源
            pen.dispose();
            return image;
        } catch (IOException exception) {
            Log.error("用户管理:个人信息基础信息绘图错误!", exception);
            return null;
        }
    }
}
