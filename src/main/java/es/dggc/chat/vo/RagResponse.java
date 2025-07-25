package es.dggc.chat.vo;

import java.time.LocalDateTime;

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
public class RagResponse {
	
	//Nombre del documento subido
	private String filename;
	
	//Tamanyio del documento subido
	private long size;
	
	//Estado de la respuesta
	private State state;
	
	//Campo para incluir informacion adicional al estado
	private String message;
	
	//Fecha de envio de la respuesta
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATETIME)
	private LocalDateTime time;
	
}
