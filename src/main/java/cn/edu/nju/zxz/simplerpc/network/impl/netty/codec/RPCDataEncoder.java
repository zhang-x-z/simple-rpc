package cn.edu.nju.zxz.simplerpc.network.impl.netty.codec;

import cn.edu.nju.zxz.simplerpc.serialization.DataSerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RPCDataEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf byteBuf) throws Exception {
        byte[] bytes = DataSerializationUtil.serializeToByte(obj);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
