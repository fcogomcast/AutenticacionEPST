package es.tributasenasturias.services.autenticacion.plataformas;

import java.util.HashMap;
import java.util.Map;

import es.tributasenasturias.services.autenticacion.exceptions.SystemException;


public interface ValidadorCertificado {

	/**
	 * Enumeración con los diferentes campos de información que se recuperarán del certificado.
	 * <p/>
	 * @author crubencvs
	 *
	 */
	public static enum CamposCertificado
	{
		NIF_PERSONA,
		NOMBRE,
		APELLIDO1,
		APELLIDO2,
		EMAIL,
		FECHA_NACIMIENTO,
		CIF,
		RAZON_SOCIAL,
		CLASIFICACION,
		NUMERO_SERIE,
		SUBJECT,
		ID_EMISOR,
		NIF_ENTIDAD_SUSCRIPTORA,
		ORGANIZACION,
		PAIS,
		OIEUROPEO
	}
	/**
	 * Resultado de la validación. Incluirá la información de certificado y un resumen del resultado
	 * de la misma.
	 * 
	 * Se podría tener esta clase sin métodos de acceso, y crear una clase que 
	 * implemente el patrón <b>Decorador</b>, y que serían las que utilizasen las clases
	 * que accediesen a ella.
	 * @author crubencvs
	 *
	 */
	public static class ResultadoValidacion
	{
		
		/**
		 * Constante que indica que el certificado es de Sede
		 */
		public static final String SEDE="3";
		/**
		 * Constante que indica que el certificado era de Sello electronico
		 */
		public static final String SELLO_ELECTRONICO="4";
		/**
		 * Constante que indica certificado cualificado de sello (UE 910/2014) 
		 */
		public static final String CUALIFICADO_SELLO="8";
		/**
		 * Constante que indica certificado de autenticación de sitio web (UE 910/2014)
		 */
		public static final String CUALIFICADO_AUTENTICACION_SITIO="9";
		
		private Map<CamposCertificado,String> infoCertificado=new HashMap<CamposCertificado,String>();
		private boolean error; 
		private String codResultado;
		private String desResultado;
		private boolean excepcion;
		private String codError;
		private String desError;
		private String plataformaValidacion;
		private String modoValidacion;
		
		
		public Map<CamposCertificado, String> getInfoCertificado() {
			return infoCertificado;
		}
		public void setInfoCertificado(Map<CamposCertificado, String> infoCertificado) {
			this.infoCertificado = infoCertificado;
		}
		public String getCodResultado() {
			return codResultado;
		}
		public void setCodResultado(String codResultado) {
			this.codResultado = codResultado;
		}
		public String getDesResultado() {
			return desResultado;
		}
		public void setDesResultado(String desResultado) {
			this.desResultado = desResultado;
		}
		public boolean isError() {
			return error;
		}
		public void setError(boolean error) {
			this.error = error;
		}
		public boolean isExcepcion() {
			return excepcion;
		}
		public void setExcepcion(boolean excepcion) {
			this.excepcion = excepcion;
		}
		public String getCodError() {
			return codError;
		}
		public void setCodError(String codError) {
			this.codError = codError;
		}
		public String getDesError() {
			return desError;
		}
		public void setDesError(String desError) {
			this.desError = desError;
		}
		public String getPlataformaValidacion() {
			return plataformaValidacion;
		}
		public void setPlataformaValidacion(String plataformaValidacion) {
			this.plataformaValidacion = plataformaValidacion;
		}
		
		public String getModoValidacion() {
			return modoValidacion;
		}
		public void setModoValidacion(String modoValidacion) {
			this.modoValidacion = modoValidacion;
		}
		//** A partir de aquí, métodos de ayuda para recuperar la información por nombre.
		/**
		 * Recupera el NIF de la persona responsable del certificado
		 */
		public String getNif()
		{
			String nif="";
			if (infoCertificado.containsKey(CamposCertificado.NIF_PERSONA))
			{
				nif = infoCertificado.get(CamposCertificado.NIF_PERSONA);
			}
			return nif;
		}
		
		/**
		 * Recupera el CIF de la persona responsable del certificado
		 */
		public String getCif()
		{
			String cif="";
			if (infoCertificado.containsKey(CamposCertificado.CIF))
			{
				cif = infoCertificado.get(CamposCertificado.CIF);
			}
			return cif;
		}
		/**
		 * Recupera el nombre de la persona responsable del certificado
		 */
		public String getNombre()
		{
			String nombre="";
			if (infoCertificado.containsKey(CamposCertificado.NOMBRE))
			{
				nombre = infoCertificado.get(CamposCertificado.NOMBRE);
			}
			return nombre;
		}
		
		/**
		 * Recupera el primer apellido de la persona responsable del certificado
		 */
		public String getApellido1()
		{
			String apellido="";
			if (infoCertificado.containsKey(CamposCertificado.APELLIDO1))
			{
				apellido = infoCertificado.get(CamposCertificado.APELLIDO1);
			}
			return apellido;
		}
		
		/**
		 * Recupera el segundo apellido de la persona responsable del certificado
		 */
		public String getApellido2()
		{
			String apellido2="";
			if (infoCertificado.containsKey(CamposCertificado.APELLIDO2))
			{
				apellido2 = infoCertificado.get(CamposCertificado.APELLIDO2);
			}
			return apellido2;
		}
		
		/**
		 * Recupera el EMAIL de la persona responsable del certificado
		 */
		public String getEmail()
		{
			String email="";
			if (infoCertificado.containsKey(CamposCertificado.EMAIL))
			{
				email = infoCertificado.get(CamposCertificado.EMAIL);
			}
			return email;
		}
		
		/**
		 * Recupera la fecha de nacimiento de la persona responsable del certificado
		 */
		public String getFechaNacimiento()
		{
			String fechaNacimiento="";
			if (infoCertificado.containsKey(CamposCertificado.FECHA_NACIMIENTO))
			{
				fechaNacimiento = infoCertificado.get(CamposCertificado.FECHA_NACIMIENTO);
			}
			return fechaNacimiento;
		}
		
		/**
		 * Recupera la razón social de la persona jurídica responsable del certificado
		 */
		public String getRazonSocial()
		{
			String razonSocial="";
			if (infoCertificado.containsKey(CamposCertificado.RAZON_SOCIAL))
			{
				razonSocial = infoCertificado.get(CamposCertificado.RAZON_SOCIAL);
			}
			return razonSocial;
		}
		
		/**
		 * Recupera la clasificación del certificado
		 */
		public String getClasificacion()
		{
			String clasificacion="";
			if (infoCertificado.containsKey(CamposCertificado.CLASIFICACION))
			{
				clasificacion = infoCertificado.get(CamposCertificado.CLASIFICACION);
			}
			return clasificacion;
		}

		/**
		 * Recupera el número de serie del certificado
		 */
		public String getNumeroSerie()
		{
			String numeroSerie="";
			if (infoCertificado.containsKey(CamposCertificado.NUMERO_SERIE))
			{
				numeroSerie = infoCertificado.get(CamposCertificado.NUMERO_SERIE);
			}
			return numeroSerie;
		}

		/**
		 * Recupera el Subject del certificado
		 */
		public String getSubject()
		{
			String subject="";
			if (infoCertificado.containsKey(CamposCertificado.SUBJECT))
			{
				subject = infoCertificado.get(CamposCertificado.SUBJECT);
			}
			return subject;
		}
		
		/**
		 * Recupera el identificador de emisor del certificado
		 */
		public String getIdEmisor()
		{
			String idEmisor="";
			if (infoCertificado.containsKey(CamposCertificado.ID_EMISOR))
			{
				idEmisor = infoCertificado.get(CamposCertificado.ID_EMISOR);
			}
			return idEmisor;
		}
		/**
		 * Recupera el NIF de la entidad suscriptora para sellos electrónicos
		 */
		public String getNifEntidadSuscriptora()
		{
			String nif="";
			if (infoCertificado.containsKey(CamposCertificado.NIF_ENTIDAD_SUSCRIPTORA))
			{
				nif = infoCertificado.get(CamposCertificado.NIF_ENTIDAD_SUSCRIPTORA);
			}
			return nif;
		}
		/**
		 * Recupera la organización asociada a sello electrónico
		 */
		public String getOrganizacion()
		{
			String org="";
			if (infoCertificado.containsKey(CamposCertificado.ORGANIZACION))
			{
				org = infoCertificado.get(CamposCertificado.ORGANIZACION);
			}
			return org;
		}
		/**
		 * Recupera el país asociado al certificado
		 */
		public String getPais()
		{
			String pais="";
			if (infoCertificado.containsKey(CamposCertificado.PAIS))
			{
				pais = infoCertificado.get(CamposCertificado.PAIS);
			}
			return pais;
		}
		
		/**
		 * Recupera el Organization Identifier asociado al certificado
		 * En este puede venir el identificador fiscal en algunos certificados, como los cualificados 
		 * de sello de entidad
		 */
		public String getOIEuropeo()
		{
			String oi="";
			if (infoCertificado.containsKey(CamposCertificado.OIEUROPEO))
			{
				oi = infoCertificado.get(CamposCertificado.OIEUROPEO);
			}
			return oi;
		}
		// Setters

		/**
		 *  Asignar nif
		 */
		public void setNif(String nif)
		{
			infoCertificado.put(CamposCertificado.NIF_PERSONA, nif);
		}
		/**
		 * Asignar nombre
		 * @param nombre
		 */
		public void setNombre(String nombre)
		{
			infoCertificado.put(CamposCertificado.NOMBRE, nombre);
		}
		/**
		 * Asignar apellido1
		 * @param apellido1
		 */
		public void setApellido1(String apellido1)
		{
			infoCertificado.put(CamposCertificado.APELLIDO1, apellido1);
		}
		/**
		 * Asignar apellido2
		 * @param apellido2
		 */
		public void setApellido2(String apellido2)
		{
			infoCertificado.put(CamposCertificado.APELLIDO2, apellido2);
		}
		/**
		 * Asignar email
		 * @param email
		 */
		public void setEmail(String email)
		{
			infoCertificado.put(CamposCertificado.EMAIL, email);
		}
		/**
		 * Asignar fecha de nacimiento
		 * @param fechaNacimiento
		 */
		public void setFechaNacimiento(String fechaNacimiento)
		{
			infoCertificado.put(CamposCertificado.FECHA_NACIMIENTO, fechaNacimiento);
		}
		/**
		 * Asignar cif
		 * @param cif
		 */
		public void setCif (String cif)
		{
			infoCertificado.put(CamposCertificado.CIF, cif);
		}
		/**
		 * Asignar razón social
		 * @param razonSocial
		 */
		public void setRazonSocial(String razonSocial)
		{
			infoCertificado.put(CamposCertificado.RAZON_SOCIAL, razonSocial);
		}
		/**
		 * Asignar número de serie
		 * @param numeroSerie
		 */
		public void setNumeroSerie(String numeroSerie)
		{
			infoCertificado.put(CamposCertificado.NUMERO_SERIE, numeroSerie);
		}
		/**
		 * Asignar subject
		 * @param subject
		 */
		public void setSubject (String subject)
		{
			infoCertificado.put(CamposCertificado.SUBJECT, subject);
		}
		
		/**
		 * inserta la clasificación.
		 * Por ahora sólo entiende las clasificaciones de \@Firma, 
		 * si en el futuro hubiera que incluir clasificaciones de otras fuentes,
		 * podría sacarse todo este código de getters y setters a una 
		 * clase que actuase como decorador, y tener uno por cada fuente.
		 * @param clasificacion La clasificación, según los códigos de \@Firma
		 */
		public void setClasificacion(String clasificacion)
		{
			infoCertificado.put(CamposCertificado.CLASIFICACION, clasificacion);
		}
		/**
		 * Asignar nif entidad suscriptora
		 * @param nif entidad suscriptora
		 */
		public void setNifEntidadSuscriptora (String nif)
		{
			infoCertificado.put(CamposCertificado.NIF_ENTIDAD_SUSCRIPTORA, nif);
		}
		/**
		 * Asignar organización
		 * @param organizacion
		 */
		public void setOrganizacion (String organizacion)
		{
			infoCertificado.put(CamposCertificado.ORGANIZACION, organizacion);
		}
		/**
		 * 
		 * @param idEmisor
		 */
		public void setIdEmisor( String idEmisor)
		{
			infoCertificado.put(CamposCertificado.ID_EMISOR, idEmisor);
		}
		
		/**
		 * Asignar país
		 * @param pais
		 */
		public void setPais(String pais)
		{
			infoCertificado.put(CamposCertificado.PAIS, pais);
		}
		
		public void setOIEuropeo(String oi)
		{
			infoCertificado.put(CamposCertificado.OIEUROPEO, oi);
		}
	}
	/**
	 * Valida un certificado y devuelve tanto el estado de validación como la información de certificado.
	 * @param certificado Certificado a validar. Formato binario
	 * @return
	 */
	public ResultadoValidacion validarCertificado(byte[] certificado) throws SystemException;
}
