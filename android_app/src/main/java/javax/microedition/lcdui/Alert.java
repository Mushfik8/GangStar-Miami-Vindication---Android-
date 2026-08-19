package javax.microedition.lcdui;

public class Alert extends Displayable {
    public static final int FOREVER = -2;
    private int timeout = 2000;
    private String text = "";

    public Alert(String title) {
        setTitle(title);
    }

    public Alert(String title, String alertText, Image alertImage, Object alertType) {
        setTitle(title);
        this.text = alertText != null ? alertText : "";
    }

    public int getDefaultTimeout() {
        return 2000;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int time) {
        this.timeout = time;
    }

    public String getString() {
        return text;
    }

    public void setString(String str) {
        this.text = str != null ? str : "";
    }
}
