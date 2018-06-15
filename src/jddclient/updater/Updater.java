package jddclient.updater;

import java.net.InetAddress;

import jddclient.Store;

import org.w3c.dom.Element;

/**
 * An updater notifies a DNS provider about the new IP address of a connection.
 */
public interface Updater {
    String getName();
    void setStore(Store store);
    void saveState(Element element);
    void loadState(Element element);

    void update(InetAddress address) throws UpdaterException,
            SameIpException, SkippedUpdateException;

    void force(InetAddress address) throws UpdaterException, SameIpException;
    void initialize();
}
