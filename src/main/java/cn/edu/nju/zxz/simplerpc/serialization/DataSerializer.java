package cn.edu.nju.zxz.simplerpc.serialization;

public interface DataSerializer {
    byte[] serializeToByte(Object o);
    Object deserializeFromByte(byte[] bytes);
}
