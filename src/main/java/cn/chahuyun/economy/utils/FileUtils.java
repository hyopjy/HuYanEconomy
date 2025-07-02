package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.HuYanEconomy;

import java.io.*;
import java.util.Objects;

import static cn.chahuyun.economy.constant.FishSignConstant.*;

public class FileUtils {
//    public static InputStream BASE_INFO_STREAM = null;
//
//
//    static {
//        try {
//            InputStream in = HuYanEconomy.INSTANCE.getResourceAsStream("收集册info.png");
//            if (!Objects.isNull(in)) {
//                BASE_INFO_STREAM = new ByteArrayInputStream(in.readAllBytes());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static ByteArrayOutputStream getBOS(InputStream in) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            BufferedInputStream br = new BufferedInputStream(in);
            for(int c=0;(c=br.read())!=-1;){
                bos.write(c);
            }
            br.close();
        } catch (Exception e) {
            //
        }
        return bos;
    }

    public static ByteArrayOutputStream getInputStream(int i) {
        try {
            InputStream in = HuYanEconomy.INSTANCE.getResourceAsStream("sign" + i + ".png");
            if (Objects.isNull(in)) {
                return null;
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(in.readAllBytes());
            HuYanEconomy.INPUT_STREAM_MAP.put(i, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ByteArrayInputStream loadBaseInfoPNG() {
        try {
            InputStream in = HuYanEconomy.INSTANCE.getResourceAsStream("收集册info.png");
            if (!Objects.isNull(in)) {
                HuYanEconomy.BASE_INFO_STREAM = new ByteArrayInputStream(in.readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getSignFishStream() {

        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_15, getByteArrayInputStream("FBPFKL.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_16, getByteArrayInputStream("FBTNKL.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_17, getByteArrayInputStream("警笛.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_20, getByteArrayInputStream("小喇叭鱼.png"));

//        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-31", getByteArrayInputStream("FISH-31.png"));
//        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-32", getByteArrayInputStream("FISH-32.png"));
//        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-33", getByteArrayInputStream("FISH-33.png"));

        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_39, getByteArrayInputStream("WDITBB4.0.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_49, getByteArrayInputStream("体育生与艺术生.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_62, getByteArrayInputStream("uranus.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_ROD_LEVEL, getByteArrayInputStream("fishing.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_95, getByteArrayInputStream("可乐华夫.png"));

        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_133, getByteArrayInputStream("GAP the series.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put(FISH_125, getByteArrayInputStream("落花落.png"));

        //ball.png
        //   HuYanEconomy.SIGN_STREAM_MAP.put("FISH-95", getByteArrayInputStream("ball.png"));
        //昆三陵鱼.png
       //  HuYanEconomy.SIGN_STREAM_MAP.put("FISH-95", getByteArrayInputStream("昆三陵鱼.png"));
    }

    public static ByteArrayInputStream getByteArrayInputStream(String key) {
        try {
            InputStream in = HuYanEconomy.INSTANCE.getResourceAsStream(key);
            if (Objects.isNull(in)) {
                return null;
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(in.readAllBytes());
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
