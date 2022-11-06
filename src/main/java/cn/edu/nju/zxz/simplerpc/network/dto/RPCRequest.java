package cn.edu.nju.zxz.simplerpc.network.dto;

public class RPCRequest {
    private String interfaceName;
    private String methodName;
    private String token;
    private Object[] parameters;
    private Class<?>[] parameterTypes;

    public RPCRequest() {}

    public RPCRequest(
            String interfaceName, String methodName, String token, Object[] parameters, Class<?>[] parameterTypes
    ) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.token = token;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
