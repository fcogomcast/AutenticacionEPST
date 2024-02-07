package es.tributasenasturias.services.autenticacion.exceptions;

public class PreferenciasException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4508598102078577160L;

	/**
	 * 
	 */
	public PreferenciasException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PreferenciasException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public PreferenciasException(String message) {
		super(message);
	}
}
