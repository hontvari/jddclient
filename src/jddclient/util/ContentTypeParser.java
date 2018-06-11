package jddclient.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

class ContentTypeParser {
    private static final boolean[] SEPARATOR_INDEX = new boolean[127];
    private static final String SEPARATORS = "()<>@,;:\\\"/[]?={} \t";
    private static final boolean[] TOKEN_CHAR_INDEX = new boolean[127];
    private final PushbackReader in;
    public String charset;
    public String type;
    public String subtype;
    public String mediaType;

    static {
        for (char ch : SEPARATORS.toCharArray()) {
            SEPARATOR_INDEX[ch] = true;
        }
        for (int i = 32; i < 127; i++) {
            TOKEN_CHAR_INDEX[i] = !SEPARATOR_INDEX[i];
        }
    }

    public ContentTypeParser(String text) {
        in = new PushbackReader(new StringReader(text), 3);
    }

    public void parse() {
        readMediaType();
        while (true) {
            skipOptionalLWS();
            if (isEOF())
                return;
            read(';');
            skipOptionalLWS();
            readParameter();
        }

    }

    private void readMediaType() {
        StringBuilder buffer = new StringBuilder();
        type = readToken();
        buffer.append(type);
        buffer.append(readChar('/'));
        subtype = readToken();
        buffer.append(subtype);
        mediaType = buffer.toString();
    }

    private String readToken() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(readTokenChar());
        int ch;
        while (isTokenChar(ch = read())) {
            buffer.append((char) ch);
        }
        unread(ch);
        return buffer.toString();
    }

    private char readTokenChar() {
        int ch = read();
        if (!isTokenChar(ch))
            throw new RuntimeException("Token character was expected");
        return (char) ch;
    }

    private int read() {
        try {
            return in.read();
        } catch (IOException e) {
            throw new RuntimeException(); // impossible
        }
    }

    private boolean isTokenChar(int ch) {
        if (ch == -1)
            return false;
        if (ch >= TOKEN_CHAR_INDEX.length)
            return false;
        return TOKEN_CHAR_INDEX[ch];
    }

    private void unread(int ch) {
        if (ch == -1)
            return;
        try {
            in.unread((char) ch);
        } catch (IOException e) {
            throw new RuntimeException(); // impossible
        }
    }

    private int read(int ch) {
        int chActual = read();
        if (chActual != ch) {
            String expectedCharName =
                    ch == -1 ? "EOF" : String.valueOf((char) ch);
            throw new RuntimeException(expectedCharName + " were expected");
        }
        return ch;
    }

    private char readChar(char ch) {
        int chActual = read();
        if (chActual == -1 || (char) chActual != ch)
            throw new RuntimeException("'" + ch + "' were expected");
        return ch;
    }

    private void skipOptionalLWS() {
        StringBuilder buffer = new StringBuilder();
        if (isCrlf()) {
            buffer.append(readCrlf());
        }
        if (isSpOrHt()) {
            buffer.append(read());
        } else {
            unread(buffer);
        }
        int ch;
        while (isSpOrHt(ch = read())) {
            ;
        }
        unread(ch);
    }

    private boolean isCrlf() {
        int ch1 = read();
        int ch2 = read();
        boolean result = ch1 == '\r' && ch2 == '\n';
        unread(ch2);
        unread(ch1);
        return result;
    }

    private String readCrlf() {
        read('\r');
        read('\n');
        return "\r\n";
    }

    private boolean isSpOrHt() {
        int ch = read();
        boolean result = ch == ' ' || ch == '\t';
        unread(ch);
        return result;
    }

    private boolean isSpOrHt(int ch) {
        return ch == ' ' || ch == '\t';
    }

    private void unread(StringBuilder buffer) {
        try {
            in.unread(buffer.toString().toCharArray());
        } catch (IOException e) {
            throw new RuntimeException(); // impossible
        }
    }

    private boolean isEOF() {
        int ch = read();
        boolean result = ch == -1;
        unread(ch);
        return result;
    }

    private void readParameter() {
        String name = readToken();
        read('=');
        String value;
        if (is('"')) {
            value = readQuotedString();
        } else {
            value = readToken();
        }
        storeParameter(name, value);

    }

    private boolean is(int ch) {
        int chActual = read();
        boolean result = chActual == ch;
        unread(chActual);
        return result;
    }

    private String readQuotedString() {
        StringBuilder buffer = new StringBuilder();
        read('"');
        while (!is('"')) {
            if (is('\\')) {
                read();
                buffer.append(readChar());
            } else if (isLws()) {
                skipOptionalLWS();
                buffer.append(' ');
            } else {
                buffer.append(readChar());
            }
        }
        read('"');
        return buffer.toString();
    }

    private boolean isLws() {
        boolean result;
        StringBuilder buffer = new StringBuilder();
        if (isCrlf()) {
            buffer.append(readCrlf());
        }
        result = isSpOrHt();
        unread(buffer);
        return result;
    }

    private char readChar() {
        int ch = read();
        if (ch == -1)
            throw new RuntimeException("Char were expected");
        return (char) ch;
    }

    private void storeParameter(String name, String value) {
        if (name.equalsIgnoreCase("charset")) {
            charset = value;
        }
    }
}
