package es.tributasenasturias.services.autenticacion.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import es.tributasenasturias.seguridadws.Seguridad;
import es.tributasenasturias.seguridadws.Seguridad_Service;
import es.tributasenasturias.services.autenticacion.exceptions.PreferenciasException;
import es.tributasenasturias.services.autenticacion.preferencias.Preferencias;
import es.tributasenasturias.services.autenticacion.xml.XMLDOMUtils;

/**
 * Utilizado para gestionar el mensaje recibido de \@Firma con WS-Security. Ha de indicarse que se 
 * entienden las cabeceras de seguridad, de otra forma la recepción del mensaje fallará.
 * @author crubencvs
 *
 */
public class SeguridadWS implements SOAPHandler<SOAPMessageContext>{

	private String idLlamada;
	private String alias;
	/**
	 * Constructor
	 * @param aliasCertificado Alias de certificado con el que se realizarán las operaciones de firma.
	 */
	public SeguridadWS(String aliasCertificado, String idLlamada)
	{
		alias= aliasCertificado;
		this.idLlamada= idLlamada;
	}
	
	/**
	 * Genera un SOAP Fault con los parámetros indicados.
	 * @param msg Mensaje SOAP
	 * @param reason Razón del fallo
	 * @param codigo Código de fallo
	 * @param mensaje Mensaje de fallo
	 */
	@SuppressWarnings("unchecked")
	public void generateSOAPErrMessage(SOAPMessage msg, String reason, String codigo, String mensaje) {
	       try {
	    	  SOAPEnvelope soapEnvelope= msg.getSOAPPart().getEnvelope();
	          SOAPBody soapBody = msg.getSOAPPart().getEnvelope().getBody();
	          SOAPFault soapFault = soapBody.addFault();
	          soapFault.setFaultString(reason);
	          Detail det= soapFault.addDetail();
	          Name name = soapEnvelope.createName("id");
	          det.addDetailEntry(name);
	          
	          name = soapEnvelope.createName("mensaje");
	          det.addDetailEntry(name);
	          DetailEntry entry;
	          Iterator<DetailEntry> it=det.getDetailEntries();
	          while (it.hasNext())
	          {
	        	  entry=it.next();
	        	  if (entry.getLocalName().equals("id"))
	        	  {	
	        		XMLDOMUtils.setNodeText (entry.getOwnerDocument(),entry, codigo);  
	        	  }
	        	  if (entry.getLocalName().equals("mensaje"))
	        	  {	
	        		XMLDOMUtils.setNodeText (entry.getOwnerDocument(),entry, mensaje);  
	        	  }
	        		  
	          }
	          throw new SOAPFaultException(soapFault); 
	       }
	       catch(SOAPException e) { }
	} 
	
	
	@Override
	public Set<QName> getHeaders() {
		//Indicamos que entendemos la cabecera de seguridad de WS-Security.
		QName security= new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd","Security","wsse");
		Set<QName> headersEntendidos= new HashSet<QName>();
		headersEntendidos.add(security);
		return headersEntendidos;
	}

	@Override
	public void close(MessageContext context) {
		
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		ByteArrayOutputStream bos = null;
		ByteArrayInputStream bi=null;
		try {
			Boolean salida = (Boolean) context.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (salida.booleanValue())
			{
				bos=new ByteArrayOutputStream();
				context.getMessage().writeTo(bos);
				String msg = new String(bos.toByteArray());
				String msgFirmado=getSOAPFirmado(msg,alias);
				bi= new ByteArrayInputStream(msgFirmado.getBytes("UTF-8"));
				StreamSource source= new StreamSource(bi);
				context.getMessage().getSOAPPart().setContent(source);
			}
		} catch (SOAPException e) {
			generateSOAPErrMessage (context.getMessage(),"Error al manejar seguridad SOAP","9999",e.getMessage());
		} catch (IOException e) {
			generateSOAPErrMessage (context.getMessage(),"Error al manejar seguridad SOAP","9999",e.getMessage());
		} catch (PreferenciasException e)
		{
			generateSOAPErrMessage (context.getMessage(),"Error al manejar seguridad SOAP","9999",e.getMessage());
		}
		
		finally
		{
			if (bos!=null)
			{
				try{ bos.close();} catch (Exception e){}
			}
			if (bi!=null)
			{
				try {bi.close();} catch (Exception e){}
			}
		}
			
		
		
		return true;
	}
	
	/**
	 * Firma el mensaje con WSSecurity
	 * @param msg Mensaje a firmar, en forma de cadena.
	 * @param alias Alias de certificado con el que firmar.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getSOAPFirmado (String msg, String alias) throws PreferenciasException
	{
		Seguridad_Service srv = new Seguridad_Service();
		Seguridad port = srv.getSeguridadSOAP();
		BindingProvider bpr= (BindingProvider) port;
		Preferencias pr = Preferencias.getPreferencias();
		bpr.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, pr.getEndpointSeguridadWS());
		List<Handler> handler=bpr.getBinding().getHandlerChain();
		if (handler==null)
		{
			handler= new ArrayList<Handler>();
		}
		handler.add(new LogMessageHandlerClient(this.idLlamada));
		bpr.getBinding().setHandlerChain(handler);
		return port.firmarMensaje(msg, alias);
	}

}
