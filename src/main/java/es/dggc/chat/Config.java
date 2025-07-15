package es.dggc.chat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.dggc.chat.util.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        String systemPrompt=null;
   	 
	   	 try {
	   		systemPrompt = Files.readString(Paths.get(Constants.PATH_SYSTEM_PROMPT));
	   	 } catch (IOException e) {
	   		log.error("Se ha producido un error al recuperar el prompt del sistema");
	   		e.printStackTrace();
	   	 }
	
	      return builder
	              .defaultSystem(systemPrompt)
	              .build();
    }
   

}