package cn.edu.nju.zxz.simplerpc.serialization.impl;

import cn.edu.nju.zxz.simplerpc.serialization.DataSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements DataSerializer {
    private final ThreadLocal<Kryo> theadLocalKryo;

    public KryoSerializer() {
        theadLocalKryo = ThreadLocal.withInitial(() -> {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.setRegistrationRequired(false);
            return kryo;
        });
    }

    @Override
    public byte[] serializeToByte(Object o) {
        Kryo kryo = theadLocalKryo.get();
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Output output = new Output(outputStream)
        ) {
            kryo.writeClassAndObject(output, o);
            output.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create ByteArrayOutputStream while serializing object to byte[].", e);
        }
    }

    @Override
    public Object deserializeFromByte(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Kryo kryo = theadLocalKryo.get();
        try (Input input = new ByteBufferInput(bytes)) {
            return kryo.readClassAndObject(input);
        }
    }
}
