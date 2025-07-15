package es.dggc.chat.vo;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.dggc.chat.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Objeto para metadatos de documentos para RAG
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MetadataDoc {
	
	//Nombre del documento. Se recupera del propio documento una vez recibido
	private String name;
	
	//Origen del documento. Ej. BOE, BOGC, Intranet
	@NotBlank(message = "El origen del documento es obligatorio. (Ej: BOE, BOGC, Intranet)")
	private String origin;
	
	//Categoria en la que se incluye el documento. Ej. General, Novedades, Destinos.
	@NotBlank(message = "La categoria del documento es un campo obligatorio. (Ej: General, Novedades, Destinos)")
	private String category;
	
	//Nivel de confidencialidad
	@NotBlank(message = "El nivel de confidencialidad es obligatorio")
	private String confidenciality;
	
	//Autor. Puede ser a titulo personal o institucional
	@NotBlank(message = "El autor del documento es obligatorio")
	private String author;
	
	//URL de la que procede el documento
	private String source;
	
	//Referencia, por ejemplo en el caso del BOE
	private String reference;
	
	//Fecha de publicacion
	@NotNull(message = "La fecha de publicacion del documento es obligatoria")
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATE)
	private LocalDate publicationDate;
	
	//Fecha de ultima actualizacion
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATE)
	private LocalDate lastUpdated;
	
	//Se genera un string con todos los metadatos para la visualizacion
	public String getMetadataAsString() {
		ObjectMapper mapper = new ObjectMapper();
		String metadataAsString = null;
		try {
			metadataAsString = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			
		}
		return metadataAsString;
	}
	
}
