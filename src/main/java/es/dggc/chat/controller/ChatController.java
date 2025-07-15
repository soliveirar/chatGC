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
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
 * -Valida la entrada del usuario. -Busca documentos relevantes relacionados con
 * el mensaje enviado. -Registra el mensaje del usuario para mantener el
 * historial de conversación. -Realiza la consulta al modelo, incluyendo
 * contexto relevante obtenido de los documentos. -Genera y devuelve una
 * respuesta al usuario en un objeto, que incluye el estado y mensaje de
 * resultado.
 * 
 */
@Slf4j
@RestController
public class ChatController {

	private final ChatClient chatClient;

	@Autowired
	VectorStore vectorStore;

	@Autowired
	ChatMemory chatMemory;

	public ChatController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	/**
	 * Metodo que recibe la consulta del usuario, recupera los documentos relevantes
	 * del RAG para el contexto y genera una respuesta
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/api/chat")
	public ResponseEntity<ChatResponse> handleChatRequest(@Valid @RequestBody ChatRequest request) {
		try {

			log.info("Pregunta recibida por el usuario {}: {}", request.getUserId(), request.getMessage());

			// Se incluye mensaje recibido en chatmemory para contexto
			chatMemory.add(request.getUserId(), new UserMessage(request.getMessage()));

			// Establece las caracteristicas para la recuperacion de documentos relevantes
			SearchRequest searchRequest = SearchRequest.builder().query(request.getMessage()).topK(5)
					.similarityThreshold(0.6).build();

			// Se define el prompt para el RAG
			PromptTemplate promptTemplate = new PromptTemplate(Files.readString(Paths.get(Constants.PATH_RAG_PROMPT)));

			// Se define el advisor del RAG incluyendo los parametros de busqueda y el
			// prompt personalizado
			QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore).searchRequest(searchRequest)
					.promptTemplate(promptTemplate).build();

			/*
			 * Devuelve la respuesta generada por el modelo y el contexto. Es decir, la
			 * documentación que se ha considerado relevante para generar la respuesta
			 */
			ChatClientResponse chatClientResponse = this.chatClient.prompt()
					.advisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), advisor).user(request.getMessage())
					.call().chatClientResponse();

			log.info("Se obtiene respuesta del modelo......");
			AssistantMessage response = chatClientResponse.chatResponse().getResult().getOutput();

			// Se registra la respuesta asociada al mismo usario que la consulta
			chatMemory.add(request.getUserId(), response);

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
			e.printStackTrace();
			log.error(e.getMessage());
			ChatResponse chatResponse = new ChatResponse();
			chatResponse.setResponse(e.getMessage());
			chatResponse.setState(State.GENERIC_ERROR);
			return ResponseEntity.internalServerError().body(chatResponse);
		}
	}

}
