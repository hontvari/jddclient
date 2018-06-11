package jddclient;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ShowInterfaces {

    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces =
                NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface iface = networkInterfaces.nextElement();
            System.out.println(iface.toString());
        }
    }

}
