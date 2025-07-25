package es.dggc.chat.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import es.dggc.chat.util.Constants;
import es.dggc.chat.util.State;
import es.dggc.chat.vo.ChatRequest;
import es.dggc.chat.vo.ChatResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador que procesa una solicitud de chat recibida desde el cliente.
 *
 * -Valida la entrada del usuario. 
 * -Busca documentos relevantes relacionados con el mensaje enviado. 
 * -Registra el mensaje del usuario para mantener el historial de conversación. 
 * -Realiza la consulta al modelo, incluyendo contexto relevante obtenido de los documentos. 
 * -Genera y devuelve una respuesta al usuario en un objeto, que incluye el estado y mensaje de
 * resultado.
 * 
 */
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class ChatController {

	@Autowired
	ChatClient chatClient;

	@Autowired
	VectorStore vectorStore;

	@Autowired
	ChatMemory chatMemory;

	/**
	 * Metodo que recibe la consulta del usuario, recupera los documentos relevantes
	 * del RAG para el contexto y genera una respuesta
	 * 
	 * @param request Peticion recibida por parte del usuario que contiene id y consulta
	 * @return
	 */
	@PostMapping("/api/chat")
	public ResponseEntity<ChatResponse> handleChatRequest(@Valid @RequestBody ChatRequest request) {
		try {

			log.info("Pregunta recibida por el usuario {}: {}", request.getUserId(), request.getMessage());

			// Se establece que messageChatMemory registre el identificador de usuario
			MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).
			conversationId(request.getUserId()).build();

			// Establece las caracteristicas para la recuperacion de documentos relevantes
			SearchRequest searchRequest = SearchRequest.builder().query(request.getMessage()).topK(5)
					.similarityThreshold(0.6).build();

			// Se define el prompt para el RAG
			PromptTemplate promptTemplate = new PromptTemplate(Files.readString(Paths.get(Constants.PATH_RAG_PROMPT)));

			// Se define el advisor del RAG incluyendo los parametros de busqueda y el
			// prompt personalizado
			QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(searchRequest)
					.promptTemplate(promptTemplate).build();

			/*
			 * Devuelve la respuesta generada por el modelo y el contexto. Es decir, la
			 * documentación que se ha considerado relevante para generar la respuesta
			 */
			ChatClientResponse chatClientResponse = this.chatClient.prompt()
					.advisors(messageChatMemoryAdvisor, 
							  questionAnswerAdvisor)
					.user(request.getMessage())
					.call().chatClientResponse();

			log.info("Se obtiene respuesta del modelo......");
			AssistantMessage response = chatClientResponse.chatResponse().getResult().getOutput();

			// Se genera la respuesta al usuario con los datos obtenidos del modelo
			ChatResponse chatResponse = new ChatResponse();
			chatResponse.setResponse(response.getText());
			chatResponse.setTime(LocalDateTime.now());
			chatResponse.setUserId(request.getUserId());

			// Recuperacion de documentos relevantes
			List<String> rag = chatClientResponse.context().entrySet().stream()
					.filter(entry -> Constants.RAG_KEY.equals(entry.getKey()))
					.flatMap(entry -> ((List<Document>) entry.getValue()).stream())
					.map(doc -> doc.getMetadata().toString()).collect(Collectors.toList());
			chatResponse.setDocuments(rag);
			chatResponse.setState(State.OK);

			return ResponseEntity.ok(chatResponse);

		} catch (Exception e) {
			return buildErrorResponse(e, State.GENERIC_ERROR);
		}
	}

	/**
	 * Construye una respuesta de error a partir de una excepción y un estado de error específico.
	 * @param exception.Excepción que provocó el error 
	 * @param state Estado que representa el tipo de error 
	 * @return
	 */
	private ResponseEntity<ChatResponse> buildErrorResponse(Exception exception, State state) {
		log.error("Error: {}", exception.getMessage(), exception);
		
		ChatResponse response = new ChatResponse();
		response.setResponse(exception.getMessage());
		response.setState(state);
		response.setTime(LocalDateTime.now());
		
		return ResponseEntity.internalServerError().body(response);
	}

}
