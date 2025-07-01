package es.dggc.chat.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

public class Util {
	
	public static List<Document> chunckContent(String content, String filename) {
		   int maxTokens = 100;
		   int maxChars = maxTokens * 4; // 

		    List<Document> partes = new ArrayList();
		    int inicio = 0;
		    while (inicio < content.length()) {
		        int fin = Math.min(inicio + maxChars, content.length());
		        String fragmento = content.substring(inicio, fin);
		        Document doc = new Document(fragmento, Map.of("source", filename));
		        partes.add(doc);
		        inicio = fin;
		    }

		    return partes;
	}
	
	public static String extractContent(MultipartFile document) {
		String contenido = null;
		try (PDDocument pdf = PDDocument.load(document.getInputStream())) {
	        PDFTextStripper stripper = new PDFTextStripper();
	        contenido =  stripper.getText(pdf);
	    } catch (IOException e) {
			e.printStackTrace();
		}

		return contenido;
	}

}
