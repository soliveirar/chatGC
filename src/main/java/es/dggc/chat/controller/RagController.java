package es.dggc.chat.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.dggc.chat.service.DocumentService;
import es.dggc.chat.util.State;
import es.dggc.chat.util.Utils;
import es.dggc.chat.vo.MetadataDoc;
import es.dggc.chat.vo.RagResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador que gestiona la inclusion de nuevos documentos en la base de datos
 * - Valida el documento y los metadatos recibidos
 * - Extrae el texto del documento y genera los tokens
 * - Almacena los tokens generados
 * - Envia una respuesta al usuario indicando que el proceso ha finalizado o con un 
 * error en caso de producirse
 */
@Slf4j
@RestController
public class RagController {

	@Autowired
	VectorStore vectorStore;

	@Autowired
	DocumentService documentService;

	/**
	 * Endpoint para cargar un documento junto con sus metadatos.
	 * 
	 * Este método recibe un archivo (por ejemplo, un PDF) y un objeto con metadatos
	 * asociados, los procesa extrayendo su contenido, y asocia los metadatos a los
	 * fragmentos del documento.
	 * 
	 * Los documentos procesados pueden ser utilizados posteriormente para
	 * enriquecer respuestas generadas por el modelo mediante técnicas de RAG
	 * (Retrieval-Augmented Generation).
	 * 
	 * @param file. Documento PDF subido por el usuario
	 * @param metadata. Metadatos asociados al documento en formato JSON
	 * @return
	 */
	@PostMapping("/api/data")
	public ResponseEntity<RagResponse> loadData(@RequestPart("file") MultipartFile file,
			@RequestPart("metadata") MetadataDoc metadata) {
		try {

			log.info("Nuevo documento recibido..............");
			log.info("Nombre: {}", file.getOriginalFilename());
			log.info("Metadata: {}", metadata.metadataAsString());

			// Validacion del documento
			State state = Utils.validateDocument(file);
			if (state.equals(State.OK)) {

				// Registra el nombre del documento subido
				metadata.setName(file.getOriginalFilename());

				// Extrae el texto del documento y lo divide en fragmentos (tokens)
				// generando un Document por cada uno
				List<Document> docs = documentService.extractTextFromDocument(file, metadata);
				log.info("Se han generado {} documentos", docs.size());

				// Se incluyen los documentos en la base de datos (Chroma)
				vectorStore.add(docs);

				// Respuesta al usuario cuando el proceso ha finalizado
				RagResponse response = new RagResponse();
				response.setFilename(metadata.getName());
				response.setSize(file.getSize());
				response.setState(state);
				response.setTime(LocalDateTime.now());
				return ResponseEntity.ok().body(response);

			} else {
				// Respuesta al usuario cuando el documento no es valido
				RagResponse response = new RagResponse();
				response.setState(state);
				response.setTime(LocalDateTime.now());
				return ResponseEntity.badRequest().body(response);
			}

		} catch (IOException e) {
			return buildErrorResponse(e, State.DOCUMENT_READ_ERROR);
		} catch (Exception ex) {
			return buildErrorResponse(ex, State.GENERIC_ERROR);
		}

	}
	
	/**
	 * Construye una respuesta de error a partir de una excepción y un estado de error específico.
	 * @param exception.Excepción que provocó el error 
	 * @param state Estado que representa el tipo de error 
	 * @return
	 */
	private ResponseEntity<RagResponse> buildErrorResponse(Exception exception, State state) {
	    log.error("Error: {}", exception.getMessage(), exception);

	    RagResponse response = new RagResponse();
	    response.setState(state);
	    response.setMessage(exception.getMessage());
	    response.setTime(LocalDateTime.now());

	    return ResponseEntity.internalServerError().body(response);
	}


//	@GetMapping("/api/test/") 
//	public ResponseEntity<String> testRAG(@RequestParam(value = "message") String message) {
//		log.info("Petición recibida");
//
//			// Retrieve documents similar to a query
//			List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
//			
//			results.stream().forEach(r -> log.info(r.getText()));
//			return ResponseEntity.ok("ok");
//	}
}
