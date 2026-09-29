package ma.lias.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<Map<String,Object>> response(ResponseStatusException e){ return ResponseEntity.status(e.getStatusCode()).body(Map.of("timestamp",LocalDateTime.now(),"status",e.getStatusCode().value(),"message",e.getReason()==null?"Erreur":e.getReason())); }
  @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException e){ return ResponseEntity.badRequest().body(Map.of("timestamp",LocalDateTime.now(),"status",400,"message","Validation échouée","errors",e.getBindingResult().getFieldErrors().stream().map(f -> f.getField()+": "+f.getDefaultMessage()).toList())); }
  @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,Object>> badRequest(IllegalArgumentException e){ return ResponseEntity.badRequest().body(Map.of("timestamp",LocalDateTime.now(),"status",400,"message",e.getMessage()==null?"Requête invalide":e.getMessage())); }
  @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,Object>> generic(Exception e){ log.error("Unhandled API error",e); return ResponseEntity.status(500).body(Map.of("timestamp",LocalDateTime.now(),"status",500,"message","Une erreur interne est survenue.")); }
}
