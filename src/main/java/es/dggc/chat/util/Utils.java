package es.dggc.chat.util;

import org.springframework.web.multipart.MultipartFile;

public final class Utils {

	/**
	 * Validacion de un documento 
	 * @param file
	 * @return
	 */
	public static State validateDocument(MultipartFile file) {
		State state = State.OK;
		
		if (file == null || file.isEmpty()) {
			state = State.DOCUMENT_REQUIRED;
		}

		if (!file.getContentType().equals(Constants.CONTENT_TYPE_PDF)) {
			state = State.INVALID_DOCUMENT;
		}
		
		return state;
	}
}
