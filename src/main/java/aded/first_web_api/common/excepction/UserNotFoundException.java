package aded.first_web_api.common.excepction;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }   

    public UserNotFoundException(String message, Object... params) {
        super(String.format(message, params));
    }
}
