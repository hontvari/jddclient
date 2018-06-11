package jddclient.run;

import jddclient.Client;
import joptsimple.OptionSet;

/**
 * This class is the command line launcher. The other launcher is the
 * {@link ApacheJsvc} daemon launcher.
 */
public class Start {

    public static void main(String[] args) throws Exception {
        Initializer initializer = new Initializer();
        initializer.initialize(args);

        Client client = initializer.client;
        OptionSet options = initializer.options;

        if (options.has("daemon")) {
            Daemon daemon = client.getDaemon();
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(daemon));
            client.run();
            daemon.start();
        } else {
            client.run();
            System.exit(0);
        }
    }

    private static class ShutdownHook extends Thread {

        private final Daemon daemon;

        public ShutdownHook(Daemon daemon) {
            this.daemon = daemon;
        }

        @Override
        public void run() {
            daemon.stop();
        }

    }
}
