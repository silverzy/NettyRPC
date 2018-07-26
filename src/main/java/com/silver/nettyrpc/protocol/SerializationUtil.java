package com.silver.nettyrpc.protocol;
import com.alibaba.fastjson.JSON;


public class SerializationUtil {


    private SerializationUtil() {
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        String json = JSON.toJSONString(obj);
        return  json.getBytes();
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        String json = new String(data);
        try {
            T obj = JSON.parseObject(json,cls);
            return obj;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
