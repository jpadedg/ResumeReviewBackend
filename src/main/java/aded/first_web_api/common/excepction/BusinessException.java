package aded.first_web_api.common.excepction;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }   

    public BusinessException(String message, Object... params) {
        super(String.format(message, params));
    }
}
