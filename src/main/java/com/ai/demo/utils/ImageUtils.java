package com.ai.demo.utils;

import java.io.FileInputStream;
import java.util.Base64;

public class ImageUtils {

    public static String imageToBase64(String imagePath) throws Exception {
        FileInputStream inputStream = new FileInputStream(imagePath);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();
        // 拼接图片格式前缀（必须，模型需识别格式）​
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
    }

}
