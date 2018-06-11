package jddclient.updater;

/**
 * Thrown if an IP address update is sent to the DNS provider, but the IP
 * address is the same as the currently set address. Providers does not like
 * this, so it is considered as an exception.
 */
public class SameIpException extends Exception {
    private static final long serialVersionUID = -8643332222301254531L;

    public SameIpException(String message) {
        super(message);
    }

}
