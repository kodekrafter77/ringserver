package in.kodekrafter.server;

import in.kodekrafter.cache.proto.impl.CacheServerImpl;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RingCacheServer {
    private final Logger log= LoggerFactory.getLogger(this.getClass());
    public static final int DEFAULT_PORT = 9072;
    private final int port;
    private final Server server;

    public RingCacheServer() {
        this(DEFAULT_PORT);
    }

    public RingCacheServer(int port) {
        this.port = port;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        this.server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new CacheServerImpl())
                .executor(executor)
                .build();
    }

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Shutting down RingCacheServer");
                try {
                    RingCacheServer.this.stop();
                } catch(InterruptedException e) {
                    log.error("Error stopping RingCacheServer", e);
                }
                log.info("RingCacheServer shut down");
            }
        });
        try {
            log.info("Server started on port {}" , port);
            blockUntilShutdown();
        } catch(InterruptedException e) {
            log.error("Error shutting down RingCacheServer", e);
        }
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
