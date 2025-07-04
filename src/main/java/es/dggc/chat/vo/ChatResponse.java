package es.dggc.chat.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
	

	private String userId;
	private String response;
	private LocalDateTime time;
	private List<String> documents;
	private State state;
	
}
