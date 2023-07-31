package cn.chahuyun.economy.utils;

import cn.chahuyun.economy.HuYanEconomy;

import java.io.*;
import java.util.Objects;

public class FileUtils {
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
}
