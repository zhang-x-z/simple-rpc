package cn.edu.nju.zxz.simplerpc.network.impl.netty.dto;

import cn.edu.nju.zxz.simplerpc.network.dto.RPCRequest;

public class NettyRPCRequest extends RPCRequest {
    private String id;

    public NettyRPCRequest() {}

    public NettyRPCRequest(RPCRequest request) {
        super(
                request.getInterfaceName(),
                request.getMethodName(),
                request.getToken(),
                request.getParameters(),
                request.getParameterTypes()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
