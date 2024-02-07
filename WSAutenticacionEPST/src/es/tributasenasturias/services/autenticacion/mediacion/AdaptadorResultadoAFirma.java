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
 * plataformas de validaci�n a resultados de servicio. 
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
	 * @param resValidacion Tipo de resultado de validaci�n, para devolver los resultados.
	 * @param tiposAdmitidos Tipos admitidos de certificado
	 * @param ficheroTiposAdmitidos Nombre de fichero que contendr� la lista de tipos de certificado, y si
	 * est�n admitidos o no.
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
	 * Se recuperar� una lista de certificados admitidos y en base a ella se comprobar� si
	 * el tipo est� admitido.
	 * Si se ha indicado un valor para tipos admitidos, se comprobar� si el fichero
	 * contiene ese tipo y est� activado. 
	 * Si no se ha indicado, se comprobar� si el tipo recibido est� en el fichero, y admitido.
	 * @return
	 */
	private boolean esTipoAdmitido() throws AutenticacionException
	{
		String tipoCertificado=this.resultado.getClasificacion();
		//Si no se ha indicado un tipo m�ximo admitido en los par�metros del servicio, nos fijamos solamente
		//en la validaci�n seg�n el fichero de tipos admitidos.
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
			throw new AutenticacionException("No se han podido interpretar como n�meros el tipo m�ximo admitido:"+ tipoMaximoAdmitido + " o la clasificaci�n recibida:"+ tipoCertificado);
		}
	}
	/**
	 * Realiza la traducci�n de un c�digo de resultado de la plataforma de validaci�n a un resultado interno.
	 * @param codMensajePasarela C�digo de mensaje de la pasarela de validaci�n.
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
	 * Transforma el resultado de la validaci�n en un resultado de servicio.
	 * Pueden darse dos situaciones.
	 * - Excepci�n devuelta por el servicio remoto. En este caso, se recuperan los datos de excepci�n y se imprime su traducci�n a errores de nuestro servicio.
	 * - Terminaci�n completa, independientemente de que se haya validado correctamente o no. En este caso, tambi�n se imprimir� el resultado con traducci�n.
	 * @return
	 */
	public ResultadoType getResultadoValidacion () throws AutenticacionException
	{
		ResultadoType rt = new ResultadoType();
		try{
			if (resultado.isExcepcion())
			{
				rt.setCodResultado(String.valueOf(Errores.GENERICO));
				rt.setDescResultado("Error en la validaci�n de certificado. Contacte con el soporte del servicio");
				rt.setEsValido(false);
			}
			else
			{
				//Comprobar si el tipo es de los admitidos
				if (!esTipoAdmitido())
				{
					throw new AutenticacionException (Errores.TIPO_NO_ADMITIDO);
				}
				//Comprobamos si el certificado es espa�ol. 
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
			throw new AutenticacionException ("Error al tratar el resultado de la validaci�n de plataforma:"+ e.getMessage(),e);
		}
	}
	
	/**
	 * Crea un nuevo elemento {@link ElementoType} y lo a�ade a un objeto de tipo {@link InformacionCertificadoType}
	 * @param identificador Identificador del elemento en InformacionCertificadoType
	 * @param valor Valor
	 * @param inf Elemento {@link InformacionCertificadoType}
	 * @return un {@link InformacionCertificadoType} con el elemento a�adido.
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
			//Raz�n social
			setInfoElement ("razonSocial",resultado.getRazonSocial(),info);
			
			//Comunes
			setInfoElement ("numeroSerie",resultado.getNumeroSerie(),info);
			//Subject
			setInfoElement ("subject",resultado.getSubject(),info);
			//Pa�s
			setInfoElement("pais",resultado.getPais(),info);
			//Clasificaci�n
			setInfoElement("clasificacion",resultado.getClasificacion(),info);
			if (ResultadoValidacion.SELLO_ELECTRONICO.equals(clasificacion) ||
					ResultadoValidacion.CUALIFICADO_SELLO.equals(clasificacion)||
					ResultadoValidacion.SEDE.equals (clasificacion))
			{
				//A�adimos nif entidad suscriptora y organizaci�n.
				setInfoElement ("organizacion",resultado.getOrganizacion(),info);
				setInfoElement ("NIFEntidadSuscriptora",resultado.getNifEntidadSuscriptora(),info);
			}
			//Organization Identifier Europeo
			setInfoElement("OIEuropeo",resultado.getOIEuropeo(), info);
			
			
		}
		return info;
	}
	/**
	 * Verifica que el tipo de certificado que se pasa est� presente en el fichero de certificados admitidos,
	 * y que se indica que se acepta
	 * @param ficheroTiposAdmitidos nombre del fichero de certificados admitidos
	 * @param clasificacion Valor de la clasificaci�n de certificado de @firma
	 * 		  0 = Certificado f�sico - certificado cualificado de firma
	 *        1 = Jur�dico (no cualificado)
	 *        2 = Componentes (no cualificado)
	 *        3= Sede
	 *        4 = Sello electr�nico
	 *        5 = Empleado p�blico
	 *        6 = Entidad sin personalidad Juridica (no cualificado)
	 *        7 = Empleado p�blico con seud�nimo
	 *        8 = Cualificado de sello (UE 910/2014)
	 *        9 = Cualificado de autenticaci�n de sello web (UE 910/2014)
	 *        10= Cualificado de Sello de tiempo
	 *        11= Persona f�sica representante ante las AAPP de persona jur�dica
	 *        12= Persona f�sica representante ante las AAPP de entidad sin persona jur�dica
	 *        
	 * @return true si est� admitido, false si no
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
				throw new AutenticacionException ("Error al tratar el resultado de la validaci�n de plataforma. No se puede comprobar si el tipo de certificado est� admitido:"+ e.getMessage(),e);
			}
			else
			{
				throw new AutenticacionException ("Error al tratar el resultado de la validaci�n de plataforma. No se puede comprobar si el tipo de certificado est� admitido:"+ e.getCause().getMessage(),e);
			}
		}
	}
}
