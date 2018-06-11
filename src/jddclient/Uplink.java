package jddclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jddclient.detector.Detector;
import jddclient.updater.Updater;

/**
 * Uplink represents a combination of a connection with a dynamic IP address.
 * The IP address is detected by one or more detectors and is sent to one or
 * more DNS providers.
 */
public class Uplink {

    private String name = "DefaultUplink";
    private final List<Detector> detectors = new ArrayList<Detector>();
    private final List<Updater> updaters = new ArrayList<Updater>();

    /**
     * @category GETSET
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @category GETSET
     */
    public String getName() {
        return name;
    }

    public void addDetector(Detector detector) {
        detectors.add(detector);
    }

    public List<Detector> getDetectors() {
        return Collections.unmodifiableList(detectors);
    }

    public void addUpdater(Updater updater) {
        updaters.add(updater);
    }

    public List<Updater> getUpdaters() {
        return Collections.unmodifiableList(updaters);
    }
}
