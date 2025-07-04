package es.dggc.chat.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
	    	List<Document> documents = new TokenTextSplitter(1000, 400, 10, 5000, true)
	    			.apply(tikaDocumentReader.read());
	    	
	    	//Set metadata
	    	Map<String, String> metadata = new HashMap<String, String>();
	    	metadata.put("source", metadataDoc.getName());
	    	metadata.put("category", metadataDoc.getCategory());
	    	metadata.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
	    	metadata.put("confidenciality", metadataDoc.getConfidenciality());
	    	metadata.put("author", metadataDoc.getAuthor());
	    	metadata.put("origin", metadataDoc.getOrigin());
	    	documents.stream().forEach(d -> d.getMetadata().putAll(metadata));
	    	
	        return documents;
	    }
}
