package ma.lias.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<Map<String,Object>> response(ResponseStatusException e){ return ResponseEntity.status(e.getStatusCode()).body(Map.of("timestamp",LocalDateTime.now(),"status",e.getStatusCode().value(),"message",e.getReason()==null?"Erreur":e.getReason())); }
  @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e){ return ResponseEntity.badRequest().body(Map.of("timestamp",LocalDateTime.now(),"status",400,"message","Validation échouée","errors",e.getBindingResult().getFieldErrors().stream().map(f -> f.getField()+": "+f.getDefaultMessage()).toList())); }
  @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,Object>> generic(Exception e){ return ResponseEntity.status(500).body(Map.of("timestamp",LocalDateTime.now(),"status",500,"message",e.getMessage()==null?"Erreur serveur":e.getMessage())); }
}
