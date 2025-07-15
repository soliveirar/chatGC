package es.dggc.chat.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import es.dggc.chat.util.Constants;
import es.dggc.chat.util.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa un la respuesta del modelo a la consulta del cliente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
	

	private String userId;
	private String response;
	private List<String> documents;
	private State state;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATETIME)
	private LocalDateTime time;
	
}
