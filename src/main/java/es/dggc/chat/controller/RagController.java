package es.dggc.chat.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.dggc.chat.util.Util;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class RagController {

	@Autowired 
	VectorStore vectorStore;
	
	@PostMapping("/ai/data")
	public ResponseEntity<String> loadData(  
			@RequestPart("file") MultipartFile documento
		    ){
		//@RequestPart("metadata") MetadataDoc metadata
		
		log.info("Documento recibido");
		if (documento.isEmpty()) {
            return ResponseEntity.badRequest().body("Archivo vacío");
        }
		
		//Extraccion del contenido
		String contenido  = Util.extractContent(documento);
		

        String nombre = documento.getOriginalFilename();
        long tamaño = documento.getSize();
        
        
        List<Document> docs = Util.chunckContent(contenido, nombre);
        
        log.info("Se han generado {} documentos", docs.size());
        
        vectorStore.add(docs);
        
        
        //log.info("metadata: {} - {}", metadata.getName(), metadata.getType());
        
        return ResponseEntity.ok("Archivo subido: " + nombre + " (" + tamaño + " bytes)");
	}
	
	
	
	
	@GetMapping("/ai/rag") 
	public ResponseEntity<String> testRAG(@RequestParam(value = "message") String message) {
		log.info("Petición recibida");

			// Retrieve documents similar to a query
			List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
			
			results.stream().forEach(r -> log.info(r.getText()));
			return ResponseEntity.ok("ok");
	}
}
