package aded.first_web_api.excepction;

public class OpenAIException extends RuntimeException {

    public OpenAIException(String message) {
        super(message);
    }
}
