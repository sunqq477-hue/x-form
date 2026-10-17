package cn.xiaofuge.x.trigger.http.form;

import cn.xiaofuge.x.api.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class FormExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(FormExceptionHandler.class.getName());

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Object>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponses.failure("400", exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Response<Object>> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponses.failure("404", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> serverError(Exception exception) {
        LOGGER.log(Level.SEVERE, "表单 API 处理失败", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponses.failure("500", "服务暂时不可用，请稍后重试"));
    }
}
