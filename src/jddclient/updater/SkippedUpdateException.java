package jddclient.updater;

/**
 * Signals that it was not necessary or it was forbidden to contact the DNS
 * service provider regarding the update of an IP address.
 */
public class SkippedUpdateException extends Exception {
    private static final long serialVersionUID = -1372309951858914436L;

    public SkippedUpdateException(String message) {
        super(message);
    }
}
