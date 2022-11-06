package cn.edu.nju.zxz.simplerpc.serialization;

import cn.edu.nju.zxz.simplerpc.serialization.impl.KryoSerializer;

public class DataSerializationUtil {
    private static final DataSerializer dataSerializer = new KryoSerializer();

    public static byte[] serializeToByte(Object o) {
        return dataSerializer.serializeToByte(o);
    }

    public static Object deserializeFromByte(byte[] bytes) {
        return dataSerializer.deserializeFromByte(bytes);
    }
}
