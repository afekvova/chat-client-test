package ua.lil.chat.config;

import java.io.File;

public class Settings extends Config {

    @Ignore
    public static final Settings IMP = new Settings();

    public String HOST = "127.0.0.1";
    public int PORT = 5000;

    public void reload(File file) {
        load(file);
        save(file);
    }
}
