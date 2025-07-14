package es.dggc.chat.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.dggc.chat.vo.MetadataDoc;

@Service
public class DocumentService {

	
		/**
		 * 
		 * @param file
		 * @return
		 * @throws IOException
		 */
	    public List<Document> extractTextFromDocument(MultipartFile file, MetadataDoc metadataDoc) throws IOException {
	    	
	    	Resource resource = new InputStreamResource(file.getInputStream());
	    	TikaDocumentReader tikaDocumentReader = new TikaDocumentReader (resource);
	    	
	    	/*
	    	 * defaultChunkSize:El tamaño objetivo de cada fragmento de texto en tokens (predeterminado: 800).
			 minChunkSizeChars:El tamaño mínimo de cada fragmento de texto en caracteres (predeterminado: 350).
			 minChunkLengthToEmbed:La longitud mínima de un fragmento que se incluirá (valor predeterminado: 5).
             maxNumChunks:El número máximo de fragmentos a generar a partir de un texto (valor predeterminado: 10000).
             keepSeparator:Si se deben mantener los separadores (como nuevas líneas) en los fragmentos (valor predeterminado: verdadero).
	    	 */
	    	List<Document> documents = new TokenTextSplitter(1024,  // chunkSize
	    			200,  // chunkOverlap
	    			200,  // maxChunks
	    			5000, // maxTokenCount
	    			true) // preserveWhitespace
	    			.apply(tikaDocumentReader.read());
	    	
	
	    	//Metadata
	    	//Conversion del objeto de metadatos a key-value para su insercion en bbdd
	    	ObjectMapper mapper = new ObjectMapper();
	    	mapper.registerModule(new JavaTimeModule());
	    	Map<String, String> metadata = mapper.convertValue(metadataDoc, new TypeReference<Map<String, String>>() {});
	    	
	    	//Asignacion de metadatos a los documentos
	    	documents.stream().forEach(d -> d.getMetadata().putAll(metadata));
	    	
	    	return documents;
	    
	    }
}
