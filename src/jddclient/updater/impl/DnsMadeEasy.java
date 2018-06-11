package jddclient.updater.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jddclient.updater.AbstractUpdater;
import jddclient.updater.ProviderException;
import jddclient.updater.SameIpException;
import jddclient.updater.UpdaterException;
import jddclient.util.HttpGetter;

public class DnsMadeEasy extends AbstractUpdater {
    private static final String URL_TEMPLATE =
            "https://cp.dnsmadeeasy.com/servlet/updateip?"
                    + "username={0}&password={1}&id={2}&ip={3}";
    private String user;
    private String password;
    private final List<String> recordIdentifiers = new ArrayList<String>();

    @Override
    protected void sendAddress(InetAddress address) throws SameIpException,
            ProviderException, UpdaterException {
        URL url;
        try {
            url = constructURL(address);
        } catch (MalformedURLException e) {
            throw new UpdaterException(e);
        }
        HttpGetter httpGetter = new HttpGetter();
        String result;
        try {
            result = httpGetter.downloadDocument(url, 1024).trim();
        } catch (IOException e) {
            throw new UpdaterException(e);
        }
        if (result.equals("success")) {
            return;
        } else if (result.contains("error-auth")
                || result.contains("error-auth-suspend")
                || result.contains("error-auth-voided")
                || result.contains("error-record-auth")) {
            throw new ProviderException(
                    "Provider reports a permanent failure status, "
                            + "no further IP address updates should be attempted "
                            + "before correcting the problem: " + result,
                    ProviderException.FurtherAction.Permanent);
        } else if (resultIncludesOtherThanNoChange(result)) {
            throw new ProviderException(
                    "Provider reports a transient failure status, "
                            + "IP address update should be retried later: "
                            + result, ProviderException.FurtherAction.Retry);
        } else {
            throw new SameIpException(result);
        }

    }

    private URL constructURL(InetAddress address) throws MalformedURLException {
        String recordList = formatRecordList();
        String ipAddressString = address.getHostAddress();
        String urlString =
                MessageFormat.format(URL_TEMPLATE, user, password, recordList,
                        ipAddressString);
        URL url = new URL(urlString);
        return url;
    }

    private String formatRecordList() {
        StringBuilder buffer = new StringBuilder();
        boolean isSecondOrLater = false;
        for (String recordIdentifier : recordIdentifiers) {
            if (isSecondOrLater) {
                buffer.append(',');
            } else {
                isSecondOrLater = true;
            }
            buffer.append(recordIdentifier);
        }
        return buffer.toString();
    }

    private boolean resultIncludesOtherThanNoChange(String result) {
        try {
            Pattern pattern = Pattern.compile("\\d* error-record-ip-same");
            BufferedReader reader =
                    new BufferedReader(new StringReader(result));
            String line;
            while (null != (line = reader.readLine())) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if (!pattern.matcher(line).matches())
                    return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e); // impossible
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

    public void addRecordIdentifier(String recordIdentifier) {
        this.recordIdentifiers.add(recordIdentifier);
    }
}
