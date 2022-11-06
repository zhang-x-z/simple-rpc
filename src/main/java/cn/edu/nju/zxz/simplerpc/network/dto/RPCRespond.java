package cn.edu.nju.zxz.simplerpc.network.dto;

public class RPCRespond {
    private boolean succeed;
    private Object respond;
    private String errorMessage;

    public RPCRespond() {}

    public RPCRespond(boolean succeed, Object respond) {
        this.succeed = succeed;
        this.respond = respond;
    }

    public RPCRespond(boolean succeed, String errorMessage) {
        this.succeed = succeed;
        this.errorMessage = errorMessage;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public Object getRespond() {
        return respond;
    }

    public void setRespond(Object respond) {
        this.respond = respond;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
