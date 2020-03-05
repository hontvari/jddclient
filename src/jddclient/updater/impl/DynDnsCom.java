package jddclient.updater.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.IDN;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import jddclient.updater.AbstractUpdater;
import jddclient.updater.ProviderException;
import jddclient.updater.ProviderMaintenanceException;
import jddclient.updater.SameIpException;
import jddclient.updater.UpdaterException;
import jddclient.util.HttpGetter;

import org.joda.time.Minutes;

public class DynDnsCom extends AbstractUpdater {
    private static final String URL_TEMPLATE =
            "https://members.dyndns.org/nic/update?hostname={0}&myip={1}&wildcard=NOCHG&mx=NOCHG&backmx=NOCHG";
    private String user;
    private String password;
    private final List<String> hosts = new ArrayList<String>();

    {
        maxAttempts = 5;
    }

    @Override
    protected void sendAddress(InetAddress address) throws SameIpException,
            ProviderException, UpdaterException {
        URL url;
        try {
            url = constructURL(address);
        } catch (MalformedURLException e) {
            throw new UpdaterException(e);
        }
        HttpGetter httpGetter = new HttpGetter(user, password);
        String result;
        try {
            result = httpGetter.downloadDocument(url, 1024).trim();
        } catch (IOException e) {
            throw new UpdaterException(e);
        }
        if (result.contains("good 127.0.0.1") || result.contains("badauth")
                || result.contains("badauth") || result.contains("!donator")
                || result.contains("notfqdn") || result.contains("nohost")
                || result.contains("numhost") || result.contains("abuse")
                || result.contains("badagent")
                || resultIncludesOtherThanSuccessfulNoChangeMaintenance(result)) {
            throw new ProviderException(
                    "Provider reports a permanent failure status, "
                            + "no further IP address updates should be attempted "
                            + "before correcting the problem: " + result,
                    ProviderException.FurtherAction.Permanent);
        } else if (result.equals("dnserr") || result.equals("911")) {
            throw new ProviderMaintenanceException("An error occured within the "
                    + "system of the provider, or it is undergoing system "
                    + "maintenance. A 30 minutes break is requested "
                    + "before the next update. Error code is " + result,
                    Minutes.minutes(30).toStandardDuration());
        } else if (result.contains("nochg")) {
            throw new SameIpException(result);
        } else {
            // success
            return;
        }
    }

    private URL constructURL(InetAddress address) throws MalformedURLException {
        String hostList = formatList(idnToAscii(hosts));
        String ipAddressString = address.getHostAddress();
        String urlString =
                MessageFormat.format(URL_TEMPLATE, hostList, ipAddressString);
        URL url = new URL(urlString);
        return url;
    }

    private static String formatList(List<String> list) {
        StringBuilder buffer = new StringBuilder();
        boolean isSecondOrLater = false;
        for (String element : list) {
            if (isSecondOrLater) {
                buffer.append(',');
            } else {
                isSecondOrLater = true;
            }
            buffer.append(element);
        }
        return buffer.toString();
    }

    private static List<String> idnToAscii(List<String> domains) {
        List<String> asciiDomains = new ArrayList<String>();
        for (String domain : domains) {
            asciiDomains.add(IDN.toASCII(domain, IDN.ALLOW_UNASSIGNED
                    | IDN.USE_STD3_ASCII_RULES));
        }
        return asciiDomains;
    }

    private boolean resultIncludesOtherThanSuccessfulNoChangeMaintenance(
            String result) {
        try {
            BufferedReader reader =
                    new BufferedReader(new StringReader(result));
            String line;
            while (null != (line = reader.readLine())) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String word;
                int i = line.indexOf(' ');
                word = i == -1 ? line : line.substring(0, i);
                if (!word.equals("good") && !word.equals("nochg")
                        && !word.equals("dnserr") && !word.equals("911"))
                    return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(); // impossible
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

    public void addHost(String recordIdentifier) {
        this.hosts.add(recordIdentifier);
    }

}
