package in.kodekrafter;

import in.kodekrafter.server.RingCacheServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        RingCacheServer cacheServer = new RingCacheServer();
        cacheServer.start();
    }
}
