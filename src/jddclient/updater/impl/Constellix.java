package jddclient.updater.impl;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jddclient.updater.AbstractUpdater;
import jddclient.updater.ProviderException;
import jddclient.updater.ProviderException.FurtherAction;
import jddclient.updater.SameIpException;
import jddclient.updater.UpdaterException;

public class Constellix extends AbstractUpdater {
    private final Logger logger = LoggerFactory.getLogger(Constellix.class);

    private String apiKey;
    private String secretKey;
    private String domainId;
    private String recordId;

    @Override
    protected void sendAddress(InetAddress address)
            throws SameIpException, ProviderException, UpdaterException {
        String url = String.format("https://api.dns.constellix.com/v1/domains/%s/records/A/%s",
                domainId, recordId);
        String addressString = address.getHostAddress();
        String json = String.format("{ ttl: 60, roundRobin: [ { value: '%s' } ] }", addressString);

        try {
            request(url, json);
        } catch (IOException e) {
            throw new UpdaterException(e);
        }
    }

    private String request(String url, String json) throws IOException, ProviderException {
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Accept", "application/json; charset=UTF-8");
        con.setRequestProperty("x-cns-security-token", securityToken());
        con.setConnectTimeout(5_000);
        con.setReadTimeout(20_000);
        con.setDoOutput(true);
        try (OutputStream out = con.getOutputStream()) {
            out.write(json.getBytes(UTF_8));
        }

        if (con.getResponseCode() == HTTP_OK) {
            try (Scanner in = new Scanner(con.getInputStream())) {
                String prettyResponse = in.useDelimiter("\\Z").next();
                logger.debug("Response:\n{}", prettyResponse);
                return prettyResponse;
            }
        } else {
            throw new ProviderException("HTTP error: " + errorMessageFromUrlConnection(con),
                    FurtherAction.Retry);
        }
    }

    @SuppressWarnings("resource")
    private String errorMessageFromUrlConnection(HttpURLConnection con) throws IOException {
        String httpErrorStatus = con.getResponseCode() + " " + con.getResponseMessage();
        try (InputStream in = con.getErrorStream()) {
            if (in == null) {
                return httpErrorStatus + "\n" + con.getHeaderFields();
            } else {
                String prettyResponse = new Scanner(in).useDelimiter("\\Z").next();
                return httpErrorStatus + "\n" + prettyResponse;
            }
        }
    }

    private String securityToken() {
        String millis = String.valueOf(System.currentTimeMillis());
        String hmac = hmac_sha1(millis, secretKey);
        return apiKey + ':' + hmac + ':' + millis;
    }

    private String hmac_sha1(String text, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            return Base64.encodeBase64String(mac.doFinal(text.getBytes(UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
}
