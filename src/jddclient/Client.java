package jddclient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jddclient.detector.Detector;
import jddclient.detector.OfflineException;
import jddclient.run.Daemon;
import jddclient.updater.PermanentSkippedUpdateException;
import jddclient.updater.SameIpException;
import jddclient.updater.SkippedUpdateException;
import jddclient.updater.Updater;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client is responsible for the primary function of jddclient: checking and
 * updating an uplink. It is also the root object of the configuration file.
 */
public class Client {

    private final Logger logger = LoggerFactory.getLogger(Client.class);
    private OptionSet options;
    private Store store = new Store();
    private Daemon daemon;
    private final List<Uplink> uplinks =
            new ArrayList<Uplink>();

    /**
     * Executes the command given in the command line options.
     * 
     * If the "daemon" option is given, then it only initialize itself, but it
     * does not execute any command.
     * <p>
     * Note: Starting the daemon is the task of the launcher. The reason for
     * this is the following. The launcher is necessarily responsible for
     * stopping the daemon, because the way to receive the stop signal depends
     * on the type of the launch. The daemon must be synchronized externally,
     * which is only possible if the same object is responsible for both
     * starting and stopping it.
     */
    public void run() throws Exception {
        store.load();

        if (options.has("daemon")) {
            // do nothing, launcher must start the daemon
        } else if (options.has("query")) {
            query();
        } else {
            update();
        }
    }

    private void query() throws IOException {
        for (Uplink uplink : uplinks) {
            for (Detector detector : uplink.getDetectors()) {
                String heading =
                        uplink.getName() + "\t" + detector.getName()
                                + "\t";
                InetAddress address;
                try {
                    address = detector.detect();
                    System.out.println(heading + address.getHostAddress());
                } catch (OfflineException e) {
                    System.out.println(heading + "OFFLINE");
                } catch (Exception e) {
                    System.out.println(heading + "FAILED");
                    e.printStackTrace();
                }
            }
        }
    }

    public void update() {
        for (Uplink uplink : uplinks) {
            updateUplink(uplink);
        }
    }

    private void updateUplink(Uplink uplink) {
        if (Thread.currentThread().isInterrupted()) {
            logger.debug("Update has been interrupted");
            return;
        }
        InetAddress address = detectIpAddress(uplink);
        if (address == null)
            return;

        for (Updater updater : uplink.getUpdaters()) {
            if (Thread.currentThread().isInterrupted()) {
                logger.debug("Update has been interrupted");
                return;
            }
            String contextInformation =
                    "Interface '" + uplink.getName() + "', "
                            + address.getHostAddress() + ", target '"
                            + updater.getName() + "': ";
            try {
                if (updateIsForced()) {
                    updater.force(address);
                } else {
                    updater.update(address);
                }
                logger.info(contextInformation + "Update successful.");
            } catch (PermanentSkippedUpdateException e) {
                logger.error(contextInformation + "Target was skipped. " + e.getMessage());
            } catch (SkippedUpdateException e) {
                String message =
                        contextInformation + "Target was skipped. "
                                + e.getMessage();
                if (isQuiet())
                    logger.debug(message);
                else
                    logger.info(message);
            } catch (SameIpException e) {
                logger.warn(contextInformation
                        + "Provider reported that the same IP address "
                        + "was already set. Providers may consider such "
                        + "repeated updates as abusive.", e);
            } catch (Exception e) {
                logger.error(contextInformation
                        + "Failed to send the IP address to the DNS provider.",
                        e);
            }
        }
    }

    private InetAddress detectIpAddress(Uplink uplink) {
        for (Detector detector : uplink.getDetectors()) {
            try {
                logger.debug("Querying uplink address '"
                        + uplink.getName() + "' using "
                        + detector.getName() + "...");
                return detector.detect();
            } catch (OfflineException e) {
                logger.debug(uplink.getName() + " detector "
                        + detector.getName() + " is offline");
                continue;
            } catch (Exception e) {
                logger.error(
                        "Querying uplink address '" + uplink.getName()
                                + "' using " + detector.getName()
                                + " is failed.", e);
                continue;
            }
        }
        return null;
    }

    private boolean updateIsForced() {
        return options.has("force") && !options.has("daemon");
    }

    private boolean isQuiet() {
        if (options.has("noquiet"))
            return false;
        return options.has("daemon") || options.has("quiet");
    }

    /**
     * @category GETSET
     */
    public void setOptions(OptionSet options) {
        this.options = options;
    }

    /**
     * @category GETSET
     */
    public void setStore(Store store) {
        this.store = store;
    }

    /**
     * @category GETSET
     */
    public Store getStore() {
        return store;
    }

    /**
     * @category GETSET
     */
    public Daemon getDaemon() {
        return daemon;
    }

    /**
     * @category GETSET
     */
    public void setDaemon(Daemon daemon) {
        this.daemon = daemon;
    }

    /**
     * @category GETSET
     */
    public void addUplink(Uplink uplink) {
        uplinks.add(uplink);
    }

    /**
     * @category GETSET
     */
    public List<Uplink> getUplinks() {
        return Collections.unmodifiableList(uplinks);
    }
}