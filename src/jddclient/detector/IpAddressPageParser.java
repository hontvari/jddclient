package jddclient.detector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpAddressPageParser {
    private final Logger logger = LoggerFactory.getLogger(IpAddressPageParser.class);
    private static final Pattern ip4AddressPattern =
            Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
    private static final int EXAMINED_LENGTH = 16000;

    @Nullable
    private String skip;

    public InetAddress parse(CharSequence buffer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(buffer);
        int iStart = positionAfterSkip(stringBuilder);
        String address = extractAddress(stringBuilder, iStart);
        return InetAddress.getByName(address);
    }

    private int positionAfterSkip(StringBuilder stringBuilder) {
        int iStart;
        if (skip == null) {
            iStart = 0;
        } else {
            iStart = stringBuilder.indexOf(skip);
            if (iStart == -1)
                throw new RuntimeException(
                        "Skip string is not found within the first "
                                + EXAMINED_LENGTH + " characters");
            iStart += skip.length();
        }
        logger.debug("Skip to character position " + iStart);
        return iStart;
    }

    private String extractAddress(StringBuilder stringBuilder, int iStart) {
        Matcher matcher =
                ip4AddressPattern.matcher(stringBuilder.subSequence(iStart,
                        stringBuilder.length()));
        if (!matcher.find())
            throw new RuntimeException(
                    "IP address was not found in the examined region");
        String address = matcher.group();
        logger.debug("Address found: " + address);
        return address;
    }

    public int getExaminedLength() {
        return EXAMINED_LENGTH;
    }

    /**
     * @category GETSET
     */
    public void setSkip(String skip) {
        this.skip = skip;
    }

}
