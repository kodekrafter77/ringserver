package in.kodekrafter.cache.proto.impl;

import in.kodekrafter.cache.impl.LRUCache;
import io.grpc.stub.StreamObserver;
import io.kodekrafter.cache.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class CacheServerImpl extends LRUCacheServiceGrpc.LRUCacheServiceImplBase {

    private final Logger log = LoggerFactory.getLogger(CacheServerImpl.class);
    private final LRUCache<ByteBuffer, ByteBuffer> cache = new LRUCache<>(10);

    @Override
    public  void set(SetRequest setRequest, StreamObserver<SetResponse> setResponse) {
        log.info("Got a set request");
        byte[] key = setRequest.getKey().getBytes();
        byte[] value = setRequest.getValue().getBytes();
        long ttl = setRequest.getTtl();
        if (ttl > 0) {
            cache.set(ByteBuffer.wrap(key), ByteBuffer.wrap(value), ttl);
        } else {
            cache.set(ByteBuffer.wrap(key), ByteBuffer.wrap(value), ttl);
        }
        SetResponse resp = SetResponse.newBuilder()
                .setMessage("OK")
                .setSuccess(true)
                .build();
        setResponse.onNext(resp);
        setResponse.onCompleted();
        log.info("set request completed");
    }

    public void get(GetRequest getRequest, StreamObserver<GetResponse> getResponse) {
        log.info("Got a get request");
        byte[] key = getRequest.getKey().getBytes();
        ByteBuffer bvalue = cache.get(ByteBuffer.wrap(key));
        byte[] value = null;
        if (bvalue != null) {
            value = bvalue.array();
            GetResponse response = GetResponse.newBuilder()
                    .setValue(new String(value))
                    .setFound(true)
                    .build();
            getResponse.onNext(response);
            getResponse.onCompleted();
        } else {
            log.info("no value found for key {}", key);
            GetResponse response = GetResponse.newBuilder()
                    .setFound(false)
                    .build();
            getResponse.onNext(response);
            getResponse.onCompleted();
        }
        log.info("get request completed");
    }
}
