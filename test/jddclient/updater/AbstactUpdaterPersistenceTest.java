package jddclient.updater;

import static jddclient.ExampleAddress.IP;
import static jddclient.TestUtil.*;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jddclient.updater.AbstractUpdater.Health;
import jddclient.updater.AbstractUpdater.TransactionState;
import mockit.Tested;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AbstactUpdaterPersistenceTest {

    @Tested
    private AbstractUpdater updater1;
    
    @Tested
    private AbstractUpdater updater2;

    private Element element;

    private Instant now;
    private java.time.Instant now2;
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
        now2 = calendar.toInstant();
        calendar.set(2010, 2, 22, 17, 00);
        later = new Instant(calendar);
    }

    @Test
    public void testSaveAndLoad() {
        setField(updater1, "transactionState", TransactionState.RUNNING);
        setField(updater1, "health", Health.PERMANENT_FAILURE);
        setField(updater1, "retryAfter", later);
        setField(updater1, "activeAddress", IP);
        setField(updater1, "updateDate", now);
        setField(updater1, "firstFailure", now2);
        setField(updater1, "cFailures", 4);
        updater1.saveState(element);

        updater2.loadState(element);
        assertEquals(TransactionState.RUNNING, getField(updater2, "transactionState"));
        assertEquals(Health.PERMANENT_FAILURE, getField(updater2, "health"));
        assertEquals(later, getField(updater2, "retryAfter"));
        assertEquals(IP, getField(updater2, "activeAddress"));
        assertEquals(now, getField(updater2, "updateDate"));
        assertEquals((Object) 4, getField(updater2, "cFailures"));
        assertEquals(now2, getField(updater2, "firstFailure"));
    }
}
