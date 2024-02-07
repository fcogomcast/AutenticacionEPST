package es.tributasenasturias.services.autenticacion.exceptions;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Clase para excepciones de dominio, que podrán mostrarse al cliente del servicio web.
 * @author crubencvs
 *
 */
public class AutenticacionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ResourceBundle resources;
	static {
		try {
			resources = ResourceBundle
					.getBundle("es.tributasenasturias.services.autenticacion.resources.errores");
		} catch (MissingResourceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private int errorCode;

	/**
	 * Constructor. <p/>
	 * 
	 * @param errorCode
	 * @param msgId
	 * @param args
	 * @param exception
	 */
	public AutenticacionException(int errorCode, String msgId, Object[] args,
			Throwable exception) {
		super(getMessage(errorCode, msgId, args), exception);
		this.errorCode = errorCode;
	}

	/**
	 * Constructor. <p/>
	 * 
	 * @param errorCode
	 * @param msgId
	 * @param args
	 */
	public AutenticacionException(int errorCode, String msgId, Object[] args) {
		super(getMessage(errorCode, msgId, args));
		this.errorCode = errorCode;
	}

	/**
	 * Constructor. <p/>
	 * 
	 * @param errorCode
	 * @param msgId
	 */
	public AutenticacionException(int errorCode, String msgId) {
		this(errorCode, msgId, null);
	}

	/**
	 * Constructor. <p/>
	 * 
	 * @param errorCode
	 */
	public AutenticacionException(int errorCode) {
		this(errorCode, null, null);
	}

	/**
	 * Constructor. <p/>
	 * 
	 * @param errorMessage
	 */
	public AutenticacionException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructor. <p/>
	 * 
	 * @param errorMessage
	 */
	public AutenticacionException(String errorMessage, Throwable t) {
		super(errorMessage, t);
	}

	/**
	 * Recupea el código de error <p/>
	 * 
	 * @return código de error de esta excepción
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Recupera el mensaje de las propiedades indicadas. <p/>
	 * 
	 * @param errorCode
	 * @param msgId
	 * @param args
	 * @return el mensaje de error.
	 */
	private static String getMessage(int errorCode, String msgId, Object[] args) {
		String msg = null;
		try {
			msg = resources.getString(String.valueOf(errorCode));
			if (msgId != null) {
				msg += " ("
					+ MessageFormat
							.format(resources.getString(msgId), args) + ")";
				return msg;
			}
		} catch (MissingResourceException e) {
			throw new RuntimeException("El identificador '" + msgId
					+ "' no se encuentra en el fichero de mensajes", e);
		}
		return msg;
	}

}
