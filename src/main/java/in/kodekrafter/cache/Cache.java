package in.kodekrafter.cache;

public interface Cache<K, V> {
    public void set(K key, V value);
    public void set(K key, V value, long ttl);
    public V get(K key);
}
