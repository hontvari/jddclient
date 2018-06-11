package jddclient;

import java.util.LinkedHashMap;

import javax.xml.parsers.ParserConfigurationException;

import jddclient.updater.AbstractUpdater;
import jddclient.updater.Updater;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(JMockit.class)
public class XmlStateManagerTest {
    @Mocked
    AbstractUpdater updater1;
    @Mocked
    AbstractUpdater updater2;

    @Test
    public void testSaveAndRestoreTwoUpdaters()
            throws ParserConfigurationException {
        LinkedHashMap<String, Updater> updaters =
                new LinkedHashMap<String, Updater>();
        updaters.put("host1", updater1);
        updaters.put("host2", updater2);
        XmlStateManager xmlStateManager = new XmlStateManager(updaters);
        
        Document state = xmlStateManager.saveState();
        
        new Expectations() {
            {
                updater1.loadState((Element) any);
                updater2.loadState((Element) any);
            }
        };
        
        xmlStateManager.restoreState(state);
    }
}
