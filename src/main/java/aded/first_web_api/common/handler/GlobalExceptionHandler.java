package aded.first_web_api.common.handler;

import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import aded.first_web_api.common.excepction.UserNotFoundException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Resource
    private MessageSource messageSource;

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseError responseError(String errorMessage, HttpStatus statusCode) {
        ResponseError responseError = new ResponseError();
        responseError.setStatus("Error");
        responseError.setError(errorMessage);
        responseError.setStatusCode(statusCode.value());
        return responseError;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Usuário não encontrado.";

        return new ResponseEntity<>(responseError(msg, status), headers(), status);
    }

    // @ExceptionHandler(OpenAIException.class)
    // public ResponseEntity<ResponseError> handleOpenAIException(OpenAIException ex, HttpServletRequest request) {
    //     HttpStatus status = HttpStatus.NOT_FOUND;
    //     String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
    //             ? ex.getMessage()
    //             : "Erro ao acessar OpenAI.";

    //     return new ResponseEntity<>(responseError(msg, status), headers(), status);
    // }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            org.springframework.http.HttpStatusCode status,
            WebRequest request
    ) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return new ResponseEntity<>(responseError(msg, httpStatus), this.headers(), httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleGeneric(Exception ex) {
        ex.printStackTrace(); 
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(responseError("Erro interno inesperado.", status), headers(), status);
    }
}
