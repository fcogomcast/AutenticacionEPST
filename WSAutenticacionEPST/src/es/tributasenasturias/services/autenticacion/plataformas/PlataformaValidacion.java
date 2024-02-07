package es.tributasenasturias.services.autenticacion.plataformas;

/** 
 * Clase abstracta de la que heredarán todas las implementaciones de plataforma de validación,
 * que realizarán las operaciones necesarias. En el momento de crear la clase sólo realizarán
 * la validación de certificado.
 * @author crubencvs
 *
 */
public abstract class PlataformaValidacion implements ValidadorCertificado{

	
	public abstract String getNombrePasarela();
}
