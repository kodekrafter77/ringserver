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
    private final List<CacheEntry> ttlEntries;
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
        this.ttlEntries = new LinkedList<>();
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
            ttlEntries.add(entry);
            log.info(ttlEntries.toString());
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

    List<CacheEntry> getTTLEntries() {
        try {
            lock.readLock().lock();
            return Collections.unmodifiableList(ttlEntries);
        } finally {
            lock.readLock().unlock();
        }

    }

    void evictEntry(CacheEntry entry) {
        try {
            log.info("Evicting entry: {}", entry);
            lock.writeLock().lock();
            cache.remove(entry.key);
            ttlEntries.remove(entry);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void startTTLScheduler() {
        executor.scheduleAtFixedRate(() -> {
            log.debug("Running ttl thread");
            if (!ttlEntries.isEmpty()) {
                List<LRUCache<K, V>.CacheEntry> evictList = ttlEntries.stream()
                        .filter(vw -> System.currentTimeMillis() - vw.enterTs >= vw.ttl)
                        .toList();

                evictList.forEach(this::evictEntry);

            } else {
                log.debug("Cache is empty");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
