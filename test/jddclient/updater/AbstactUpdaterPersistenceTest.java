package jddclient.updater;

import static jddclient.ExampleAddress.IP;
import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jddclient.updater.AbstractUpdater.Health;
import jddclient.updater.AbstractUpdater.TransactionState;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


@RunWith(JMockit.class)
public class AbstactUpdaterPersistenceTest {
    @Mocked("")
    private AbstractUpdater updater1;

    @Mocked("")
    private AbstractUpdater updater2;

// @Mocked
    // private AbstractUpdater updater1;
    // @Mocked
    // private AbstractUpdater updater2;

    private Element element;

    private Instant now;
    private Instant later;

    @Before
    public void initialize() throws ParserConfigurationException {
        DocumentBuilder documentBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        element = document.createElement("updater");
        Calendar calendar =
                Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        calendar.clear();
        calendar.set(2010, 2, 22, 16, 30);
        now = new Instant(calendar);
        calendar.set(2010, 2, 22, 17, 00);
        later = new Instant(calendar);
    }

    @Test
    public void testSaveAndLoad() {
        // new Expectations(AbstractUpdater.class) {
        // {
        // // only used for dynamic mocking, see constructor!
        // }
        // };
        Deencapsulation.setField(updater1, TransactionState.RUNNING);
        Deencapsulation.setField(updater1, Health.PERMANENT_FAILURE);
        Deencapsulation.setField(updater1, "retryAfter", later);
        Deencapsulation.setField(updater1, IP);
        Deencapsulation.setField(updater1, "updateDate", now);
        Deencapsulation.setField(updater1, 4);
        updater1.saveState(element);

        updater2.loadState(element);
        assertEquals(TransactionState.RUNNING, Deencapsulation.getField(
                updater2, TransactionState.class));
        assertEquals(Health.PERMANENT_FAILURE, Deencapsulation.getField(
                updater2, Health.class));
        assertEquals(later, Deencapsulation.getField(updater2, "retryAfter"));
        assertEquals(IP, Deencapsulation.getField(updater2, InetAddress.class));
        assertEquals(now, Deencapsulation.getField(updater2, "updateDate"));
        assertEquals((Object) 4, Deencapsulation.getField(updater2,
                Integer.class));

    }
}
