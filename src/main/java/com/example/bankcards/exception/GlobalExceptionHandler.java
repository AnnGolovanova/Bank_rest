package com.example.bankcards.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> onValidation(MethodArgumentNotValidException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", "Validation failed");
        body.put("errors", ex.getBindingResult().getFieldErrors()
                .stream().map(f -> Map.of("field", f.getField(), "error", f.getDefaultMessage())).toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class, NoSuchElementException.class})
    public ResponseEntity<?> onBusiness(RuntimeException ex) {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }
}
