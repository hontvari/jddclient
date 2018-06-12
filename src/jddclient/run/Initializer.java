package jddclient.run;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jddclient.Client;
import jddclient.Store;
import jddclient.Uplink;
import jddclient.XmlStateManager;
import jddclient.configuration.Configurator;
import jddclient.detector.Detector;
import jddclient.updater.Updater;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;

/**
 * It parses command line and sets up the object graph accordingly.
 */
class Initializer {
    private OptionParser parser;
    public OptionSet options;
    private OptionSpec<File> configurationFileOption;
    private File configurationFile;
    public Client client;

    /**
     * After this method completed the root of the object graph, the
     * {@link #client} field, is set, as well as the {@link #options} field
     * which may be needed by the launcher. It calls System.exit() and does not
     * return if only help was requested or the command line was invalid. In
     * both cases the user has already received the necessary message.
     */
    public void initialize(String[] args) throws IOException, JoranException {
        checkLogger();
        setupCommandLineParser();

        try {
            options = parser.parse(args);
            configurationFile = configurationFileOption.value(options);
        } catch (OptionException e) {
            System.err.println("jddclient: " + e.getMessage());
            System.err.println("Try '--help' for more information.");
            System.exit(1);
            return;
        }

        if (options.has("isDefaultConfiguration")) {
            boolean isDefaultUsed =
                    !options.has("configuration") && !options.has("help");
            System.exit(isDefaultUsed ? 0 : 1);
        }

        if (options.has("help")) {
            printHelp();
            System.exit(0);
            return;
        }

        client = new Configurator().configure(configurationFile);
        completeWiring();
    }

    private void checkLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (!new StatusUtil(lc).isWarningOrErrorFree(0)) {
            throw new RuntimeException(
                    "Logback configuration has errors or warnings, see console");
        }
    }

    private void setupCommandLineParser() {
        parser = new OptionParser();

        configurationFileOption =
                parser.acceptsAll(asList("configuration"),
                        "read configuration from File").withRequiredArg()
                        .ofType(File.class).defaultsTo(configFileDefaultPath());
        parser.acceptsAll(asList("force"),
                "force an update even if the update may be unnecessary. "
                        + "Reset any error status.");
        parser.acceptsAll(asList("query"), "print ip addresses and exit");
        parser.acceptsAll(asList("daemon"),
                "start in daemon mode, do not exit but periodically "
                        + "check and update ip addresses");
        parser.acceptsAll(asList("quiet"),
                "log 'update skipped' messages on debug level only");
        parser.acceptsAll(asList("noquiet"),
                "log 'update skipped' messages even in daemon mode");
        parser.accepts("isDefaultConfiguration",
                "test if the default configuration file were used and exit. "
                        + "Exit code is 0 if yes.");
        parser.acceptsAll(asList("help"), "display this help and exit");
    }

    private static File configFileDefaultPath() {
        File configFileDefaultDir = new File("/etc/jddclient");
        if (System.getProperty("os.name").startsWith("Windows")) {
            String envString = System.getenv("ALLUSERSPROFILE");
            if (envString != null)
                configFileDefaultDir = new File(envString, "jddclient");
        }
        File configFileDefaultPath =
                new File(configFileDefaultDir, "jddclient.conf.xml");
        return configFileDefaultPath;
    }

    private void printHelp() throws IOException {
        System.out
                .println("Retrieve and send IP address to a dynamic DNS provider.");
        System.out.println();
        parser.printHelpOn(System.out);
    }

    private void completeWiring() {
        client.setOptions(options);
        List<Updater> updaters = new ArrayList<Updater>();
        for (Uplink uplink : client.getUplinks()) {
            for (Detector detector : uplink.getDetectors())
                detector.initialize();
            updaters.addAll(uplink.getUpdaters());
        }
        Store store = client.getStore();
        store
                .setXmlStateManager(new XmlStateManager(
                        createUpdaterMap(updaters)));
        store.initialize();
        for (Updater updater : updaters) {
            updater.setStore(store);
        }
        if (client.getDaemon() != null)
            client.getDaemon().setClient(client);
    }

    private LinkedHashMap<String, Updater> createUpdaterMap(
            List<Updater> updaters) {
        LinkedHashMap<String, Updater> result =
                new LinkedHashMap<String, Updater>();
        for (Updater updater : updaters) {
            Updater existingUpdater = result.put(updater.getName(), updater);
            if (existingUpdater != null)
                throw new RuntimeException("Updater name '" + updater.getName()
                        + "' is not unique. "
                        + "You have to fix this in configuration file.");

        }
        return result;
    }

}
