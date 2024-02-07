package es.tributasenasturias.services.autenticacion;

import javax.xml.ws.Holder;

import es.tributasenasturias.services.autenticacion.exceptions.AutenticacionException;
import es.tributasenasturias.services.autenticacion.exceptions.SystemException;
import es.tributasenasturias.services.autenticacion.log.Logger;
import es.tributasenasturias.services.autenticacion.mediacion.AdaptadorResultadoAFirma;
import es.tributasenasturias.services.autenticacion.plataformas.PlataformaValidacion;
import es.tributasenasturias.services.autenticacion.plataformas.SelectorPlataformas;
import es.tributasenasturias.services.autenticacion.plataformas.ValidadorCertificado.ResultadoValidacion;
import es.tributasenasturias.services.autenticacion.preferencias.Preferencias;

public class AutenticacionEPST {

	private String idLlamada;

	public AutenticacionEPST(String idLlamada) {
		this.idLlamada = idLlamada;
	}
	/**
	 * Realiza la validación de un certificado
	 * @param certificado Contenido binario del certificado.
	 * @param tiposAdmitidos Tipos admitidos de certificado en esta llamada
	 * @param informacionCertificado Devuelve la información del certificado.
	 * @param resultado Devuelve el resultado de la llamada.
	 */
	public void validacionCertificado(byte[] certificado, TiposAdmitidosType tiposAdmitidos,
			Holder<InformacionCertificadoType> informacionCertificado,
			Holder<ResultadoType> resultado) {
		Logger log = new Logger(idLlamada);
		try {
			log.debug("===Inicio llamada con id:"+idLlamada);
			SelectorPlataformas selector = new SelectorPlataformas();
			Preferencias pref=Preferencias.getPreferencias();
			PlataformaValidacion pla = selector.getPlataforma(
					pref, this.idLlamada);
			log.debug("Plataforma de validación seleccionada:" + pla.getNombrePasarela()+". Se procede a validar el certificado.");
			ResultadoValidacion res = pla.validarCertificado(certificado);
			log.debug("Llamada a validación de certificado completa. Se procesa el resultado.");
			AdaptadorResultadoAFirma adt = new AdaptadorResultadoAFirma(res,
					tiposAdmitidos, pref.getNombreFicheroTiposAdmitidos());
			resultado.value = adt.getResultadoValidacion();
			informacionCertificado.value = adt.getInformacionCertificado();
			log.debug("Resultado procesado. Se termina con resultado:"+resultado.value.getCodResultado());
		} catch (AutenticacionException e) {
			// Errores de dominio, se muestran tal cual al cliente
			ResultadoType rt = new ResultadoType();
			rt.setCodResultado(String.valueOf(e.getErrorCode()));
			rt.setDescResultado(e.getMessage());
			resultado.value = rt;

		} catch (SystemException e) {
			// Errores técnicos, se enmascaran en uno de dominio, escribiendo
			// primero en log.
			if (log != null) {
				log.error("Error técnico producido:" + e.getMessage(), e);
			}
			ResultadoType rt = new ResultadoType();
			rt.setCodResultado("9999");
			rt.setDescResultado("Error producido durante la validación.");
			resultado.value = rt;

		} catch (Exception e) {
			// Igual que los técnicos. Nada sale del servicio sin estar
			// controlado, a ser posible.
			if (log != null) {
				log.error("Error en servicio:" + e.getMessage(), e);
			}
			ResultadoType rt = new ResultadoType();
			rt.setCodResultado("9999");
			rt.setDescResultado("Error producido durante la validación.");
			resultado.value = rt;
		}
		log.debug("===Fin llamada con id:"+idLlamada);

	}
}
