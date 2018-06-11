package jddclient.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpGetter {
    private final Logger logger = LoggerFactory.getLogger(HttpGetter.class);
    private final Logger documentBodyLogger = LoggerFactory
            .getLogger(HttpGetter.class.getName() + ".body");
    /**
     * If null, then no HTTP authentication will be performed.
     */
    @Nullable
    private final String user;
    /**
     * It is required if {@link #user} is supplied.
     */
    @Nullable
    private final String password;

    /**
     * null means the date is not known
     */
    private Instant lastModified;

    private HttpURLConnection connection;

    /**
     * @param user
     *            if it is null then no HTTP authentication will be performed
     */
    public HttpGetter(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public HttpGetter() {
        this.user = null;
        this.password = null;
    }

    public String downloadDocument(URL url, int maxLength) throws IOException {
        try {
            logger.debug("Downloading {}", url);
            setupConnection(url);
            connection.connect();
            checkOkResponse();
            saveLastModified();
            return readWithLimit(maxLength);
        } finally {
            cleanup();
        }
    }

    private void setupConnection(URL url) throws IOException {
        Authenticator.setDefault(user == null ? null : new AuthenticatorImpl());
        connection = (HttpURLConnection) url.openConnection();
        connection.setAllowUserInteraction(false);
        connection.setUseCaches(false);
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", constructUserAgentString());
    }

    private String constructUserAgentString() {
        String dyndnsInfo = "jddclient/1.0.0";
        String osComment =
                "(" + System.getProperty("os.name") + " "
                        + System.getProperty("os.version") + " "
                        + System.getProperty("os.arch") + ")";
        String javaInfo = "Java/" + System.getProperty("java.version");
        return dyndnsInfo + " " + osComment + " " + javaInfo;
    }

    private void checkOkResponse() throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code returned: " + responseCode
                    + " " + connection.getResponseMessage());
        }
    }

    private void saveLastModified() {
        lastModified =
                connection.getLastModified() == 0 ? null : new Instant(
                        connection.getLastModified());
    }

    private String readWithLimit(int maxLength) throws IOException,
            UnsupportedEncodingException {
        CharBuffer buffer = CharBuffer.allocate(maxLength);
        fillBuffer(buffer);
        buffer.flip();
        return buffer.toString();
    }

    private void fillBuffer(CharBuffer buffer) throws IOException,
            UnsupportedEncodingException {
        Charset charset = determineCharset();
        InputStream inputStream = connection.getInputStream();
        try {
            Reader reader = new InputStreamReader(inputStream, charset);
            int cAvailableInBuffer = buffer.remaining();
            int cRead;
            while (-1 != (cRead = reader.read(buffer))) {
                if (cRead == 0) {
                    logger.debug("The document is longer then "
                            + cAvailableInBuffer
                            + " characters. Later characters are not "
                            + "examined.");
                    break;
                }
            }
            if (documentBodyLogger.isDebugEnabled()) {
                documentBodyLogger.debug("Document body: "
                        + System.getProperty("line.separator")
                        + buffer.asReadOnlyBuffer().flip());
            }
        } finally {
            inputStream.close();
        }
    }

    private Charset determineCharset() {
        String contentType = connection.getContentType();
        logger.debug("Content type: {}", contentType);
        if (contentType == null) {
            logger.debug("Content type is missing from the HTTP response. "
                    + "HTTP response headers: {}", connection.getHeaderFields());
            return Charset.forName("ISO-8859-1");
        }
        ContentTypeParser contentTypeParser =
                new ContentTypeParser(contentType);
        contentTypeParser.parse();
        String charset =
                contentTypeParser.charset == null ? "ISO-8859-1"
                        : contentTypeParser.charset;
        return Charset.forName(charset);
    }

    private void cleanup() {
        Authenticator.setDefault(null);
    }

    /**
     * Returns null if the document has not been changed since the supplied
     * date.
     * 
     * @param null means that the document must be downloaded independently from
     *        its modification date.
     * 
     */
    public String downloadDocumentIfChanged(URL url, int maxLength,
            Instant lastModified) throws IOException {
        try {
            logger.debug("Downloading if modified: {}", url);
            setupConnection(url);
            if (lastModified != null)
                connection.setIfModifiedSince(lastModified.getMillis());
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                logger.debug("Document status is 'Not modified', it was not sent.");
                return null;
            }
            checkOkResponse();
            saveLastModified();
            logger.debug("Last modified: "
                    + (lastModified == null ? "-" : lastModified.toDateTime()
                            .toString()));
            return readWithLimit(maxLength);
        } finally {
            cleanup();
        }
    }

    /**
     * @category GETSET
     */
    public Instant getLastModified() {
        return lastModified;
    }

    private class AuthenticatorImpl extends Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            logger.debug("Authentication credentials requested");
            return (new PasswordAuthentication(user, password.toCharArray()));
        }
    }

}
