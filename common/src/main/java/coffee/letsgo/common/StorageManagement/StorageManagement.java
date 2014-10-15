package coffee.letsgo.common.StorageManagement;

import static com.google.common.base.Verify.verify;

/**
 * Created by xbwu on 10/14/14.
 */
public abstract class StorageManagement<K, V> {
    protected final int capacity;

    public StorageManagement() {
        this(Integer.MAX_VALUE);
    }

    public StorageManagement(int capacity) {
        verify(capacity > 0, "capacity must be a positive integer");
        this.capacity = capacity;
    }

    public abstract void put(K key, V val);

    public abstract V get(K key);

    public abstract void invalid(K key);

    public abstract boolean contains(K key);

    public abstract int size();

    public abstract void clear();
}
