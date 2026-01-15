package aded.first_web_api.common.excepction;

public class TypeErrorException extends RuntimeException {

    public TypeErrorException(String message) {
        super(message);
    }

    public TypeErrorException(String message, Object... params) {
        super(String.format(message, params));
    }
}
