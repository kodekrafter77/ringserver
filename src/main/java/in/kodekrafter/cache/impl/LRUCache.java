package in.kodekrafter.cache.impl;

import in.kodekrafter.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache<K, V> implements Cache<K,V> {

    private static final Logger log = LoggerFactory.getLogger(LRUCache.class);

    private static final int DEFAULT_CAPACITY = 16;

    private final LinkedHashMap<K, CacheEntry> cache;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final ScheduledExecutorService executor;

    private static final ScheduledExecutorService DEFAULT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    class CacheEntry {
        final K key;
        final V value;
        final long ttl;
        final long enterTs;

        public CacheEntry() {
            this(null, null,0L);
        }

        public CacheEntry(K key, V value) {
            this(key,  value, 0L);
        }

        public CacheEntry(K key, V value, long ttl) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
            this.enterTs = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "{" +  key.toString() + ":" + value.toString()  + ((ttl > 0) ? ", ttl: " + ttl : "") + "}";
        }
    }

    public LRUCache() {
        this(DEFAULT_CAPACITY, DEFAULT_EXECUTOR);
    }

    public LRUCache(int capacity) {
        this(capacity, DEFAULT_EXECUTOR);
    }

    public LRUCache(int capacity, ScheduledExecutorService executor) {
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry> eldest) {
                return size() > capacity;
            }
        };

        this.executor = executor;
        startTTLScheduler();
    }

    @Override
    public void set(K key, V value) {
        try {
            lock.writeLock().lock();
            cache.put(key, new CacheEntry(key, value));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void set(K key, V value, long ttl) {
        try {
            lock.writeLock().lock();
            CacheEntry entry = new CacheEntry(key, value, ttl);
            cache.put(key, entry);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V get(K key) {
        try {
            lock.readLock().lock();
            if (cache.containsKey(key)) {
                return cache.get(key).value;
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }


    private void evictEntry(CacheEntry entry) {
        log.info("Evicting entry: {}", entry);
        cache.remove(entry.key);
    }

    private void startTTLScheduler() {
        executor.scheduleAtFixedRate(() -> {
            log.debug("Running ttl thread");
            try {
                lock.writeLock().lock();
                cache.values().stream()
                        .filter(cacheEntry -> cacheEntry.ttl > 0
                                && System.currentTimeMillis() - cacheEntry.enterTs >= cacheEntry.ttl)
                        .toList().forEach(this::evictEntry);
            } finally {
                lock.writeLock().unlock();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
