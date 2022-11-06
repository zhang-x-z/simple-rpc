package cn.edu.nju.zxz.simplerpc;

import cn.edu.nju.zxz.simplerpc.annotation.RPCService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleRPCServiceRegister {
    private static final Map<String, Object[]> RPC_SERVICE_MAP = new ConcurrentHashMap<>();
    public static void registerRPCService(Object service) {
        RPCService rpcService = service.getClass().getDeclaredAnnotation(RPCService.class);
        if (rpcService == null) {
            throw new RuntimeException("Failed to register RPC service [" + service +"] because it is not be annotated with @RPCService.");
        }
        String token = rpcService.token();
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token in @RPCService of [" + service + "] can't be empty.");
        }
        RPC_SERVICE_MAP.put(rpcService.rpcInterface().getName(), new Object[] {service, rpcService.token()});
    }

    public static Map<String, Object[]> getRpcServiceMap() {
        return RPC_SERVICE_MAP;
    }
}
