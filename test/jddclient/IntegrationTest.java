package jddclient;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.junit.Ignore;

@Ignore
public class IntegrationTest {
    /**
     * null if not initialized
     */
    private Properties properties;

    protected Properties props() {
        if (properties != null)
            return properties;

        try {
            properties = new Properties();
            URL url =
                    getClass().getResource(
                            getClass().getSimpleName() + ".properties");
            if (url == null)
                throw new RuntimeException(
                        "Integration property file was not found");
            InputStream stream = url.openStream();
            try {
                Reader reader = new InputStreamReader(stream, "UTF-8");
                properties.load(reader);
            } finally {
                stream.close();
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String prop(String key) {
        String value = props().getProperty(key);
        if (value == null || value.equals("CHANGEIT")) {
            String prompt =
                    String.format("%s / %s?", getClass().getName(), key);
            Console console = System.console();
            if (console != null) {
                value = console.readLine("%s", prompt);
            } else {
                try {
                    System.out.print(prompt);
                    value =
                            new BufferedReader(new InputStreamReader(System.in))
                                    .readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            props().setProperty(key, value);
        }
        return value;
    }
}
