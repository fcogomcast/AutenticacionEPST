package es.tributasenasturias.services.autenticacion.mediacion;


import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import es.tributasenasturias.services.autenticacion.ElementoType;
import es.tributasenasturias.services.autenticacion.InformacionCertificadoType;
import es.tributasenasturias.services.autenticacion.ResultadoType;
import es.tributasenasturias.services.autenticacion.TiposAdmitidosType;
import es.tributasenasturias.services.autenticacion.constantes.Errores;
import es.tributasenasturias.services.autenticacion.exceptions.AutenticacionException;
import es.tributasenasturias.services.autenticacion.plataformas.ValidadorCertificado.ResultadoValidacion;
import es.tributasenasturias.services.autenticacion.tipos.TiposCertificadoAdmitidos;
import es.tributasenasturias.services.autenticacion.tipos.TiposCertificadoAdmitidos.TipoCertificado;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMDocumentException;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMUtils;

/**
 * Mediador para realizar las transformaciones necesarias de datos de resultado de 
 * plataformas de validación a resultados de servicio. 
 * @author crubencvs
 *
 */
public class AdaptadorResultadoAFirma {

	private ResultadoValidacion resultado;
	private String tipoMaximoAdmitido;
	private List<Integer> tiposAdmitidos;
	private String ficheroTiposAdmitidos;
	/**
	 * Constructor
	 * @param resValidacion Tipo de resultado de validación, para devolver los resultados.
	 * @param tiposAdmitidos Tipos admitidos de certificado
	 * @param ficheroTiposAdmitidos Nombre de fichero que contendrá la lista de tipos de certificado, y si
	 * están admitidos o no.
	 */
	public AdaptadorResultadoAFirma(ResultadoValidacion resValidacion, TiposAdmitidosType tiposAdmitidos, String ficheroTipos)
	{
		this.resultado = resValidacion;
		this.tiposAdmitidos = tiposAdmitidos.getTipoAdmitido();
		this.ficheroTiposAdmitidos= ficheroTipos;
	}
	
	private static class ResultadoTraducido
	{
		private String codigoResultado;
		private String descResultado;
		public String getCodigoResultado() {
			return codigoResultado;
		}
		public void setCodigoResultado(String codigoResultado) {
			this.codigoResultado = codigoResultado;
		}
		public String getDescResultado() {
			return descResultado;
		}
		public void setDescResultado(String descResultado) {
			this.descResultado = descResultado;
		}
		
		
	}
	/**
	 * Devuelve si el tipo de certificado es uno de los admitidos en la llamada al servicio web.
	 * Modificaciones realizadas:
	 * Se recuperará una lista de certificados admitidos y en base a ella se comprobará si
	 * el tipo está admitido.
	 * Si se ha indicado un valor para tipos admitidos, se comprobará si el fichero
	 * contiene ese tipo y está activado. 
	 * Si no se ha indicado, se comprobará si el tipo recibido está en el fichero, y admitido.
	 * @return
	 */
	private boolean esTipoAdmitido() throws AutenticacionException
	{
		String tipoCertificado=this.resultado.getClasificacion();
		//Si no se ha indicado un tipo máximo admitido en los parámetros del servicio, nos fijamos solamente
		//en la validación según el fichero de tipos admitidos.
		//Es int, no podemos en este momento.
		try
		{
			boolean admitido =  verificarTipoFichero(ficheroTiposAdmitidos,Integer.valueOf(tipoCertificado));
			
			if (tiposAdmitidos==null||  tiposAdmitidos.size()==0 || !admitido)
			{
				return admitido;
			}
			if (tiposAdmitidos.contains(Integer.valueOf(tipoCertificado)))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (NumberFormatException e)
		{
			//No hemos podido convertir a entero, esto es un error.
			throw new AutenticacionException("No se han podido interpretar como números el tipo máximo admitido:"+ tipoMaximoAdmitido + " o la clasificación recibida:"+ tipoCertificado);
		}
	}
	/**
	 * Realiza la traducción de un código de resultado de la plataforma de validación a un resultado interno.
	 * @param codMensajePasarela Código de mensaje de la pasarela de validación.
	 * @return
	 * @throws XMLDOMDocumentException
	 */
	private ResultadoTraducido getResultadoTraducido(String codMensajePasarela) throws XMLDOMDocumentException 
	{
		Document mensajes= XMLDOMUtils.parseXML(AdaptadorResultadoAFirma.class.getResourceAsStream("/xml/mensajes_afirma.xml"));
		Node mensaje = XMLDOMUtils.selectSingleNode(mensajes, "//mapa_mensaje[remoto/text()='"+codMensajePasarela+"']");
		ResultadoTraducido res = new ResultadoTraducido();
		String error = XMLDOMUtils.selectSingleNode(mensaje, "epst/text()").getNodeValue();
		String desc = XMLDOMUtils.selectSingleNode(mensaje, "epst_texto/text()").getNodeValue();
		res.setCodigoResultado(error);
		res.setDescResultado(desc);
		return res;
	}
	/**
	 * Transforma el resultado de la validación en un resultado de servicio.
	 * Pueden darse dos situaciones.
	 * - Excepción devuelta por el servicio remoto. En este caso, se recuperan los datos de excepción y se imprime su traducción a errores de nuestro servicio.
	 * - Terminación completa, independientemente de que se haya validado correctamente o no. En este caso, también se imprimirá el resultado con traducción.
	 * @return
	 */
	public ResultadoType getResultadoValidacion () throws AutenticacionException
	{
		ResultadoType rt = new ResultadoType();
		try{
			if (resultado.isExcepcion())
			{
				rt.setCodResultado(String.valueOf(Errores.GENERICO));
				rt.setDescResultado("Error en la validación de certificado. Contacte con el soporte del servicio");
				rt.setEsValido(false);
			}
			else
			{
				//Comprobar si el tipo es de los admitidos
				if (!esTipoAdmitido())
				{
					throw new AutenticacionException (Errores.TIPO_NO_ADMITIDO);
				}
				//Comprobamos si el certificado es español. 
				if (resultado.getPais()!=null && !"".equals(resultado.getPais()) && !"ES".equalsIgnoreCase(resultado.getPais()))
				{
					throw new AutenticacionException(Errores.PAIS_EXTRANJERO);
				}
				ResultadoTraducido resTrad = getResultadoTraducido(resultado.getCodResultado());
				rt.setCodResultado(resTrad.getCodigoResultado());
				rt.setDescResultado(resTrad.getDescResultado());
				rt.setEsValido(!resultado.isError());
			}
			return rt;
		}
		catch (XMLDOMDocumentException e)
		{
			throw new AutenticacionException ("Error al tratar el resultado de la validación de plataforma:"+ e.getMessage(),e);
		}
	}
	
	/**
	 * Crea un nuevo elemento {@link ElementoType} y lo añade a un objeto de tipo {@link InformacionCertificadoType}
	 * @param identificador Identificador del elemento en InformacionCertificadoType
	 * @param valor Valor
	 * @param inf Elemento {@link InformacionCertificadoType}
	 * @return un {@link InformacionCertificadoType} con el elemento añadido.
	 */
	private InformacionCertificadoType setInfoElement (String identificador, String valor, InformacionCertificadoType inf)
	{
		ElementoType el= new ElementoType();
		if (valor!=null && !"".equals(valor))
		{
			el.setIdentificador(identificador);
			el.setValor(valor);
			inf.getElemento().add(el);
		}
		return inf;
	}
	public InformacionCertificadoType getInformacionCertificado() throws AutenticacionException
	{
		InformacionCertificadoType info= new InformacionCertificadoType();
		if (!resultado.isExcepcion())
		{
			String clasificacion = resultado.getClasificacion();
			//NIF
			setInfoElement ("nif",resultado.getNif(),info);
			//Nombre
			setInfoElement ("nombre",resultado.getNombre(),info);
			//Apellido1
			setInfoElement ("apellido1",resultado.getApellido1(),info);
			//Apellido2
			setInfoElement ("apellido2",resultado.getApellido2(),info);
			//Email
			setInfoElement ("email",resultado.getEmail(),info);
			//Fecha de nacimiento
			setInfoElement ("fechaNacimiento",resultado.getFechaNacimiento(),info);
			//CIF
			setInfoElement ("cif",resultado.getCif(),info);
			//Razón social
			setInfoElement ("razonSocial",resultado.getRazonSocial(),info);
			
			//Comunes
			setInfoElement ("numeroSerie",resultado.getNumeroSerie(),info);
			//Subject
			setInfoElement ("subject",resultado.getSubject(),info);
			//País
			setInfoElement("pais",resultado.getPais(),info);
			//Clasificación
			setInfoElement("clasificacion",resultado.getClasificacion(),info);
			if (ResultadoValidacion.SELLO_ELECTRONICO.equals(clasificacion) ||
					ResultadoValidacion.CUALIFICADO_SELLO.equals(clasificacion)||
					ResultadoValidacion.SEDE.equals (clasificacion))
			{
				//Añadimos nif entidad suscriptora y organización.
				setInfoElement ("organizacion",resultado.getOrganizacion(),info);
				setInfoElement ("NIFEntidadSuscriptora",resultado.getNifEntidadSuscriptora(),info);
			}
			//Organization Identifier Europeo
			setInfoElement("OIEuropeo",resultado.getOIEuropeo(), info);
			
			
		}
		return info;
	}
	/**
	 * Verifica que el tipo de certificado que se pasa está presente en el fichero de certificados admitidos,
	 * y que se indica que se acepta
	 * @param ficheroTiposAdmitidos nombre del fichero de certificados admitidos
	 * @param clasificacion Valor de la clasificación de certificado de @firma
	 * 		  0 = Certificado físico - certificado cualificado de firma
	 *        1 = Jurídico (no cualificado)
	 *        2 = Componentes (no cualificado)
	 *        3= Sede
	 *        4 = Sello electrónico
	 *        5 = Empleado público
	 *        6 = Entidad sin personalidad Juridica (no cualificado)
	 *        7 = Empleado público con seudónimo
	 *        8 = Cualificado de sello (UE 910/2014)
	 *        9 = Cualificado de autenticación de sello web (UE 910/2014)
	 *        10= Cualificado de Sello de tiempo
	 *        11= Persona física representante ante las AAPP de persona jurídica
	 *        12= Persona física representante ante las AAPP de entidad sin persona jurídica
	 *        
	 * @return true si está admitido, false si no
	 * @throws AutenticacionException 
	 */
	public boolean verificarTipoFichero(String ficheroTiposAdmitidos, int clasificacion) throws AutenticacionException
	{
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(TiposCertificadoAdmitidos.class.getPackage().getName());
			TiposCertificadoAdmitidos tipos = (TiposCertificadoAdmitidos) context.createUnmarshaller().unmarshal(new File(ficheroTiposAdmitidos));
			boolean admitido=false;
			if (tipos==null)
			{
				return false;
			}
			String strClas = String.valueOf (clasificacion);
			for (TipoCertificado t:tipos.getTipoCertificado())
			{
				if (strClas.equalsIgnoreCase(t.getClasificacion()) && "S".equalsIgnoreCase(t.getAdmitido()))
				{
					admitido=true;
					break;
				}
			}
			return admitido;
		} catch (JAXBException e) {
			if (e.getMessage()!=null)
			{
				throw new AutenticacionException ("Error al tratar el resultado de la validación de plataforma. No se puede comprobar si el tipo de certificado está admitido:"+ e.getMessage(),e);
			}
			else
			{
				throw new AutenticacionException ("Error al tratar el resultado de la validación de plataforma. No se puede comprobar si el tipo de certificado está admitido:"+ e.getCause().getMessage(),e);
			}
		}
	}
}
