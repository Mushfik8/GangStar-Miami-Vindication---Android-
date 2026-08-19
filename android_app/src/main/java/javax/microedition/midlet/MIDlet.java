package javax.microedition.midlet;

import java.util.HashMap;
import java.util.Map;

public abstract class MIDlet {
    private static MIDlet activeMidlet;
    private static final Map<String, String> appProperties = new HashMap<>();

    public static MIDlet getActiveMidlet() {
        return activeMidlet;
    }

    public static void setAppProperty(String key, String value) {
        if (key != null && value != null) {
            appProperties.put(key, value);
        }
    }

    protected MIDlet() {
        activeMidlet = this;
    }

    public abstract void startApp();
    public abstract void pauseApp();
    public abstract void destroyApp(boolean unconditional);

    public void notifyDestroyed() {
        // Called when midlet has finished
    }

    public void notifyPaused() {
        // Called when midlet has paused
    }

    public void resumeRequest() {
        // Called to request resume
    }

    public String getAppProperty(String key) {
        if (key == null) return null;
        return appProperties.get(key);
    }

    public boolean platformRequest(String URL) {
        return false;
    }

    public int checkPermission(String permission) {
        return 1; // 1 = allowed
    }
}
