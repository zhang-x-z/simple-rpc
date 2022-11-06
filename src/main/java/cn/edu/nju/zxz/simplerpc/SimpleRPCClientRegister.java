package cn.edu.nju.zxz.simplerpc;

import cn.edu.nju.zxz.simplerpc.annotation.RPCInterface;
import cn.edu.nju.zxz.simplerpc.proxy.ClientDynamicProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class SimpleRPCClientRegister {
    public static <T> void registerRPCClient(T instance) {
        try {
            Class<?> clazz = instance.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                RPCInterface rpcInterface = field.getDeclaredAnnotation(RPCInterface.class);
                if (rpcInterface != null) {
                    String addr = rpcInterface.serverAddress();
                    String token = rpcInterface.token();
                    field.set(instance, Proxy.newProxyInstance(
                            SimpleRPCClientRegister.class.getClassLoader(),
                            new Class[] {field.getType()},
                            new ClientDynamicProxy(addr, token)
                    ));
                }
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to register " + instance, e);
        }
    }
}
