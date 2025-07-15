package es.dggc.chat.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import es.dggc.chat.util.State;
import es.dggc.chat.vo.ChatResponse;

/**
 * Manejador global de excepciones para la aplicación.
 *
 * Esta clase captura y gestiona de forma centralizada las excepciones lanzadas
 * por los controladores REST, incluyendo errores de validación (como campos faltantes
 * o mal formateados en solicitudes con @Valid).
 *
 * Personaliza la respuesta enviada al cliente, devolviendo objetos de tipo {@link ChatResponse}
 * con mensajes y estados adecuados según el tipo de error.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ChatResponse response = new ChatResponse();
        response.setState(State.MISSING_FIELDS);
        response.setTime(LocalDateTime.now());
        response.setResponse(errorMessage);

        return ResponseEntity.badRequest().body(response);
    }
}


