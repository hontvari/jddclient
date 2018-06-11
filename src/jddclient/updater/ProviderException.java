package jddclient.updater;

public class ProviderException extends UpdaterException {
    private static final long serialVersionUID = 9172351242282697436L;
    private final FurtherAction furtherAction;

    public ProviderException(String message, FurtherAction furtherAction) {
        super(message);
        this.furtherAction = furtherAction;
    }

    public FurtherAction getFurtherAction() {
        return furtherAction;
    }

    public enum FurtherAction {
        Permanent, Retry;
    }
}
