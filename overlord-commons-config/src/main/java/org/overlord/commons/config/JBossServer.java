package org.overlord.commons.config;

public class JBossServer {

	private static String baseUrl = null;
	
	/**
     * Uses the 'jboss.bind.address' and 'jboss.socket.binding.port-offset' parameters to construct
     * the baseUrl. The scheme defaults to http, but is set to https if the port setting ends in 443.
     * Defaults to http://localhost:8080.
     * @return baseUrl of the server.
     */
    public static String getBaseUrl() {
    	if (baseUrl==null) {
	    	String hostname = System.getProperty("jboss.bind.address","localhost"); //$NON-NLS-1$ //$NON-NLS-2$
	    	Integer portOffset = Integer.valueOf(System.getProperty("jboss.socket.binding.port-offset","0")); //$NON-NLS-1$ //$NON-NLS-2$
	    	String port = String.valueOf(8080 + portOffset);
	    	String scheme = "http://"; //$NON-NLS-1$
	    	if (port.endsWith("443")) scheme = "https://"; //$NON-NLS-1$ //$NON-NLS-2$
	    	baseUrl = scheme + hostname + ":" + port; //$NON-NLS-1$
    	}
    	return baseUrl;
    }
}
