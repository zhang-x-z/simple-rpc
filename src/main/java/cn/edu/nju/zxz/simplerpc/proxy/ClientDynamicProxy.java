package cn.edu.nju.zxz.simplerpc.proxy;

import cn.edu.nju.zxz.simplerpc.exceptions.SimpleRPCInvocationException;
import cn.edu.nju.zxz.simplerpc.network.RPCClientUtil;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRequest;
import cn.edu.nju.zxz.simplerpc.network.dto.RPCRespond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ClientDynamicProxy implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDynamicProxy.class);
    private final String serverAddress;
    private final String token;

    public ClientDynamicProxy(String serverAddress, String token) {
        this.serverAddress = serverAddress;
        this.token = token;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            RPCRequest request = new RPCRequest(
                    method.getDeclaringClass().getName(), method.getName(), token, args, method.getParameterTypes()
            );
            RPCRespond respond = RPCClientUtil.sendRPCData(serverAddress, request);
            if (!respond.isSucceed()) {
                throw new SimpleRPCInvocationException(respond.getErrorMessage());
            }
            return respond.getRespond();
        } catch (Throwable e) {
            LOGGER.error("Failed to invoke remote method [" + method.getName() + "] of interface [" + method.getDeclaringClass() + "].");
            if (e instanceof SimpleRPCInvocationException) {
                throw e;
            }
            throw new SimpleRPCInvocationException(e);
        }
    }
}
