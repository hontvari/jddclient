package jddclient.detector;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface queries the IP address assigned to the configured network
 * interface.
 */
public class LocalNetworkInterfaceDetector extends AbstractDetector {
    private final Logger logger = LoggerFactory.getLogger(LocalNetworkInterfaceDetector.class);

    /**
     * name of the network interface
     */
    private String interfaceName;
    private boolean requireIpv4Address = true;

    @Override
    public void initialize() {
        if (interfaceName == null)
            throw new RuntimeException("Network interface name is required");
    }

    @Override
    public InetAddress detect() throws IOException, DetectorException,
            OfflineException {
        NetworkInterface iface = NetworkInterface.getByName(interfaceName);
        if (iface == null)
            throw new DetectorException("No network interface named " + interfaceName
                    + " exists, or the interface is disabled.");
        if (!iface.isUp())
            throw new DetectorException("Interface '" + interfaceName + "' is down.");
        List<InetAddress> addresses =
                Collections.list(iface.getInetAddresses());
        logger.debug("Addresses of interface {}: {}", interfaceName, addresses);
        addresses = filterAddresses(addresses);
        if (addresses.isEmpty())
            throw new OfflineException();
        logger.debug("Filtered addresses of interface {}: {}", interfaceName, addresses);
        return addresses.get(0);
    }

    private List<InetAddress> filterAddresses(List<InetAddress> addresses) {
        List<InetAddress> suitableAddresses = new ArrayList<InetAddress>();
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress()) {
                logger.debug("Skipping \"any local address\" {}, this is "
                        + "not expected on an interface anyway", address);
                continue;
            }
            if (address.isLoopbackAddress()) {
                logger.debug("Skipping loopback address {}", address);
                continue;
            }
            if (address.isLinkLocalAddress()) {
                logger.debug("Skipping link local address {}", address);
                continue;
            }
            if (address.isSiteLocalAddress()) {
                logger.debug("Skipping site local address {}", address);
                continue;
            }
            if (requireIpv4Address && !(address instanceof Inet4Address)) {
                logger.debug("Skipping non-IPv4 address {}", address);
                continue;
            }
            suitableAddresses.add(address);
        }
        return suitableAddresses;
    }

    /**
     * @category GETSET
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * @category GETSET
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

}
