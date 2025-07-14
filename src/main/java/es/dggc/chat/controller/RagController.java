package es.dggc.chat.controller;

import java.io.IOException;
import java.util.List;

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

import es.dggc.chat.vo.MetadataDoc;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class RagController {

	@Autowired 
	VectorStore vectorStore;
	
	@Autowired
	DocumentService documentService;
	
	//Controlado de subida de documentos
	@PostMapping("/api/data")
	public ResponseEntity<String> loadData(  
			@RequestPart("file") MultipartFile documento,
			@RequestPart("metadata") MetadataDoc metadata
		    ){
		
		log.info("Documento recibido");
		log.info("metadata: {} - {}", metadata.getName(), metadata.getCategory());
		
		if (documento.isEmpty()) {
            return ResponseEntity.badRequest().body("Archivo vacío");
        }
		
		//Set name documento 
		metadata.setName(documento.getOriginalFilename());
		
		List<Document> docs=null;
		try {
			docs = documentService.extractTextFromDocument(documento, metadata);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        log.info("Se han generado {} documentos", docs.size());
        
        vectorStore.add(docs);
        

        String nombre = documento.getOriginalFilename();
        long tamaño = documento.getSize();
        return ResponseEntity.ok("Archivo subido: " + nombre + " (" + tamaño + " bytes)");
	}
	
	@GetMapping("/api/test/") 
	public ResponseEntity<String> testRAG(@RequestParam(value = "message") String message) {
		log.info("Petición recibida");

			// Retrieve documents similar to a query
			List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
			
			results.stream().forEach(r -> log.info(r.getText()));
			return ResponseEntity.ok("ok");
	}
}
