package es.tributasenasturias.services.autenticacion.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import es.tributasenasturias.services.autenticacion.preferencias.Preferencias;
import es.tributasenasturias.services.autenticacion.utils.Utils;
/**
 * Clase que permite guardar mensajes en un fichero.
 * @author crubencvs
 *
 */
public class Logger {
	private static final String SEPARADOR = "::";
	private final static String LOG_FILE = "Application.log";
	private final static String LOG_DIR = "proyectos/WSAutenticacionEPST";
	private  String idLlamada = null;
	
	public Logger(String idLlamada)
	{
		this.idLlamada=idLlamada;
	}
	public enum LEVEL {TRACE, DEBUG, INFO, WARNING, ERROR,ALL}
	//Método privado que comprueba el nivel de detalle de log definido en el fichero de preferencias
	// y devuelve si en base a un nivel de detalle de log que se indica por parámetros se han de 
	// imprimir los mensajes o no.
	private boolean esImprimible(LEVEL nivel)
	{
		Preferencias pr=null;
		boolean res=false;
		try
		{
			pr= Preferencias.getPreferencias();
			String modo = pr.getModoLog();
			if (modo.equalsIgnoreCase(LEVEL.ALL.toString())) //Imprimir todo. Siempre verdadero.
			{res=true;}
			else if (modo.equalsIgnoreCase(LEVEL.DEBUG.toString()))
			{
				if (nivel.equals(LEVEL.DEBUG)|| nivel.equals(LEVEL.WARNING)|| nivel.equals(LEVEL.ERROR))
				{res=true;}
				else {res=false;}
			}
			else if (modo.equalsIgnoreCase(LEVEL.WARNING.toString()))
			{
				if (nivel.equals(LEVEL.WARNING)|| nivel.equals(LEVEL.ERROR))
				{res=true;}
				else {res=false;}
			}
			else if (modo.equalsIgnoreCase(LEVEL.ERROR.toString()))
			{
				if (nivel.equals(LEVEL.ERROR))
				{res=true;}
				else {res=false;}
			}
			else if (modo.equalsIgnoreCase(LEVEL.INFO.toString()))
			{
				if (nivel.equals(LEVEL.INFO))
				{res=true;}
				else {res=false;}
			}
			else if (modo.equalsIgnoreCase(LEVEL.TRACE.toString()))
			{
				if (nivel.equals(LEVEL.TRACE))
				{res=true;}
				else {res=false;}
			}
		}
		catch (Exception ex) // En principio sólo debería ocurrir porque hay un error al abrir el fichero
							// de preferencias. En ese caso, devolvemos true para que imprima todo se pase el 
							// parámetro que se pase, incluído error.
		{
			res=true;
		}
		return res;
	}
	// Método sincronizado. 
	/**
	 * Método que realiza log.
	 * 
	 */
	public final synchronized void doLog(String message, LEVEL level)
	{
		File file;
        FileWriter fichero = null;
        PrintWriter pw=null;
        String cPrefijo="";
        try
        {
        	if (idLlamada!=null)
        	{
        		cPrefijo = "Id:"+idLlamada+SEPARADOR;
        	}
        	else
        	{
        		cPrefijo="";
        	}
            Date today = new Date();
            String completeMsg = "AutenticacionEPST "+SEPARADOR+cPrefijo + today + SEPARADOR + level + SEPARADOR + message;
            
            file = new File(LOG_DIR);
            if (!file.exists() && !file.mkdirs())
            	{
            		throw new IOException("No se puede crear el directorio de log."); 
            	}
            fichero = new FileWriter(LOG_DIR + "/" + LOG_FILE, true);//true para que agregemos al final del fichero
            if (fichero!=null)
            {
            	pw = new PrintWriter(fichero);
            
            	pw.println(completeMsg);
            }
            
        }
        catch (IOException e)
        {
            Utils.logConsole("Error escribiendo log '"+message+"' -> "+e.getMessage(),e);
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
        		Utils.logConsole("Error cerrando flujo de impresión: " + e.getMessage(),e);
        	}
            try
            {
            	if(fichero != null)
                {
            		fichero.close();
                }
            }
            catch(Exception e)
            {
                Utils.logConsole("Error cerrando fichero de log -> "+e.getMessage(),e);
            }
        }
	}
	
	public final void trace(String message)
	{	
		if (esImprimible(LEVEL.TRACE))
		{doLog(message,LEVEL.TRACE);}
	}
	
	public final void trace(StackTraceElement[] stackTraceElements)
	{
		 if (stackTraceElements == null){
	            return;
		 }
		 if (esImprimible(LEVEL.TRACE))
		 {
	        for (int i = 0; i < stackTraceElements.length; i++)
	        {
	            doLog("StackTrace -> " + stackTraceElements[i].getFileName() + SEPARADOR + stackTraceElements[i].getClassName() + SEPARADOR + stackTraceElements[i].getFileName() + SEPARADOR + stackTraceElements[i].getMethodName() + SEPARADOR + stackTraceElements[i].getLineNumber(),LEVEL.TRACE);
	        }
		 }
	}
	
	public final void debug(String message)
	{
		if (esImprimible(LEVEL.DEBUG))
		{doLog(message,LEVEL.DEBUG);}
	}
	
	public final void info(String message)
	{ // Info siempre se muestra.
		doLog(message,LEVEL.INFO); 
	}
	
	public final  void warning(String message)
	{
		if (esImprimible(LEVEL.WARNING))
		{doLog(message,LEVEL.WARNING);}
	}
	
	public final void error(String message)
	{
		if (esImprimible(LEVEL.ERROR))
		{doLog(message,LEVEL.ERROR);}
	}
	public final void error(String message, Throwable e)
	{
		if (esImprimible(LEVEL.ERROR))
		{doLog(message,LEVEL.ERROR);
		 doLog(message,LEVEL.TRACE);}
	}
	/**
	 * @return the idLlamada
	 */
	public synchronized final String getIdLlamada() {
		return idLlamada;
	}
	/**
	 * @param idLlamada the idLlamada to set
	 */
	public synchronized final void setIdLlamada(String idLlamada) {
		this.idLlamada = idLlamada;
	}
}
