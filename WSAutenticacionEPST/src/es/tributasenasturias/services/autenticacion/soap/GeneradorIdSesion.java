package es.tributasenasturias.services.autenticacion.soap;

/**
 * Genera id de sesi�n �nico.
 * 
 * @author crubencvs
 *
 */
public final class GeneradorIdSesion {
	private GeneradorIdSesion(){};
	/**
	 * Genera un id de sesi�n �nico.
	 * @return
	 */
	public static String generaIdSesion()
	{
		java.util.UUID uid = java.util.UUID.randomUUID();
		return Integer.toHexString(uid.hashCode()); // UID para esta petici�n. Se a�adir� al log.
	}
}
