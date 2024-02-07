package es.tributasenasturias.services.autenticacion.plataformas;

/** 
 * Clase abstracta de la que heredar�n todas las implementaciones de plataforma de validaci�n,
 * que realizar�n las operaciones necesarias. En el momento de crear la clase s�lo realizar�n
 * la validaci�n de certificado.
 * @author crubencvs
 *
 */
public abstract class PlataformaValidacion implements ValidadorCertificado{

	
	public abstract String getNombrePasarela();
}
