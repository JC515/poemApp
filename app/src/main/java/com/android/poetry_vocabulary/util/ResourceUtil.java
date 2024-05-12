package com.android.poetry_vocabulary.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;

public class ResourceUtil {

    /**
     * 从 Assets 中获取输入流
     *
     * @param context  上下文
     * @param fileName 文件名
     * @return InputStream
     */
    public static InputStream getInputStreamFromAssets(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            return assetManager.open(fileName);
        } catch (Exception e) {
            Log.e("ResourceUtil", "Error opening asset file: " + fileName, e);
            return null;
        }
    }

    /**
     * 从输入流中解析 XML 文件
     *
     * @param inputStream InputStream
     * @param clazz       要解析的类
     * @param <T>         泛型类型
     * @return 解析后的对象
     * @throws Exception 解析异常
     */
    public static <T> T parseXmlFromInputStream(InputStream inputStream, Class<T> clazz) throws Exception {
        Serializer serializer = new Persister();
        return serializer.read(clazz, inputStream);
    }
}