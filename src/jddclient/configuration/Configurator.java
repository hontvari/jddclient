package jddclient.configuration;

import java.io.File;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.NestedBasicPropertyIA;
import ch.qos.logback.core.joran.action.NestedComplexPropertyIA;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;
import jddclient.Client;

public class Configurator extends GenericConfigurator {
    public Client configure(File configurationFile) throws JoranException {
        Context context = new ContextBase();
        Configurator configurator = new Configurator();
        configurator.setContext(context);
        configurator.doConfigure(configurationFile);
        checkStatus(context.getStatusManager());
        return (Client) context.getObject("client");
    }

    private void checkStatus(StatusManager statusManager) {
        if (new StatusUtil(statusManager).isWarningOrErrorFree(0))
            return;
        StringBuilder buffer = new StringBuilder();
        for (Status status : statusManager.getCopyOfStatusList())
            StatusPrinter.buildStr(buffer, "", status);
        String lineSeparator = System.getProperty("line.separator");
        throw new RuntimeException("Invalid client file, status messages: "
                + lineSeparator + buffer);
    }

    @Override
    protected void addImplicitRules(Interpreter interpreter) {
        NestedComplexPropertyIA nestedComplexPropertyIA =
                new NestedComplexPropertyIA(getBeanDescriptionCache());
        nestedComplexPropertyIA.setContext(context);
        interpreter.addImplicitAction(nestedComplexPropertyIA);

        NestedBasicPropertyIA nestedBasicIA = new NestedBasicPropertyIA(getBeanDescriptionCache());
        nestedBasicIA.setContext(context);
        interpreter.addImplicitAction(nestedBasicIA);
    }

    @Override
    protected void addInstanceRules(RuleStore rs) {
        rs.addRule(new ElementSelector("/configuration"), new ConfigurationAction());
    }

}
