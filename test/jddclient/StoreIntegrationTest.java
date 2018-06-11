package jddclient;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import jddclient.updater.Updater;
import jddclient.updater.impl.DnsMadeEasy;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class StoreIntegrationTest {

    @Test
    public void testSave() throws IOException {
        Store store = new Store();
        File file = File.createTempFile(getClass().getSimpleName(), ".xml");
        file.deleteOnExit();
        store.setFile(file.toString());

        LinkedHashMap<String, Updater> updaters =
                new LinkedHashMap<String, Updater>();
        updaters.put("router", new DnsMadeEasy());
        store.setXmlStateManager(new XmlStateManager(updaters));

        store.save();

        fail("Not yet implemented");
    }

    @Test
    public void testLoad() {
        fail("Not yet implemented");
    }

}
