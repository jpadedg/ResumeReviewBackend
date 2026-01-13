package aded.first_web_api.common.excepction;

public class OpenAIException extends RuntimeException {

    public OpenAIException(String message) {
        super(message);
    }
}
