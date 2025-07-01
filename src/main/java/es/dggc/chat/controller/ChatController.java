package es.dggc.chat.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	@GetMapping("/ai/generate")
    public ResponseEntity<String> generate(@RequestParam(value = "message") String message) {
        try {
        	
        	log.info("Pregunta recibida: {}", message);
        	

        	//Contiene el ChatResponse anterior y el contexto de la ejecucion del ChatClient
        	//La documentacion indica que es interesante en RAG por ejemplo para los documentos relevantes recuperados	
        	ChatClientResponse response = chatClient.prompt()
        			.advisors(
        			        //MessageChatMemoryAdvisor.builder(chatMemory).build(), //Chatmemory
        			        QuestionAnswerAdvisor.builder(vectorStore).build() 	  //RAG
        			    )
        		    .user(message)
        		    .call()
        		    .chatClientResponse();
        	
            log.info("Se obtiene respuesta del modelo");
            
            log.info("Se envía la respuesta");
            
            return ResponseEntity.ok(response.chatResponse().getResult().getOutput().getText());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("ERROR:  " + e.getMessage());
        }
    }
	
	
}
