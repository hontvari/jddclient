package jddclient.detector;


public abstract class AbstractDetector implements Detector {
    protected static String DEFAULT_NAME = "DefaultDetector";
    protected String detectorName = DEFAULT_NAME;

    /**
     * @category GETSET
     */
    @Override
    public String getName() {
        return detectorName;
    }

    /**
     * @category GETSET
     */
    public void setName(String id) {
        this.detectorName = id;
    }
}
