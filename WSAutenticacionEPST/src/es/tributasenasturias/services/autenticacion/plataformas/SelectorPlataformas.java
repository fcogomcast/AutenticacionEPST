package es.tributasenasturias.services.autenticacion.plataformas;

import es.tributasenasturias.services.autenticacion.preferencias.Preferencias;

/**
 * Selecciona la plataforma de validación a utilizar. Esta plataforma permitirá validar lo que se precise, <br/>
 * principio el certificado.
 * <p/>
 * @author crubencvs
 *
 */
public class SelectorPlataformas {

	/**
	 * Devuelve un objeto para usar una plataforma de validación.
	 * La plataforma se podrá escoger en función de las preferencias del servicio.
	 * En este momento, se devuelve siempre la de \@Firma
	 * @param pref Preferencias del servicio
	 * @param idLlamada Identificador de llamada al servicio web.
	 * @return
	 */
	public PlataformaValidacion getPlataforma (Preferencias pref, String idLlamada)
	{
		return new AFirma(pref, idLlamada);
	}
	
}
