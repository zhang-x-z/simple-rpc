package cn.edu.nju.zxz.simplerpc.network;

import cn.edu.nju.zxz.simplerpc.network.dto.RPCRequest;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRespond;
import cn.edu.nju.zxz.simplerpc.network.impl.netty.NettyRPCClient;

public class RPCClientUtil {
    private static final RPCClient dataTransfer = new NettyRPCClient();

    public static RPCRespond sendRPCData(String serverAddress, RPCRequest rpcRequest) {
        try {
            return dataTransfer.sendRPCData(serverAddress, rpcRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
