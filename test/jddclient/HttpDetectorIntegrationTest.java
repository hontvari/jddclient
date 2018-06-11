package jddclient;

import java.io.IOException;
import java.net.InetAddress;

import jddclient.detector.IpAddressPageParser;
import jddclient.detector.OfflineException;
import jddclient.detector.HttpDetector;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class HttpDetectorIntegrationTest extends IntegrationTest {

    @Test
    public void testRun() throws IOException, OfflineException {
        HttpDetector detector = new HttpDetector();
        detector.setUrl(prop("url"));
        IpAddressPageParser ipAddressPageParser = new IpAddressPageParser();
        ipAddressPageParser.setSkip(prop("skip"));
        detector.setPageParser(ipAddressPageParser);
        detector.setUser(prop("user"));
        detector.setPassword(prop("password"));
        detector.initialize();
        InetAddress address = detector.detect();
        System.out.println(address);
    }

}
