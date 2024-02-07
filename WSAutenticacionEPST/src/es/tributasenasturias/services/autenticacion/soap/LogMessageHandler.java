package es.tributasenasturias.services.autenticacion.soap;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import es.tributasenasturias.services.autenticacion.utils.Utils;

 
/**
 * Clase manejadora de mensajes soap
 * 
 * @author Andres
 *
 */
public class LogMessageHandler implements SOAPHandler<SOAPMessageContext> 
{
	private static final String SEPARADOR = "::";
	private final static String LOG_FILE = "SOAP_SERVER.log";
	private final static String LOG_DIR = "proyectos/WSAutenticacionEPST";
	
    public boolean handleMessage(SOAPMessageContext messageContext) 
    {
    	log(messageContext);
    	return true;
    }
 
    public Set<QName> getHeaders() 
    {
        return Collections.emptySet();
    }
 
    public boolean handleFault(SOAPMessageContext messageContext) 
    {
        return true;
    }
 
    public void close(MessageContext context) 
    {
    }
    
    private void log(SOAPMessageContext messageContext) 
    { 	
    	//obtenemos la ip origen
    	HttpServletRequest request = (HttpServletRequest)messageContext.get(MessageContext.SERVLET_REQUEST);        
        String ip = request.getRemoteAddr();
    	
        SOAPMessage msg = messageContext.getMessage();
        FileWriter out = null;
        PrintWriter pw=null;
        synchronized (this)
        {
	     try 
	     {
	    	 Boolean salida = (Boolean) messageContext.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	    	 String direccion=(salida)?"Envío":"Recepción"; // La propiedad nos dice si el mensaje es de salida o no.
	    	 //Obtenemos, si existe, el id de sesión de contexto.
	    	 String idSesion = (String)messageContext.get(Constantes.ID_SESION);
	    	 //obtenemos el array de bytes que se corresponde con el mensaje soap
	    	 ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
	         msg.writeTo(byteArray);
	         
	         //componemos la linea del log
	         String soapMessage = new String(byteArray.toByteArray());
	         Date today = new Date();
	         String log ="";
	         if (idSesion!=null)
	         {
	        	 log = "AutenticaciónEPST::SOAP_SERVER " +SEPARADOR+"sesión"+SEPARADOR+idSesion +SEPARADOR+direccion+SEPARADOR+ ip + SEPARADOR + today + SEPARADOR + soapMessage;
	         }
	         else
        	 {
	        	 log = "AutenticaciónEPST::SOAP_SERVER "+SEPARADOR +direccion+SEPARADOR+ ip + SEPARADOR + today + SEPARADOR + soapMessage; 
        	 }
	         
	         out = new FileWriter(LOG_DIR+"/"+LOG_FILE,true);
	         if (out!=null)
	         {
	        	 pw = new PrintWriter(out);
	        	 pw.println(log);
	         }
	     } 
	     catch (SOAPException ex) 
	     {
	         Utils.logConsole("AutenticacionEPST::SOAP Exception escribiendo mensaje a fichero: "+ex.getMessage(),ex);
	     } 
	     catch (IOException ex) 
	     {
	    	 Utils.logConsole("AutenticacionEPST::IO Exception escribiendo mensaje a fichero: "+ex.getMessage(),ex);
	     }
	     finally
	     {
	    	try
         	{
	    		if (pw!=null)
	    		{
	    			pw.close();
	    		}
         	}
         	catch (Exception e) // En principio no debería devolver, nunca, una excepción. Se controla 
         						// por si hay cambios en la implementación.
         	{
         		Utils.logConsole ("AutenticacionEPST::Error cerrando flujo de impresión: " + e.getMessage(),e);
         	}
            
            try
            {
            	if(out != null)
                {
            		out.close();
                }
            }
            catch(Exception e)
            {
            	Utils.logConsole("AutenticacionEPST::Error cerrando fichero de log -> "+e.getMessage(),e);
            }
	     }
        }
    }
}
