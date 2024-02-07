package es.tributasenasturias.services.autenticacion.plataformas;

import es.tributasenasturias.services.autenticacion.exceptions.SystemException;
import es.tributasenasturias.services.autenticacion.mediacion.AdaptadorAFirma;
import es.tributasenasturias.services.autenticacion.preferencias.Preferencias;

public class AFirma extends PlataformaValidacion{

	private Preferencias pref;
	private String idLlamada;
	public AFirma(Preferencias pref, String idLlamada)
	{
		this.pref= pref;
		this.idLlamada= idLlamada;
	}
	@Override
	public ResultadoValidacion validarCertificado(byte[] certificado) throws SystemException{
		ResultadoValidacion va;
		AdaptadorAFirma adp = new AdaptadorAFirma(this.idLlamada);
		va= adp.callValidarCertificado(certificado, pref);
		return va;
	}
	@Override
	public String getNombrePasarela() {
		return "@Firma";
	}
	
	

}
