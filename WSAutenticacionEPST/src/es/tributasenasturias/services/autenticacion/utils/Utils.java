package es.tributasenasturias.services.autenticacion.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
/**
 * Utilidades comunes.
 * @author crubencvs
 *
 */
public final class Utils {
	private Utils(){}
	/**
     * Envía a consola un mensaje. Utilizado cuando es imposible, como en este caso, realizar log, para
     * mostrar los mensajes de error.
     * @param mensaje Mensaje a enviar a consola
     */
    public static void logConsole(String mensaje, Throwable e)
    {
    	Logger log = Logger.getLogger("es.tributasenasturias.logger");
    	log.setLevel(Level.ALL);
    	ConsoleHandler handler = new ConsoleHandler();
    	handler.setFormatter(new SimpleFormatter());
    	log.addHandler(handler);
    	log.log(Level.SEVERE, mensaje, e);
    }

}
