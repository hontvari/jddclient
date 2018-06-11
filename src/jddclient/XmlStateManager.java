package jddclient;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jddclient.updater.Updater;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * XmlStateManager converts the state of jddclient updaters into an XML document
 * and converts back from an XML document.
 */
public class XmlStateManager {
    private final LinkedHashMap<String, Updater> updaters;

    public XmlStateManager(LinkedHashMap<String, Updater> updaters) {
        this.updaters = updaters;
    }

    Document saveState() throws ParserConfigurationException {
        DocumentBuilder documentBuilder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        document.setXmlStandalone(true);
        Element rootElement = document.createElement("status");
        document.appendChild(rootElement);

        for (Entry<String, Updater> updaterEntry : updaters.entrySet()) {
            Element element = document.createElement("updater");
            element.setAttribute("name", updaterEntry.getKey());
            updaterEntry.getValue().saveState(element);
            rootElement.appendChild(element);
        }
        return document;
    }

    void restoreState(Document document) {
        NodeList updaterElements = document.getElementsByTagName("updater");
        for (int i = 0; i < updaterElements.getLength(); i++) {
            Element updaterElement = (Element) updaterElements.item(i);
            String name = updaterElement.getAttribute("name");
            Updater updater = updaters.get(name);
            if (updater != null)
                updater.loadState(updaterElement);

        }
    }
}
