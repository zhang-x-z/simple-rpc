package cn.edu.nju.zxz.simplerpc.network.impl.netty.codec;

import cn.edu.nju.zxz.simplerpc.serialization.DataSerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RPCDataDecoder extends ByteToMessageDecoder {
    private static final int HEADER_LENGTH = 4;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEADER_LENGTH) {
            throw new RuntimeException("Decode failed. Readable bytes less than header length.");
        }
        in.markReaderIndex();
        int length = in.readInt();
        if (length <= 0) {
            throw new RuntimeException("Decode failed. Data length <= 0.");
        }
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        out.add(DataSerializationUtil.deserializeFromByte(bytes));
    }
}
