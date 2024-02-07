package es.tributasenasturias.services.autenticacion.preferencias;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import es.tributasenasturias.services.autenticacion.exceptions.PreferenciasException;
public class Preferencias {
	//Se hace instancia privada y estática.
	static private Preferencias pref = new Preferencias();
	private Preferences mPreferencias;
	private final static String FICHERO_PREFERENCIAS = "prefsAutorizacionEPST.xml";
    private final static String DIRECTORIO_PREFERENCIAS = "proyectos/WSAutenticacionEPST";
    
    private Map<String, String> tablaPreferencias = new HashMap<String, String>();
	
	//nombres de las preferencias
	private final static String KEY_PREF_LOG = "ModoLog";
	private final static String KEY_PREF_ENDPOINT_WS_SECURITY = "EndpointSeguridad";
	private final static String KEY_PREF_APLICACION = "IdAplicacion";
	private final static String KEY_PREF_MODO_VALIDACION = "ModoValidacion";
	private final static String KEY_PREF_OBTENER_INFO = "ObtenerInfo";
	private final static String KEY_PREF_ENDPOINT_AFIRMA = "EndpointAFirma";
	private final static String KEY_PREF_ALIAS_FIRMA = "AliasFirma";
	private final static String KEY_PREF_FICH_TIPOS = "FicheroTiposAdmitidos";
	
	
	
	private Preferencias() 
	{		
		try
		{
			cargarPreferencias();
		}
		catch (Exception e)
		{
			//Para comprobar posteriormente si se ha creado bien, se comprobará que la 
			//variable privada no es estática.
		}
	}
	protected synchronized void cargarPreferencias() throws PreferenciasException
    {
		if(CompruebaFicheroPreferencias())
		{		       
	        FileInputStream inputStream;
			try {
				inputStream = new FileInputStream(DIRECTORIO_PREFERENCIAS + "/" + FICHERO_PREFERENCIAS);
				Preferences.importPreferences(inputStream);
		        inputStream.close();
			
	        
	        //Logger.debug("Fichero importado");
	
	        mPreferencias = Preferences.systemNodeForPackage(this.getClass());
	        
	        String[] keys = mPreferencias.keys();
	        String msgKeys ="Leyendo las siguientes claves -> ";
	        for(int i=0;i<keys.length;i++)
	        {
	        	msgKeys += "["+keys[i]+"] ";
	        }
	        
	        for(int i=0;i<keys.length;i++)
	        {
	        	String value = mPreferencias.get(keys[i], "");
	        	
	        	tablaPreferencias.put(keys[i], value);
	        }
			} catch (FileNotFoundException e) {
				throw new PreferenciasException ("Fichero de preferencias no encontrado:"+ e.getMessage(),e);
			} catch (IOException e) {
				throw new PreferenciasException ("Error de entrada/salida:"+e.getMessage(),e);
			} catch (InvalidPreferencesFormatException e) {
				throw new PreferenciasException ("Error en el formato del fichero de preferencias:"+e.getMessage(),e);
			} catch (BackingStoreException e) {
				throw new PreferenciasException ("Error en el fichero de preferencias:"+ e.getMessage(),e);

			}
		}
    }
	
	private void InicializaTablaPreferencias()
	{
		//Logger.debug("Cargando tabla con preferencias por defecto");
		
		tablaPreferencias.clear();
		
		tablaPreferencias.put(KEY_PREF_LOG,							  		"ALL");
		tablaPreferencias.put(KEY_PREF_ENDPOINT_WS_SECURITY,				"http://bus:7101/WSInternos/ProxyServices/PXSeguridadWS");
		tablaPreferencias.put(KEY_PREF_APLICACION,					  		"princast.stpa.tributas");
		tablaPreferencias.put(KEY_PREF_MODO_VALIDACION,				  		"2");
		tablaPreferencias.put(KEY_PREF_OBTENER_INFO,				  		"S");
		tablaPreferencias.put(KEY_PREF_ENDPOINT_AFIRMA,				  		"https://bus:7101/WSAutenticacionEPST/ProxyServices/PXAFirma");
		tablaPreferencias.put(KEY_PREF_ALIAS_FIRMA,					  		"Tributas");
		tablaPreferencias.put(KEY_PREF_FICH_TIPOS,					  		"proyectos/WSAutenticacionEPST/tiposCertificadoAdmitidos.xml");
		
	}
	
	private boolean CompruebaFicheroPreferencias() throws PreferenciasException
    {
		boolean existeFichero = false;
		
        File f = new File(DIRECTORIO_PREFERENCIAS + "/" + FICHERO_PREFERENCIAS);
        existeFichero = f.exists();
        if (existeFichero == false)
        {
        	//Logger.debug("El fichero de preferencias ("+DIRECTORIO_PREFERENCIAS + "/" + FICHERO_PREFERENCIAS+") no existe!");
            CrearFicheroPreferencias();
        }
        
        return existeFichero;
    }
	
	 /***********************************************************
     * 
     * Creamos el fichero de preferencias con los valores por 
     * defecto
     * 
     ***************************************************************/
    @SuppressWarnings("unchecked")
	private synchronized void CrearFicheroPreferencias() throws PreferenciasException
    {
    	//Logger.debug("INICIO CREACION FICHERO PREFERENCIAS");
    	
        //preferencias por defecto
        mPreferencias = Preferences.systemNodeForPackage(this.getClass());
        
        InicializaTablaPreferencias();
        
        //recorremos la tabla cargada con las preferencias por defecto
        Iterator itr = tablaPreferencias.entrySet().iterator();
        while(itr.hasNext())
        {
        	Map.Entry<String, String> e = (Map.Entry<String, String>)itr.next();
        	//Logger.debug("Cargando en fichero preferencias ['"+e.getKey()+"' - '"+e.getValue()+"']");
        	
        	mPreferencias.put(e.getKey(),e.getValue());
        }

        FileOutputStream outputStream = null;
        File fichero;
        try
        {
            fichero = new File(DIRECTORIO_PREFERENCIAS);
            if (!fichero.exists() && !fichero.mkdirs())
            	{
            	 throw new java.io.IOException ("No se puede crear el directorio de las preferencias.");
            	}
            outputStream = new FileOutputStream(DIRECTORIO_PREFERENCIAS + "/" + FICHERO_PREFERENCIAS);
            mPreferencias.exportNode(outputStream);
        }
        catch (Exception e)
        {
        	throw new PreferenciasException ("Error al crear el fichero de preferencias:"+ e.getMessage(),e);
        }
        finally
        {
            try
            {
                if(outputStream != null)
                {
                    outputStream.close();
                }
            }
            catch(Exception e)
            {
            	throw new PreferenciasException ("Error al crear el fichero de preferencias:"+ e.getMessage(),e);
            }
        }
        
    }
    
    public void recargaPreferencias() throws PreferenciasException
    {
    	cargarPreferencias();
    }
    
    private String getValueFromTablaPreferencias(String key)
    {
    	String toReturn="";
    	
    	if(tablaPreferencias.containsKey(key))
    	{
    		toReturn = tablaPreferencias.get(key);
    	}
    	
    	//Logger.debug("Se ha pedido la preferencia '"+key+"' a lo que el sistema devuelve '"+toReturn+"'");
    	
    	return toReturn;
    }
    
    private synchronized void setValueIntoTablaPreferencias(String key, String value)
    {
    	//Logger.debug("Se actualizara el valor de la preferencia '"+key+"' a '"+value+"'");
    	tablaPreferencias.put(key, value);
    }
	
	// Este método devolverá la instancia de clase.
    public synchronized static Preferencias getPreferencias () throws PreferenciasException
    {
    	if (pref==null)
    	{
    		throw new PreferenciasException("No se han podido recuperar las preferencias.");
    	}
  		pref.cargarPreferencias();
    	return pref;
    }

	public String getModoLog() {
		return getValueFromTablaPreferencias(KEY_PREF_LOG);
	}
	public void setModoLog(String modo) {
		setValueIntoTablaPreferencias(KEY_PREF_LOG, modo);
	}
	public String getEndpointSeguridadWS() {
		return getValueFromTablaPreferencias(KEY_PREF_ENDPOINT_WS_SECURITY);
	}
	public String getIdAplicacion() {
		return getValueFromTablaPreferencias(KEY_PREF_APLICACION);
	}
	public String getModoValidacion() {
		return getValueFromTablaPreferencias(KEY_PREF_MODO_VALIDACION);
	}
	public String getObtenerInfo() {
		return getValueFromTablaPreferencias(KEY_PREF_OBTENER_INFO);
	}
	public String getEndpointAFirma() {
		return getValueFromTablaPreferencias(KEY_PREF_ENDPOINT_AFIRMA);
	}
	public String getAliasFirma() {
		return getValueFromTablaPreferencias(KEY_PREF_ALIAS_FIRMA);
	}
	public String getNombreFicheroTiposAdmitidos() {
		return getValueFromTablaPreferencias(KEY_PREF_FICH_TIPOS);
	}
	
}
