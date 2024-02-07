/**
 * 
 */
package es.tributasenasturias.services.autenticacion.xml;

/**
 * @author crubencvs
 *
 */
public class XMLDOMDocumentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 520464406390387410L;
	
	public XMLDOMDocumentException(String error, Throwable original) {
		super(error, original);
	}

	public XMLDOMDocumentException(String error) {
		super(error);
	}

}
