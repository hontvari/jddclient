package jddclient.detector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.annotation.Nullable;

import jddclient.util.HttpGetter;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpDetector downloads a HTTP document, for example a HTML page or a plain
 * text document and finds the IP address within the text.
 */
public class HttpDetector extends AbstractDetector {
    private static final InetAddress NULL_ADDRESS;

    private final Logger logger = LoggerFactory.getLogger(HttpDetector.class);
    private URL url;
    /**
     * If null, then no HTTP authentication will be performed.
     */
    @Nullable
    private String user;
    /**
     * It is required if {@link #user} is supplied.
     */
    @Nullable
    private String password;

    private IpAddressPageParser pageParser = new IpAddressPageParser();

    private Instant lastModified;

    private InetAddress lastAddress;

    static {
        try {
            NULL_ADDRESS = InetAddress.getByName("0.0.0.0");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize() {
        if (user == null && password == null)
            return;
        if (user != null && password == null)
            throw new RuntimeException(
                    "Invalid configuration, user is configured but password is not");
        if (user == null && password != null)
            throw new RuntimeException(
                    "Invalid configuration, password is configured but user is not");
    }

    @Override
    public InetAddress detect() throws IOException, OfflineException {
        HttpGetter httpGetter = new HttpGetter(user, password);
        String body;
        if (lastAddress != null) {
            body =
                    httpGetter.downloadDocument(url, pageParser
                            .getExaminedLength());
        } else {
            body =
                    httpGetter.downloadDocumentIfChanged(url,
                            pageParser.getExaminedLength(), lastModified);
            if (body == null) {
                logger.debug("Document was not modified, "
                        + "which means that the address has not been changed.");
                return lastAddress;
            }
        }
        lastModified = httpGetter.getLastModified();
        lastAddress = pageParser.parse(body);
        if (addressIsIndicatingOfflineState(lastAddress))
            throw new OfflineException();
        return lastAddress;
    }

    private boolean addressIsIndicatingOfflineState(InetAddress address) {
        return address.equals(NULL_ADDRESS);
    }

    /**
     * @category GETSET
     */
    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @category GETSET
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @category GETSET
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @category GETSET
     */
    public void setPageParser(IpAddressPageParser ipAddressPageParser) {
        this.pageParser = ipAddressPageParser;
    }

    @Override
    public String getName() {
        if (detectorName == DEFAULT_NAME)
            return url.getHost();
        else
            return detectorName;
    }

};
