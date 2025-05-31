package in.kodekrafter.client;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.kodekrafter.cache.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RingeCacheClient {
    private static final Logger log = LoggerFactory.getLogger(RingeCacheClient.class);
    private static LRUCacheServiceGrpc.LRUCacheServiceBlockingStub blockingStub;

    public RingeCacheClient(Channel channel) {
        blockingStub = LRUCacheServiceGrpc.newBlockingStub(channel);
    }

    public void set(String key, String value) {
        log.info("Setting key {}, value {}", key, value);
        SetRequest request = SetRequest.newBuilder().setKey(ByteString.copyFrom(key.getBytes()))
                .setValue(ByteString.copyFrom(value.getBytes()))
                .setTtl(5000).build();

        try {
            SetResponse response = blockingStub.set(request);
            log.info("Set response: " + response.getMessage());
        } catch (Exception e) {
            log.error("Exception occurred during cache set", e);
        }
    }

    public void get(String key) {
        log.info("Get key {}", key);
        GetRequest request = GetRequest.newBuilder().setKey(ByteString.copyFrom(key.getBytes())).build();
        try {
            GetResponse response = blockingStub.get(request);
            log.info("Response received {}", new String(response.getValue().toByteArray()));
        }catch (Exception e) {
            log.error("Exception occurred during cache get", e);
        }
    }

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = Grpc.newChannelBuilder("localhost:9072", InsecureChannelCredentials.create())
                .build();
        RingeCacheClient client = new RingeCacheClient(channel);
        client.set("test1", "test1value");
        client.get("test1");
        Thread.sleep(10000);
        client.get("test1");
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}
