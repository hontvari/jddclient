package jddclient.configuration;

import jddclient.Client;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class ConfigurationAction extends Action {

    @Override
    public void begin(InterpretationContext ec, String name,
            Attributes attributes) throws ActionException {
        Client client = new Client();
        ec.getContext().putObject("client", client);
        ec.pushObject(client);
    }

    @Override
    public void end(InterpretationContext ec, String name)
            throws ActionException {
        ec.popObject();
    }

}
