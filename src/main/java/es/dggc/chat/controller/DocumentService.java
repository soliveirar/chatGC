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

/**
 * Servicio responsable de procesar documentos cargados por el usuario. -
 * Lectura del contenido del archivo usando Apache Tika. - Division del texto en
 * fragmentos manejables utilizando un TokenTextSplitter. - Asignacion de
 * metadatos proporcionados por el usuario a cada fragmento del documento.
 */
@Service
public class DocumentService {

	/**
	 * Extrae el texto de un documento cargado por el usuario, lo divide en
	 * fragmentos y asocia los metadatos proporcionados a cada fragmento.
	 *
	 * @param file.        Documento cargado
	 * @param metadataDoc. Objeto que contiene los metadatos asociados al documento.
	 * @return Una lista de objetos, cada uno con una porción de texto y sus
	 *         metadatos.
	 * @throws IOException Si ocurre un error al leer el contenido del archivo.
	 */
	public List<Document> extractTextFromDocument(MultipartFile file, MetadataDoc metadataDoc) throws IOException {

		// Crea un recurso a partir del archivo cargado
		Resource resource = new InputStreamResource(file.getInputStream());

		// Lee el contenido del documento usando Apache Tika
		TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);

		// Divide el texto en fragmentos (chunks) utilizando reglas de tokenización
		List<Document> documents = new TokenTextSplitter(1024, // chunkSize
				200, // chunkOverlap
				200, // maxChunks
				5000, // maxTokenCount
				true // preserveWhitespace
		).apply(tikaDocumentReader.read());
		;

		// Convierte los metadatos a un Map<String, String> para asignarlos a los
		// documentos
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		Map<String, String> metadata = mapper.convertValue(metadataDoc, new TypeReference<Map<String, String>>() {
		});

		// Asigna los metadatos a cada fragmento del documento
		documents.stream().forEach(d -> d.getMetadata().putAll(metadata));

		return documents;

	}
}
