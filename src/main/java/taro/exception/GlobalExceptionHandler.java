package taro.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        
        // 로그인 실패나 인증 관련 에러는 401 반환
        if (ex.getMessage().contains("아이디 또는 비밀번호") || 
            ex.getMessage().contains("Invalid") || 
            ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        // 중복 관련 에러는 409 반환
        if (ex.getMessage().contains("이미 사용중")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
        
        // 튜토리얼 미완료 에러는 403 반환
        if (ex.getMessage().contains("튜토리얼")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        
        // 그 외는 400 반환
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
