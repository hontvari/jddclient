package jddclient.detector;

import java.io.IOException;
import java.net.InetAddress;


/**
 * Detector is responsible to detect the current IP address of an internal or
 * external network interface, usually the IP address on which the current local
 * network is seen on the Internet.
 */
public interface Detector {
    void initialize();
    String getName();
    InetAddress detect() throws IOException, DetectorException;
}