package jddclient.updater;


public class UpdaterException extends Exception {
    private static final long serialVersionUID = -2660469952713171066L;

    public UpdaterException(String message) {
        super(message);
    }

    public UpdaterException(Throwable cause) {
        super(cause);
    }

    public UpdaterException(String message, Throwable cause) {
        super(message, cause);
    }

}
