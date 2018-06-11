package jddclient.updater;

import org.joda.time.Duration;

public class ProviderMaintenanceException extends ProviderException {
    private static final long serialVersionUID = -8098444742098677281L;
    private final Duration maintenanceDuration;

    public ProviderMaintenanceException(String message, Duration maintenanceDuration) {
        super(message, FurtherAction.Retry);
        this.maintenanceDuration = maintenanceDuration;
    }

    public Duration getMaintenanceDuration() {
        return maintenanceDuration;
    }

}
