package es.dggc.chat.vo;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import es.dggc.chat.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objeto para metadatos de documentos para RAG
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDoc {
	
	private String name;
	private String origin;
	private String category;
	private String confidenciality;
	private String author;
	private String source;
	private String reference;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATE)
	private LocalDate publicationDate;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, 
			pattern = Constants.FORMAT_LOCAL_DATE)
	private LocalDate lastUpdated;
	
}
