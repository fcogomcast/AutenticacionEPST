package es.tributasenasturias.services.autenticacion.exceptions;

/**
 * Clase para excepciones internas, de tipo técnico.
 * @author crubencvs
 *
 */
public class SystemException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4047660014995300631L;
	/**
	 * 
	 */
	public SystemException() {
		super();
	}
	/**
	 * Constructor
	 * @param message
	 * @param cause
	 */
	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * Constructor
	 * @param message
	 */
	public SystemException(String message) {
		super(message);
	}

}
