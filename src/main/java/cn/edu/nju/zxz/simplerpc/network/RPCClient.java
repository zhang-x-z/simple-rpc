package cn.edu.nju.zxz.simplerpc.network;

import cn.edu.nju.zxz.simplerpc.network.dto.RPCRequest;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRespond;

public interface RPCClient {
    RPCRespond sendRPCData(String serverAddress, RPCRequest request) throws Exception;
}
