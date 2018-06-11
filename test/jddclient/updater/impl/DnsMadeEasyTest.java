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
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class DnsMadeEasyTest {

    @Mocked
    private HttpGetter httpGetter;

    @Mocked
    private Store store;

    private DnsMadeEasy updater;

    @Before
    public void initialize() {
        updater = new DnsMadeEasy();
        updater.setStore(store);
    }

    @Test
    public final void testGoodResponse() throws IOException, ProviderException,
            SameIpException, UpdaterException {
        new Expectations() {
            {
                httpGetter.downloadDocument((URL) any, anyInt);
                result = "success";
            }
        };

        updater.sendAddress(ExampleAddress.IP);
    }

    @Test(expected = SameIpException.class)
    public final void testNoChange() throws IOException, ProviderException,
            SameIpException, UpdaterException {
        new Expectations() {
            {
                httpGetter.downloadDocument((URL) any, anyInt);
                result = "1000001 error-record-ip-same";
            }
        };

        updater.sendAddress(ExampleAddress.IP);
    }

    @Test(expected = ProviderException.class)
    public final void testNoChangeMixedWithError() throws IOException,
            ProviderException, SameIpException, UpdaterException {
        new Expectations() {
            {
                httpGetter.downloadDocument((URL) any, anyInt);
                result = "1000000 error-record-invalid\r\n"
                                + "1000001 error-record-ip-same";
            }
        };

        updater.sendAddress(ExampleAddress.IP);
    }
}
