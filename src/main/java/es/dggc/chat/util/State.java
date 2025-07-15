package es.dggc.chat.util;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO para el estado de la respuesta del modelo al cliente
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum State {

	OK("0000", "La consulta se ha realizado correctamente."),
    CAMPOS_OBLIGATORIOS("E001", "Faltan campos obligatorios en la petición"),
	GENERIC_ERROR("E999", "Se ha producido un error durante la consulta." );
	
	private final String code;
	private final String message;
	
	@Override
    public String toString() {
        return String.format("[%s] %s", code, message);
    }
	
}
