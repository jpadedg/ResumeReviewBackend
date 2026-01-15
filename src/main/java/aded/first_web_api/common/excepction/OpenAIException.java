package aded.first_web_api.common.excepction;

public class OpenAIException extends RuntimeException {

    public OpenAIException(String message) {
        super(message);
    }

    public OpenAIException(String message, Object... params) {
        super(String.format(message, params));
    }
}
