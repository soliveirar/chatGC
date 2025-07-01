package es.dggc.chat.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import es.dggc.chat.vo.ChatRequest;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class ChatController {

	private final ChatClient chatClient;
	
	@Autowired 
	VectorStore vectorStore;
	
	
	public ChatController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}
	
	
	@PostMapping("/ai/generate")
    public ResponseEntity<String> generate(@RequestBody ChatRequest request) {
        try {
        	
        	log.info("Pregunta recibida en la petición: {}", request.getMessage());
        	/*
        	 * Devuelve la respuesta generada por el modelo y el contexto
        	 * Es decir la documentación que se ha considerado relevante para generar la respuesta
        	 */
        	ChatClientResponse chatClientResponse = chatClient.prompt()
        			.advisors(
        			        //MessageChatMemoryAdvisor.builder(chatMemory).build(), //Chatmemory
        			        QuestionAnswerAdvisor.builder(vectorStore).build() 	  	//RAG
        			    )
        		    .user(request.getMessage())
        		    .call()
        		    .chatClientResponse();
        	
            log.info("Se obtiene respuesta del modelo");
            String response = chatClientResponse.chatResponse().getResult().getOutput().getText();
            
            log.info("Informacion recuperada de los documentos: ");
            chatClientResponse.context().forEach((key, value) -> {
                System.out.println(key);
                if(key.equals("qa_retrieved_documents")) {
                	List<Document> docs = (ArrayList<Document>)value;
                	docs.forEach(d -> {
                		log.info(d.getMetadata().toString());
                	});
                }
            });
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("ERROR:  " + e.getMessage());
        }
    }
	
	
}
