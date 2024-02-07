package es.tributasenasturias.services.autenticacion.mediacion;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import es.tributasenasturias.services.autenticacion.constantes.Plataformas;
import es.tributasenasturias.services.autenticacion.exceptions.SystemException;
import es.tributasenasturias.services.autenticacion.log.Logger;
import es.tributasenasturias.services.autenticacion.plataformas.ValidadorCertificado.ResultadoValidacion;
import es.tributasenasturias.services.autenticacion.preferencias.Preferencias;
import es.tributasenasturias.services.autenticacion.remote.AFirmaClient;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMDocumentException;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMUtils;

/**
 * Realiza la adaptación de mensajes entre objetos de dominio y cliente de \@Firma
 * @author crubencvs
 *
 */
public class AdaptadorAFirma {

	private Logger log; 
	private AFirmaClient client;
	/**
	 * Constructor
	 * @param idLlamada Id de la llamada a la que pertenecerá el objeto creado.
	 */
	public AdaptadorAFirma(String idLlamada)
	{
		client = new AFirmaClient(idLlamada);
		log = new Logger(idLlamada);
	}
	
	private static class ElementosApellido {
		private String apellido1=null;
		private String apellido2=null;
		public String getApellido1() {
			return apellido1;
		}
		public void setApellido1(String apellido1) {
			this.apellido1 = apellido1;
		}
		public String getApellido2() {
			return apellido2;
		}
		public void setApellido2(String apellido2) {
			this.apellido2 = apellido2;
		}
	}
	/**
	 * Devuelve datos del nodo "ResultadoValidacion" de la respuesta del servicio remoto.
	 * @param doc Documento que contiene el nodo "ResultadoValidacion"
	 * @param nombreNodo Nombre del nodo de "ResultadoValidacion" cuyo valor queremos recuperar.
	 * @return
	 */
	private String getDatosResultado (Document doc, String nombreNodo)
	{
		String valor=null;
		Node node = XMLDOMUtils.selectSingleNode(doc, "//*[local-name()='ResultadoProcesamiento']/*[local-name()='ResultadoValidacion']/*[local-name()='"+nombreNodo+"']");
		if (node!=null)
		{
			valor = XMLDOMUtils.getNodeText(node);
		}
		return valor;
		
	}
	/**
	 * Recupera el valor de un campo de InfoCertificado en la respuesta
	 * @param doc Documento de respuesta que contiene la información del certificado según \@Firma
	 * @param idCampo Identificador de campo cuyo valor queremos recuperar
	 * @return
	 */
	private String getValorCampo (Document doc, String idCampo)
	{
		String valor=null;
		//FIXME: Desde @Firma parece que puede llegar más de un namespace. Arreglar esto para que trabaje con el que llegue, cogiendo el namespace y creando un contexto de NAMESPACE en la búsqueda.
		Node node = XMLDOMUtils.selectSingleNode(doc, "//*[local-name()='InfoCertificado']/*[local-name()='Campo'][normalize-space(*[local-name()='idCampo']/text())='"+idCampo+"']/*[local-name()='valorCampo']");
		if (node!=null)
		{
			valor = XMLDOMUtils.getNodeText(node);
		}
		return valor;
	}
	/**
	 * Recupera el valor de un nodo del elemento "Excepcion" para una respuesta recibida del servicio remoto.
	 * @param doc Documento que contiene la respuesta del servicio remoto.
	 * @param nombreNodo Nombre del nodo cuyo valor se quiere obtene.r
	 * @return
	 */
	private String getDatosExcepcion (Document doc, String nombreNodo)
	{
		String valor=null;
		Node node = XMLDOMUtils.selectSingleNode(doc.getDocumentElement(), "//*[local-name()='respuesta']/*[local-name()='Excepcion']/*[local-name()='"+nombreNodo+"']");
		if (node!=null)
		{
			valor = XMLDOMUtils.getNodeText(node);
		}
		return valor;
		
	}
	/**
	 * Recupera el identificador fiscal de certificado, en función del resto de datos
	 * Esto es así porque no siempre viene el CIF en "NIF-CIF", sino que al menos 
	 * en los sellos cualificados de entidad (clasificación 8) emitidos por 
	 * FirmaProfesional viene en el nodo OI_Europeo
	 * @param res
	 * @return
	 */
	private String getIdentificadorFiscalCertificado(ResultadoValidacion res) {
		String identificador=null;
		//Podría ser un formato de sello de entidad cualificado según UE 910/2014, por ahora es el que recibimos
		//que no tiene NIF ni CIF, lo envía el Principado
		if (ResultadoValidacion.CUALIFICADO_SELLO.equals(res.getClasificacion())) {
			String oiEuropeo= res.getOIEuropeo(); 
			if (oiEuropeo!=null) {
				identificador = oiEuropeo.toUpperCase().replace("VATES-", "");
			}
		}
		return identificador;
	}
	
	private ElementosApellido getElementosApellido (String apellido1, String apellido2, String apellidos)
	{
		ElementosApellido elementosApellido= new ElementosApellido();
		if ((apellido1!=null && apellido2!=null) && 
				(!"".equals(apellido1) || !"".equals(apellido2)))	{
			elementosApellido.setApellido1(apellido1);
			elementosApellido.setApellido2(apellido2);
		}
		else //No están desglosados
		{
			if (apellidos!= null && !"".equals(apellidos))
			{
				String[] tokens= apellidos.trim().split("\\s+"); //Espacios (1 o más) separan palabras.
				if (tokens.length>0)
				{
					elementosApellido.setApellido1(tokens[0]);
					if (tokens.length>1)
					{
						StringBuilder builder= new StringBuilder(30);
						for (int i=1;i<tokens.length;i++)
						{
							builder.append(" "+ tokens[i]);
						}
						elementosApellido.setApellido2(builder.toString());
					}
				}
			}
		}
		return elementosApellido;
	}
	/**
	 * Transforma la respuesta recibida del cliente de Firma en un resultado de validación
	 * @param doc Documento que contiene la respuesta recibida del cliente
	 * @return {@link ResultadoValidacion} que tendrá los datos ya procesados.
	 * @throws XMLDOMDocumentException
	 * @throws SystemException
	 */
	private ResultadoValidacion transformarRespuesta(Document doc) throws XMLDOMDocumentException, SystemException
	{
		ResultadoValidacion res= new ResultadoValidacion();
		Document payload=null;
		//Texto de resultado
		Node respuesta = XMLDOMUtils.selectSingleNode(doc.getDocumentElement(), "//*[local-name()='ValidarCertificadoReturn']");
		if (respuesta!=null)
		{
			payload = XMLDOMUtils.parseXml(XMLDOMUtils.getNodeText(respuesta));
			if (payload==null)
			{
				throw new SystemException ("No se ha recibido una respuesta válida o la respuesta está vacía.");
			}
		}
		else
		{
			throw new SystemException ("No se ha recibido una respuesta válida o la respuesta está vacía");
		}
		//Lo primero, si es erróneo o no.
		//Si lo es, se podrá devolver o no la información de certificado.
		String codError = getDatosExcepcion(payload,"codigoError");
		if (codError==null)
		{
			//Recuperamos el valor de la respuesta normal.
			String resultado=getDatosResultado(payload,"resultado");
			log.debug("Validación de @FIRMA regresa con resultado:"+resultado);
			if ("0".equals(resultado)) //0 es el indicador de que el certificado se ha validado correctamente.
			{
				res.setError(false);
			}
			else
			{
				res.setError(true);
			}
			res.setCodResultado(resultado);
			res.setDesResultado(getDatosResultado(payload,"descripcion"));
			String clasificacion=getValorCampo(payload,"clasificacion");
			res.setClasificacion(clasificacion);
			//Datos del certificado
			res.setNif(getValorCampo(payload,"NIFResponsable"));
			res.setNombre(getValorCampo(payload,"nombreResponsable"));
			//03/07/2019. Se modifica el tratamiento de apellidos, ya que en un caso
			//en lugar de primerApellidoResponsable y segundoApellidoResponsable ha llegado 
			//"apellidosResponsable"
			ElementosApellido elementosApellido = getElementosApellido(getValorCampo(payload,"primerApellidoResponsable"),
																	   getValorCampo(payload,"segundoApellidoResponsable"),
																	   getValorCampo(payload,"ApellidosResponsable"));
			//res.setApellido1(getValorCampo(payload,"primerApellidoResponsable"));
			//res.setApellido2(getValorCampo(payload,"segundoApellidoResponsable"));
			res.setApellido1(elementosApellido.getApellido1());
			res.setApellido2(elementosApellido.getApellido2());
			
			//Fin 03/07/2019
			res.setEmail(getValorCampo(payload,"email"));
			res.setFechaNacimiento(getValorCampo(payload,"fechaNacimiento"));
			//Añadimos el OI_Europeo, que además puede ser necesario para recuperar el CIF de los sellos cualificados
			res.setOIEuropeo(getValorCampo(payload,"OI_Europeo"));
			res.setCif(getValorCampo(payload,"NIF-CIF"));
			res.setRazonSocial(getValorCampo(payload,"razonSocial"));
			res.setNumeroSerie(getValorCampo(payload,"numeroSerie"));
			res.setSubject(getValorCampo(payload,"subject"));
			res.setPais(getValorCampo(payload,"pais"));
			//FIXME: Esto es una suposición, no tenemos documentación del mapeo de campos.
			if (ResultadoValidacion.SELLO_ELECTRONICO.equals (clasificacion) || 
					ResultadoValidacion.CUALIFICADO_SELLO.equals (clasificacion) ||
					ResultadoValidacion.SEDE.equals (clasificacion))
			{
				String identificador= getValorCampo(payload,"NIFEntidadSuscriptora");
				if (identificador==null || "".equals(identificador)) {
					identificador= getIdentificadorFiscalCertificado(res);
				}
				res.setNifEntidadSuscriptora(identificador);
				res.setOrganizacion(getValorCampo(payload,"organizacion"));
			}
		}
		else
		{
			log.debug("Validación de @FIRMA regresa con código de error:"+codError);
			//Excepción. Este será el código a devolver.
			res.setError(true);
			res.setExcepcion(true);
			res.setCodError(codError);
			res.setDesError(getDatosExcepcion(payload,"descripcion"));
		}
		return res;
	}
	/**
	 * Realiza la llamada a "Validar Certificado", y traduce los parámetros y los resultados
	 * cuando sea necesario.
	 * @param certificado Certificado a validar.
	 * @param pref Preferencias. De aquí deberá tomar los datos de modo de validación, aplicacion <br/>
	 * y modo de validación.
	 * @return
	 */
	public ResultadoValidacion callValidarCertificado(byte[] certificado, Preferencias pref) throws SystemException
	{
		try{
			String cert = new String(Base64.encode(certificado));
			String aplicacion = pref.getIdAplicacion();
			String modoValidacion = pref.getModoValidacion();
			boolean obtenerInfo= "S".equals(pref.getObtenerInfo())?true:false;
			String alias = pref.getAliasFirma();
			String endpoint= pref.getEndpointAFirma();
			//Llamada
			Document respuesta=client.validarCertificado(cert, modoValidacion, aplicacion, obtenerInfo, alias,endpoint);
			//Convertimos la respuesta a un ResultadoValidacion.
			ResultadoValidacion rva = transformarRespuesta(respuesta); 
			rva.setPlataformaValidacion(Plataformas.AFIRMA); //Registramos la plataforma por la que se ha validado.
			//TODO: Traducir los modos de validación a algo independiente de la plataforma.
			rva.setModoValidacion(modoValidacion);
			return rva;
		}
		catch (XMLDOMDocumentException e)
		{
			throw new SystemException ("Error en llamada a validación de certificado en plataforma remota:"+e.getMessage(),e);
		}
	}
	
}
