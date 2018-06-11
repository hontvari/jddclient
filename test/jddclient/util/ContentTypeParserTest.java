package jddclient.util;

import static org.junit.Assert.*;
import jddclient.util.ContentTypeParser;

import org.junit.Test;

public class ContentTypeParserTest {

    @Test
    public void testParseMediaType() {
        ContentTypeParser parser = new ContentTypeParser("text/html");
        parser.parse();
        assertEquals("text", parser.type);
        assertEquals("html", parser.subtype);
        assertEquals("text/html", parser.mediaType);
        assertNull(parser.charset);
    }

    @Test
    public void testParseCharset() {
        ContentTypeParser parser =
                new ContentTypeParser("text/html; charset=UTF-8");
        parser.parse();
        assertEquals("text/html", parser.mediaType);
        assertEquals("UTF-8", parser.charset);
    }

    @Test
    public void testParseQuotedCharset() {
        ContentTypeParser parser =
                new ContentTypeParser("text/html; charset=\"UTF-8\"");
        parser.parse();
        assertEquals("UTF-8", parser.charset);
    }

    @Test
    public void testParseMoreParam() {
        ContentTypeParser parser =
                new ContentTypeParser(
                        "text/plain; Format=Flowed; charset=UTF-8");
        parser.parse();
        assertEquals("UTF-8", parser.charset);
    }

    @Test
    public void testParseCaseInsensitive() {
        ContentTypeParser parser =
                new ContentTypeParser("text/plain; CHARSET=UTF-8");
        parser.parse();
        assertEquals("UTF-8", parser.charset);
    }

    @Test
    public void testParseLinearWhitespace() {
        ContentTypeParser parser =
                new ContentTypeParser("text/plain;\r\n CHARSET=UTF-8");
        parser.parse();
        assertEquals("UTF-8", parser.charset);
    }

    @Test
    public void testParseQuotedStringEscape() {
        ContentTypeParser parser =
                new ContentTypeParser("text/plain; CHARSET=\"UTF\\-8\"");
        parser.parse();
        assertEquals("UTF-8", parser.charset);
    }

    @Test
    public void testParseQuotedStringLinearWhitespace() {
        ContentTypeParser parser =
                new ContentTypeParser("text/plain; CHARSET=\"UTF\r\n\t-8\"");
        parser.parse();
        assertEquals("UTF -8", parser.charset);
    }
}
