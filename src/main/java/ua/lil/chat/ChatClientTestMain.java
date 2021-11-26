package ua.lil.chat;

import jline.console.ConsoleReader;
import ua.lil.chat.protocol.MessagePacket;

import java.io.IOException;
import java.util.Scanner;

public class ChatClientTestMain {
    ConsoleReader consoleReader;

    public ChatClientTestMain() throws IOException {
//        consoleReader = new ConsoleReader();
//        consoleReader.setExpandEvents(false);
//        while (true) {
//            String line = consoleReader.readLine();
//            System.out.println(line);
//        }
    }


    public static void main(String[] args) {
        System.out.println("GGWP!");
        Connector connector = new Connector("test", "127.0.0.1", 5000);
        Connector.setInstance(connector);
        connector.start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            MessagePacket packet = new MessagePacket();
            packet.setMessage(line);
            connector.sendPacket(packet);
        }
    }
}
