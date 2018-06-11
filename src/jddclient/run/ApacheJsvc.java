package jddclient.run;

import jddclient.Client;
import joptsimple.OptionSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes it possible to use Apache Commons Daemon jsvc to run
 * jddclient as a daemon on UNIX like operating systems. In addition to throwing
 * exceptions forward, it also logs them, so that the logback log file contains
 * the complete list of events. These exceptions will also appear in the syslog.
 */
public class ApacheJsvc {
    /**
     * The initialization of Logback must be delayed, otherwise the log file
     * would be created with owner readable permissions. At least on Ubuntu with
     * Commons Daemon 1.0.2.
     */
    private Logger logger;
    private String[] arguments;
    private Daemon daemon;

    public void init(String[] arguments) {
        this.arguments = arguments;
    }

    public void start() {
        logger = LoggerFactory.getLogger(getClass());
        try {
            Initializer initializer = new Initializer();
            initializer.initialize(arguments);

            Client client = initializer.client;
            OptionSet options = initializer.options;

            initializer.client.run();

            if (options.has("daemon")) {
                daemon = client.getDaemon();
                daemon.start();
            } else {
                System.exit(0);
            }
        } catch (RuntimeException e) {
            logger.error("Can not start daemon", e);
            throw e;
        } catch (Exception e) {
            logger.error("Can not start daemon", e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            if (daemon != null) {
                daemon.stop();
            }
        } catch (RuntimeException e) {
            logger.error("Can not stop daemon", e);
            throw e;
        } catch (Exception e) {
            logger.error("Can not stop daemon", e);
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        // nothing to do
    }
}
