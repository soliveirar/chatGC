package es.dggc.chat.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import es.dggc.chat.util.Constants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa un mensaje recibido por parte del cliente
 * Incluye el identificador del usuario, la consulta y fecha/hora de la misma
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

	@NotBlank(message = "El identificador de usuario es obligatorio")
	private String userId;
	
	@NotBlank(message = "El mensaje es obligatorio")
	private String message;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATETIME)
	private LocalDateTime time;
	
}
