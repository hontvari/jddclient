package jddclient.updater;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jddclient.Store;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Element;

public abstract class AbstractUpdater implements Updater {

    private static final int MAX_ATTEMPTS = 5;
    private static final DateTimeFormatter ISO_TIME_FORMAT = ISODateTimeFormat
            .dateTimeNoMillis();
    /**
     * It identifies a provider configuration instance in the saved status file.
     */
    private String name = "DefaultUpdater";
    private Store store;
    private TransactionState transactionState = TransactionState.IDLE;
    private Health health = Health.HEALTHY;
    /**
     * A not null value specifies the time until which no updates should be
     * attempted.
     */
    private Instant retryAfter;

    /**
     * The address which is currently set on the service. Null means no update
     * happened yet.
     */
    private InetAddress activeAddress = null;
    /**
     * Date of last successful update.
     */
    private Instant updateDate = null;
    /**
     * count of continuous failures
     */
    private int cFailures = 0;

    @Override
    public void initialize() {
        // nothing to do
    }

    /**
     * Sends the new address to the service, but only if it is necessary.
     */
    @Override
    public void update(InetAddress address) throws UpdaterException,
            SameIpException, SkippedUpdateException {
        if (health == Health.PERMANENT_FAILURE)
            throw new PermanentSkippedUpdateException("Updater is in a permanent "
                    + "failure status, IP addresses are not sent to the "
                    + "provider. Fix the problem and reset the "
                    + "status by a forced update");
        if (activeAddress != null && activeAddress.equals(address))
            throw new SkippedUpdateException(
                    "The IP address has not been changed, therefore it is "
                            + "not necessary to contact the provider.");
        if (retryAfter != null && retryAfter.isAfterNow())
            throw new SkippedUpdateException(
                    "IP address has been changed, but the provider requested "
                            + "a break until " + retryAfter
                            + ", which is still going on.");

        doUpdate(address);
    }

    private void doUpdate(InetAddress address) throws UpdaterException, SameIpException {
        try {
            beginTransaction();
        } catch (UpdaterException e) {
            throw registerPermanentFailure(e);
        }

        try {
            sendAddress(address);
            activeAddress = address;
            updateDate = new Instant();
            cFailures = 0;
        } catch (SameIpException e) {
            activeAddress = address;
            updateDate = new Instant();
        } catch (ProviderMaintenanceException e) {
            retryAfter = new Instant().plus(e.getMaintenanceDuration());
            throw registerTransientFailure(e);
        } catch (ProviderException e) {
            if (e.getFurtherAction() == ProviderException.FurtherAction.Permanent)
                throw registerPermanentFailure(e);
            else
                throw registerTransientFailure(e);
        } catch (UpdaterException e) {
            throw registerTransientFailure(e);
        } catch (Exception e) {
            throw registerPermanentFailure(new UpdaterException("Unexpected error", e));
        } finally {
            commitTransaction();
        }
    }

    private void beginTransaction() throws UpdaterException {
        if (transactionState == TransactionState.RUNNING) {
            throw new UpdaterException(
                    "The saving of the result of a previous update failed. "
                            + "Correct the underlying problem manually, then "
                            + "run a forced update. "
                            + "A forced update resets the domain and restarts "
                            + "automatic updates.");
        }
        transactionState = TransactionState.RUNNING;
        store.save();
    }

    protected abstract void sendAddress(InetAddress address)
            throws SameIpException, ProviderException, UpdaterException;

    private UpdaterException registerPermanentFailure(UpdaterException e) {
        cFailures++;
        health = Health.PERMANENT_FAILURE;
        return new UpdaterException(
                "The update failed. This is a permanent failure. "
                        + "No further IP address update attempts will be done. "
                        + "Correct the underlying problem "
                        + "manually, then run a forced update. A forced update "
                        + "resets the domain and restarts automatic updates.",
                e);
    }

    private UpdaterException registerTransientFailure(UpdaterException e) {
        cFailures++;
        if (cFailures < MAX_ATTEMPTS) {
            return new UpdaterException(
                    "Update failed. It seems that the failure is temporary. "
                            + "The update will be retried later.", e);
        } else {
            health = Health.PERMANENT_FAILURE;
            return new UpdaterException(
                    "There were too many transient failures. "
                            + "No further IP address update attempts will be done. "
                            + "Correct the underlying problem "
                            + "manually, then run a forced update. A forced update "
                            + "resets the domain and restarts automatic updates.",
                    e);
        }
    }

    private void commitTransaction() {
        transactionState = TransactionState.IDLE;
        store.save();
    }

    /**
     * Sends the new address to the service unconditionally.
     */
    @Override
    public void force(InetAddress address) throws UpdaterException,
            SameIpException {
        reset();

        doUpdate(address);
    }

    private void reset() {
        transactionState = TransactionState.IDLE;
        health = Health.HEALTHY;
        retryAfter = null;
        cFailures = 0;
    }

    @Override
    public void saveState(Element element) {
        if (transactionState != TransactionState.IDLE)
            element.setAttribute("transactionState",
                    transactionState.toString());
        if (health != Health.HEALTHY)
            element.setAttribute("health", health.toString());
        if (retryAfter != null)
            element.setAttribute("retryAfter",
                    ISO_TIME_FORMAT.print(retryAfter));
        if (activeAddress != null)
            element.setAttribute("address", activeAddress.getHostAddress());
        if (updateDate != null) {
            element.setAttribute("lastUpdate",
                    ISO_TIME_FORMAT.print(updateDate));
        }
        if (cFailures != 0)
            element.setAttribute("failures", String.valueOf(cFailures));
    }

    @Override
    public void loadState(Element element) {
        try {
            if (element.hasAttribute("transactionState"))
                transactionState =
                        TransactionState.valueOf(element
                                .getAttribute("transactionState"));
            if (element.hasAttribute("health"))
                health = Health.valueOf(element.getAttribute("health"));
            if (element.hasAttribute("retryAfter"))
                retryAfter =
                        ISO_TIME_FORMAT.parseDateTime(
                                element.getAttribute("retryAfter")).toInstant();
            if (element.hasAttribute("address"))
                activeAddress =
                        InetAddress.getByName(element.getAttribute("address"));
            if (element.hasAttribute("lastUpdate")) {
                updateDate =
                        ISO_TIME_FORMAT.parseDateTime(
                                element.getAttribute("lastUpdate")).toInstant();
            }
            if (element.hasAttribute("failures")) {
                cFailures = Integer.valueOf(element.getAttribute("failures"));
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @category GETSET
     */
    public void setName(String id) {
        this.name = id;
    }

    /**
     * @category GETSET
     */
    public String getName() {
        return name;
    }

    /**
     * @category GETSET
     */
    @Override
    public void setStore(Store store) {
        this.store = store;
    }

    protected enum TransactionState {
        IDLE, RUNNING;
    }

    protected enum Health {
        HEALTHY,
        /**
         * A permanent error condition occurred, no automatic IP address updates
         * should be sent, until a successful, forced IP address update.
         */
        PERMANENT_FAILURE;
    }
}