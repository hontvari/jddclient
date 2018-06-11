package jddclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Store saves and retrieves the persistent state of jddclient.
 */
public class Store {
    private final Logger logger = LoggerFactory.getLogger(Store.class);
    private File file;
    private XmlStateManager xmlStateManager;

    public void initialize() {
        if (file == null) {
            file = defaultFile();
        }
    }

    private File defaultFile() {
        File defaultDir = new File("/var/lib/jddclient");
        if (System.getProperty("os.name").startsWith("Windows")) {
            String envString = System.getenv("ALLUSERSPROFILE");
            if (envString != null)
                defaultDir = new File(envString, "jddclient");
        }
        if (!defaultDir.exists()) {
            throw new RuntimeException("Store dir '" + defaultDir
                    + "' does not exist.");
        }
        File defaultPath = new File(defaultDir, "status.xml");
        return defaultPath;
    }

    public void save() {
        try {
            Document document = xmlStateManager.saveState();
            saveDocument(document);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveDocument(Document document) throws IOException,
            TransformerException {
        OutputStream outStream = null;
        try {
            Transformer transformer =
                    TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            outStream = new FileOutputStream(file);
            OutputStreamWriter writer =
                    new OutputStreamWriter(outStream, "UTF-8");
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            writer.close();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    logger.warn("Cannot close file", e);
                }
            }

        }
    }

    public void load() throws ParserConfigurationException, SAXException,
            IOException {
        // after installation there is no store file, there is nothing to load
        if (!file.exists())
            return;
        DocumentBuilder documentBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        xmlStateManager.restoreState(document);
    }

    /**
     * @category GETSET
     */
    public void setFile(String file) {
        this.file = new File(file);
    }

    public void setXmlStateManager(XmlStateManager xmlStateManager) {
        this.xmlStateManager = xmlStateManager;
    }

}
