package jddclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ExampleAddress {
    public static final InetAddress IP;
    public static final InetAddress IP1;
    public static final InetAddress IP2;
    public static final InetAddress IP3;

    static {
        try {
            IP = InetAddress.getByName("192.0.2.0");
            IP1 = InetAddress.getByName("192.0.2.1");
            IP2 = InetAddress.getByName("192.0.2.2");
            IP3 = InetAddress.getByName("192.0.2.3");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
