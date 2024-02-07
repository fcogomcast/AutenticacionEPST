package es.tributasenasturias.services.autenticacion.remote;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import es.tributasenasturias.services.autenticacion.exceptions.SystemException;
import es.tributasenasturias.services.autenticacion.soap.LogMessageHandlerClient;
import es.tributasenasturias.services.autenticacion.soap.SeguridadWS;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMDocumentException;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMUtils;


/**
 * Cliente contra el servicio remoto de \@Firma
 * @author crubencvs
 *
 */
public class AFirmaClient {

	private String idLlamada;
	
	/**
	 * Constructor
	 * @param idLlamada Id de la llamada al servicio web al que pertenecerá el objeto construido.
	 */
	public AFirmaClient(String idLlamada)
	{
		this.idLlamada=idLlamada;
	}
	/**
	 * Crear el cliente mediante el que se podrá comunicar con el servicio de \@Firma
	 * @param endpoint Endpoint contra el que enviar las peticiones.
	 * @return Objeto cliente  {@link Dispatch}
	 */
	public Dispatch<SOAPMessage> crearCliente (String endpoint)
	{
		QName servicio = new QName("http://afirmaws/services/ValidarCertificado","ValidacionService");
		QName port = new QName("http://afirmaws/services/ValidarCertificado","ValidarCertificado");
		Service service = Service.create(servicio);
		service.addPort(port, SOAPBinding.SOAP11HTTP_BINDING, endpoint);
		return service.createDispatch(port, SOAPMessage.class, Service.Mode.MESSAGE);
	}
	/**
	 * Forma el mensaje a enviar.
	 * @param certificado Certificado en base64
	 * @param modoValidacion Modo de validación. 0=Simple, 1=Simple+ Revocación,2=1+Cadena de certificación.
	 * @param aplicacion Identificación de la aplicación que realiza la petición. Tributas tiene asignado uno.
	 * @param obtenerInfo true si se quiere obtener información del certificado, false si no
	 * @return
	 * @throws SystemException
	 * @throws XMLDOMDocumentException
	 */
	public Document formarMensaje(String certificado, String aplicacion, String modoValidacion, boolean obtenerInfo) throws SystemException, XMLDOMDocumentException
	{
		Document payload;
		Document envelope;
		//Crear mensaje de petición.
		payload=XMLDOMUtils.parseXML(AFirmaClient.class.getResourceAsStream("/xml/Payload.xml"));
		Node certNodo = XMLDOMUtils.selectSingleNode(payload,"mensajeEntrada/parametros/certificado");
		if (certNodo!=null)
		{
			CDATASection texto = payload.createCDATASection(certificado);
			certNodo.appendChild(texto);
		}
		XMLDOMUtils.setNodeText(payload, "mensajeEntrada/parametros/idAplicacion", aplicacion);
		XMLDOMUtils.setNodeText(payload, "mensajeEntrada/parametros/modoValidacion", modoValidacion);
		XMLDOMUtils.setNodeText(payload, "mensajeEntrada/parametros/obtenerInfo", Boolean.toString(obtenerInfo));
		//Incluir mensaje en envelope
		envelope = XMLDOMUtils.parseXML(AFirmaClient.class.getResourceAsStream("/xml/Envelope.xml"));
		Node request = XMLDOMUtils.selectSingleNode(envelope, "//*[local-name()='ValidarCertificadoRequest']");
		if (request!=null)
		{
			CDATASection texto= envelope.createCDATASection(XMLDOMUtils.getXMLText(payload));
			request.appendChild(texto);
		}
		else //Error grave, no podemos montar el mensaje
		{
			throw new SystemException("Imposible componer mensaje de envío a @Firma. Revisar plantilla de mensaje.");
		}
		return envelope;
	}
	/**
	 * Asigna los manejadores de log y tratamiento de las cabeceras WS-Security
	 * al cliente de comunicación con \@Firma.
	 * @param d Objeto {@link Dispatch} al que se añadirán los manejadores.
	 * @param aliasCertificado Alias de certificado para las operaciones de seguridad.
	 */
	@SuppressWarnings("unchecked")
	private void asignaManejadores(Dispatch<SOAPMessage> d, String aliasCertificado)
	{
		List<Handler> handler=d.getBinding().getHandlerChain();
		if (handler==null)
		{
			handler= new ArrayList<Handler>();
		}
		handler.add(new SeguridadWS(aliasCertificado, this.idLlamada));
		handler.add(new LogMessageHandlerClient(this.idLlamada));
		d.getBinding().setHandlerChain(handler);
	}
	
	
	/**
	 * Invoca la operación de validación de certificado de \@Firma
	 * @param certificado Certificado en base64
	 * @param modoValidacion Modo de validación. 0=Simple, 1=Simple+ Revocación,2=1+Cadena de certificación.
	 * @param aplicacion Identificación de la aplicación que realiza la petición. Tributas tiene asignado uno.
	 * @param obtenerInfo true si se quiere obtener información del certificado, false si no
	 * @param aliasFirma Alias de la firma para usar en WS-Security.
	 * @param endpoint Endpoint a donde enviar la petición.
	 * @return {@link Document} con el mensaje SOAP resultado de la petición.
	 * @throws SystemException
	 */
	public Document validarCertificado (String certificado, String modoValidacion,String aplicacion, boolean obtenerInfo, String aliasFirma,String endpoint) throws SystemException
	{
		Document envelope;
		SOAPMessage response;
		//Declarar servicio
		Dispatch<SOAPMessage> dispatch = crearCliente( endpoint);
		asignaManejadores(dispatch, aliasFirma);
		try
		{
		envelope= formarMensaje(certificado, aplicacion, modoValidacion, obtenerInfo);
		//Llamar y retornar.
		String msg = XMLDOMUtils.getXMLText(envelope); 
		MessageFactory msgFact = MessageFactory.newInstance();
		SOAPMessage soap = msgFact.createMessage();
		soap.getSOAPPart().setContent(new StreamSource(new ByteArrayInputStream(msg.getBytes("UTF-8"))));
		response=dispatch.invoke(soap);
		}
		catch (XMLDOMDocumentException e)
		{
			throw new SystemException("Imposible componer mensaje de envío a @Firma"+e.getMessage(),e);
		} catch (SOAPException e) {
			throw new SystemException("Imposible componer mensaje de envío a @Firma."+e.getMessage(),e);
		} catch (UnsupportedEncodingException e) {
			throw new SystemException("Imposible componer mensaje de envío a @Firma."+e.getMessage(),e);
		}
		return response.getSOAPPart();
	}
	
}
