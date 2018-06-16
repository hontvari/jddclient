package jddclient.updater.impl;

import java.net.InetAddress;

import org.junit.Ignore;
import org.junit.Test;

import jddclient.IntegrationTest;

@Ignore
public class ConstellixIntegrationTest extends IntegrationTest {

    @Test
    public void test() throws Exception {
        Constellix u = new Constellix();
        u.setApiKey(prop("apiKey"));
        u.setSecretKey(prop("secretKey"));
        u.setDomainId(prop("domainId"));
        u.setRecordId(prop("recordId"));
        u.setRecordName(prop("recordName"));
        u.sendAddress(InetAddress.getByName("192.0.2.2"));
    }

}
