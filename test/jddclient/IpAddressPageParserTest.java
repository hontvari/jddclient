package jddclient;

import static jddclient.ExampleAddress.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.CharBuffer;

import jddclient.detector.IpAddressPageParser;

import org.junit.Test;

public class IpAddressPageParserTest {

    @Test
    public void testParse() throws IOException {
        CharBuffer buffer = CharBuffer.wrap("192.0.2.0");
        IpAddressPageParser page = new IpAddressPageParser();
        InetAddress address = page.parse(buffer);
        assertEquals(IP, address);
    }

    @Test
    public void testParseWithSkip() throws IOException {
        CharBuffer reader =
                CharBuffer.wrap("192.0.2.255 here comes the real: 192.0.2.0");
        IpAddressPageParser page = new IpAddressPageParser();
        page.setSkip("here");
        InetAddress address = page.parse(reader);
        assertEquals(IP, address);
    }
}
