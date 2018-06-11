package jddclient.updater.impl;

import static jddclient.ExampleAddress.*;
import jddclient.IntegrationTest;
import jddclient.Store;
import jddclient.updater.SameIpException;
import jddclient.updater.UpdaterException;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore
@RunWith(JMockit.class)
public class DnsMadeEasyIntegrationTest extends IntegrationTest {
    @Mocked
    private Store store;

    private DnsMadeEasy updater;

    @Before
    public void initialize() {
        updater = new DnsMadeEasy();
        updater.setStore(store);
    }

    @Test
    public void testForce() throws UpdaterException, SameIpException {
        updater.setUser(prop("user"));
        updater.setPassword(prop("password"));
        updater.addRecordIdentifier(prop("recordIdentifier"));
        // updater.addRecordIdentifier("1000000");

        updater.force(IP);
    }

}
