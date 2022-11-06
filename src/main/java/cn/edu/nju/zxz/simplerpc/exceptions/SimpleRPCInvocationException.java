package cn.edu.nju.zxz.simplerpc.exceptions;

public class SimpleRPCInvocationException extends RuntimeException {
    public SimpleRPCInvocationException(String errorMessage) {
        super(errorMessage);
    }

    public SimpleRPCInvocationException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public SimpleRPCInvocationException(Throwable cause) {
        super(cause);
    }
}
