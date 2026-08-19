package javax.microedition.lcdui;

import java.util.ArrayList;
import java.util.List;

public abstract class Displayable {
    private String title = "";
    private CommandListener commandListener;
    private final List<Command> commands = new ArrayList<>();
    protected int width = 800;
    protected int height = 480;

    protected Displayable() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String s) {
        this.title = s != null ? s : "";
    }

    public boolean isShown() {
        return Display.getActiveDisplay() != null && Display.getActiveDisplay().getCurrent() == this;
    }

    public void addCommand(Command cmd) {
        if (cmd != null && !commands.contains(cmd)) {
            commands.add(cmd);
        }
    }

    public void removeCommand(Command cmd) {
        commands.remove(cmd);
    }

    public void setCommandListener(CommandListener l) {
        this.commandListener = l;
    }

    public CommandListener getCommandListener() {
        return commandListener;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected void sizeChanged(int w, int h) {
        this.width = w;
        this.height = h;
    }
}
