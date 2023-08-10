//package cn.chahuyun.economy.utils;
//
//import cn.chahuyun.economy.dto.LotteryLocationInfo;
//import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class Test  {
//    public static void main(String[] args) {
//        InputStream asStream = FileUtils.LOTTERY_STREAM;
//        //转图片处理
//        try {
//            BufferedImage image = ImageIO.read(asStream);
//            asStream.reset();
//            //创建画笔
//            Graphics2D pen = image.createGraphics();
//            //图片与文字的抗锯齿
//            pen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//            pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            pen.setFont(new Font("黑体", Font.BOLD, 35));
//            pen.drawString(String.format("本期强制透开签啦！开签号码%s", "4444"), 540, 307);
//            pen.setFont(new Font("黑体", Font.BOLD, 25));
//            pen.drawString("中奖名单", 158, 278);
//            List<String> list = new ArrayList<>();
//            for(int i =0; i < 100 ; i++){
//              list.add("当前人嫁鸡随鸡就是"+i);
//            }
//
//            int startX = 160;
//            int startY = 280;
//            pen.setFont(new Font("黑体", Font.BOLD, 20));
//            for (int i = 0; i < list.size(); i++) {
//                pen.drawString("中奖人：" + list.get(i)+ "购买号码：" + list.get(i) + "中奖金额" +list.get(i), startX, startY);
//                if (startX >= 1270 - 160) {
//                    startY = startY + 50;
//                    if (startY >= 960 - 280) {
//                        break;
//                    }
//                } else {
//                    startX = startX + 20;
//                }
//
//            }
//            pen.dispose();
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            try {
//                ImageIO.write(image, "png", stream);
//            } catch (IOException e) {
//                Log.error(":!", e);
//                return;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
