package es.dggc.chat.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import es.dggc.chat.vo.ChatRequest;
import es.dggc.chat.vo.ChatResponse;
import es.dggc.chat.vo.State;
import lombok.extern.slf4j.Slf4j;


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
	
	
	@PostMapping("/api/chat")
    public ResponseEntity<ChatResponse> generate(@RequestBody ChatRequest request) {
        try {
        	
        	log.info("Pregunta recibida en la petición {}: {}",
        			request.getUserId(), request.getMessage());
        	
        	//Se incluye mensaje recibido en chatmemory
        	chatMemory.add(request.getUserId(), new UserMessage(request.getMessage()));
        	/*
        	 * Devuelve la respuesta generada por el modelo y el contexto
        	 * Es decir la documentación que se ha considerado relevante para generar la respuesta
        	 */
           SearchRequest searchRequest = SearchRequest.builder()
                    .query(request.getMessage())
                    .topK(3)
                    .similarityThreshold(0.5)
                    .build();
          
          QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
        		    .searchRequest(searchRequest)
        		    .build();

       	ChatClientResponse chatClientResponse = this.chatClient.prompt()
           .advisors(
        		   MessageChatMemoryAdvisor.builder(chatMemory).build(),
        		   advisor
        		   )
           .user(request.getMessage())
           .call()
           .chatClientResponse();
           
        	
//        	ChatClientResponse chatClientResponse = chatClient.prompt()
//        			.advisors(
//        			        MessageChatMemoryAdvisor.builder(chatMemory).build(),   //Chatmemory
//        			        QuestionAnswerAdvisor.builder(vectorStore).build() 	  	//RAG
//        			    )
//        		    .user(request.getMessage())
//        		    .call()
//        		    .chatClientResponse();
        	
            log.info("Se obtiene respuesta del modelo");
            AssistantMessage response = chatClientResponse.chatResponse().getResult().getOutput();
            
           
            //Se guarda la respuesta
            chatMemory.add(request.getUserId(), response);
            
            log.info("Mensajes en memoria para la conversacion de {} : {}", chatMemory.get(request.getUserId()).size());
            
            log.info("Informacion recuperada de los documentos: ");
           
            List<String> rag = new ArrayList<String>();
            chatClientResponse.context().forEach((key, value) -> {
                if(key.equals("qa_retrieved_documents")) {
                	List<Document> docs = (ArrayList<Document>)value;
                	docs.forEach(d -> {
                		log.info(d.getMetadata().toString());
                		rag.add(d.getMetadata().toString());
                	});
                }
            });
            
            //Response
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setResponse(response.getText());
            chatResponse.setTime(LocalDateTime.now());
            chatResponse.setUserId(request.getUserId());
            chatResponse.setDocuments(rag);
            State state = new State("000", "OK");
            chatResponse.setState(state);
            
            return ResponseEntity.ok(chatResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ChatResponse chatResponse = new ChatResponse();
            State state = new State("001", "ERROR:  " + e.getMessage());
            chatResponse.setState(state);
            return ResponseEntity.internalServerError().body(chatResponse);
        }
    }
	
	
}
