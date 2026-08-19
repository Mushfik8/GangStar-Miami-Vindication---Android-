package javax.microedition.media;

import java.io.InputStream;

public class Manager {
    public static final String MIDI = "audio/midi";
    public static final String TONE_DEVICE_LOCATOR = "device://tone";

    public static Player createPlayer(InputStream stream, String type) {
        return new Player(stream, type);
    }

    public static Player createPlayer(String locator) {
        return new Player(null, locator);
    }

    public static void playTone(int note, int duration, int volume) {
        // Optional tone playback
    }

    public static String[] getSupportedContentTypes(String protocol) {
        return new String[]{"audio/midi", "audio/x-wav", "audio/amr"};
    }

    public static String[] getSupportedProtocols(String content_type) {
        return new String[]{"device", "http"};
    }
}
