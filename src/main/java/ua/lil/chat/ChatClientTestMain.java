package ua.lil.chat;

import ua.lil.chat.config.Settings;
import ua.lil.chat.helpers.LogHelper;
import ua.lil.chat.protocol.UserMessagePacket;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ChatClientTestMain {

    public static final Path WORKING_DIR = Paths.get(System.getProperty("user.dir"));

    public static void main(String[] args) throws Exception {
        LogHelper.info("Welcome! Chat Test coded by LiLTeam");
        Settings.IMP.reload(WORKING_DIR.resolve("config.yml").toFile());
        Scanner scanner = new Scanner(System.in);
        LogHelper.info("Enter user name: ");
        String userName = scanner.nextLine();
        Connector connector = new Connector(userName, Settings.IMP.HOST, Settings.IMP.PORT);
        Connector.setInstance(connector);
        connector.start();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.startsWith("/") && line.trim().equalsIgnoreCase("/stop")) {
                connector.stop();
                continue;
            }

            UserMessagePacket packet = new UserMessagePacket();
            packet.setUserName(userName);
            packet.setMessage(line);
            connector.sendPacket(packet);
        }
    }
}
