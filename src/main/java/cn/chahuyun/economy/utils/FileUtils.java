package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.HuYanEconomy;

import java.io.*;
import java.util.Objects;

public class FileUtils {
    public static InputStream LOTTERY_STREAM = null;

    static {
        try {
            InputStream in = HuYanEconomy.INSTANCE.getResourceAsStream("lottery.png");
            if (!Objects.isNull(in)) {
                LOTTERY_STREAM = new ByteArrayInputStream(in.readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static void getSignFishStream() {
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-15", getByteArrayInputStream("FISH-15.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-16", getByteArrayInputStream("FISH-16.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-17", getByteArrayInputStream("FISH-17.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-20", getByteArrayInputStream("FISH-20.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-31", getByteArrayInputStream("FISH-31.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-32", getByteArrayInputStream("FISH-32.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-33", getByteArrayInputStream("FISH-33.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-39", getByteArrayInputStream("FISH-39.png"));
        HuYanEconomy.SIGN_STREAM_MAP.put("FISH-49", getByteArrayInputStream("FISH-49.png"));
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
