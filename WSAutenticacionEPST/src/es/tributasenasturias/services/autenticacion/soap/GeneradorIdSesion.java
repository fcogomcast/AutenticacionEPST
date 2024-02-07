package es.tributasenasturias.services.autenticacion.soap;

/**
 * Genera id de sesión único.
 * 
 * @author crubencvs
 *
 */
public final class GeneradorIdSesion {
	private GeneradorIdSesion(){};
	/**
	 * Genera un id de sesión único.
	 * @return
	 */
	public static String generaIdSesion()
	{
		java.util.UUID uid = java.util.UUID.randomUUID();
		return Integer.toHexString(uid.hashCode()); // UID para esta petición. Se añadirá al log.
	}
}
