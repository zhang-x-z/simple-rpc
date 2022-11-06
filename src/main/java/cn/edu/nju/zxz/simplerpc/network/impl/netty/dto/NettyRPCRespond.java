package cn.edu.nju.zxz.simplerpc.network.impl.netty.dto;

import cn.edu.nju.zxz.simplerpc.network.dto.RPCRespond;

public class NettyRPCRespond extends RPCRespond {
    private String id;

    public NettyRPCRespond() {
        super();
    }

    public NettyRPCRespond(boolean succeed, Object respond) {
        super(succeed, respond);
    }

    public NettyRPCRespond(boolean succeed, String errorMessage) {
        super(succeed, errorMessage);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
