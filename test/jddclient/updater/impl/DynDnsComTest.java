package jddclient.updater.impl;

import java.io.IOException;
import java.net.URL;

import jddclient.ExampleAddress;
import jddclient.Store;
import jddclient.updater.ProviderException;
import jddclient.updater.SameIpException;
import jddclient.updater.UpdaterException;
import jddclient.util.HttpGetter;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;

public class DynDnsComTest {

    @Mocked
    private HttpGetter httpGetter;

    @Mocked
    private Store store;

    private DynDnsCom updater;

    @Before
    public void initialize() {
        updater = new DynDnsCom();
        updater.setStore(store);
    }

    @Test
    public final void testGoodResponse() throws IOException, UpdaterException,
            SameIpException {
        new Expectations() {
            {
                httpGetter.downloadDocument((URL) any, anyInt);
                result = "good 192.0.2.0";
            }
        };

        updater.sendAddress(ExampleAddress.IP);
    }

    @Test(expected = SameIpException.class)
    public final void testNoChangeResponse() throws IOException,
            UpdaterException, SameIpException {
        new Expectations() {
            {
                httpGetter.downloadDocument((URL) any, anyInt);
                result = "nochg 192.0.2.0";
            }
        };

        updater.sendAddress(ExampleAddress.IP);
    }

    @Test(expected = ProviderException.class)
    public final void testUnknownResponse() throws IOException,
            UpdaterException, SameIpException {
        new Expectations() {
            {
                httpGetter.downloadDocument((URL) any, anyInt);
                result = "XXXXX 192.0.2.0";
            }
        };

        updater.sendAddress(ExampleAddress.IP);
    }
}
