package jddclient.updater;

/**
 *  Thrown when an update is skipped because the updater is in permanent failure state.
 */
public class PermanentSkippedUpdateException extends SkippedUpdateException {
    private static final long serialVersionUID = 3856974205089758259L;

    public PermanentSkippedUpdateException(String message) {
        super(message);
    }
}
