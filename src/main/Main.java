package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("java.class.path"));
        int port = 8080;
        if (args.length == 1) {
            String portString = args[0];
            port = Integer.valueOf(portString);
        } else {
            logger.info("Порт не задан.");
        }

        logger.info("Starting at port: {}", String.valueOf(port));

        AppServer server = new AppServer(port);

        server.start();
    }
}