package coffee.letsgo.common.StorageManagement;

import coffee.letsgo.common.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xbwu on 10/14/14.
 */
public class StorageManagementLRUImpl<K, V> extends StorageManagement<K, V> {
    private final Map<K, DoublyLinkedList<K, V>> map;
    private final DoublyLinkedList<K, V> head, tail;

    public StorageManagementLRUImpl() {
        this(Integer.MAX_VALUE);
    }

    public StorageManagementLRUImpl(int capacity) {
        super(capacity);
        head = new DoublyLinkedList<K, V>(null, null);
        tail = new DoublyLinkedList<K, V>(null, null);
        head.next = tail;
        tail.prev = head;
        map = new HashMap<K, DoublyLinkedList<K, V>>();
    }

    @Override
    public void put(K key, V val) {
        synchronized (this) {
            if (map.containsKey(key)) {
                remove(key);
            }
            insert(new DoublyLinkedList<K, V>(key, val));
        }
    }

    @Override
    public V get(K key) {
        synchronized (this) {
            if (!map.containsKey(key)) {
                throw new NotFoundException();
            }
            DoublyLinkedList<K, V> n = remove(key);
            insert(n);
            return n.val;
        }
    }

    @Override
    public void invalid(K key) {
        synchronized (this) {
            remove(key);
        }
    }

    @Override
    public boolean contains(K key) {
        synchronized (this) {
            return map.containsKey(key);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return map.size();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            if (head.next != null) {
                head.next.prev = null;
            }
            if (tail.prev != null) {
                tail.prev.next = null;
            }
            head.next = tail;
            tail.prev = head;
            map.clear();
        }
    }

    private DoublyLinkedList<K, V> remove(K key) {
        DoublyLinkedList<K, V> ret = map.get(key);
        ret.prev.next = ret.next;
        ret.next.prev = ret.prev;
        ret.prev = ret.next = null;
        map.remove(key);
        return ret;
    }

    private void insert(DoublyLinkedList<K, V> node) {
        if (map.size() >= capacity) {
            remove(tail.prev.key);
        }
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
        map.put(node.key, node);
    }

    class DoublyLinkedList<K, V> {
        public final K key;
        public final V val;
        public DoublyLinkedList<K, V> prev;
        public DoublyLinkedList<K, V> next;

        public DoublyLinkedList(K key, V val) {
            this.key = key;
            this.val = val;
            prev = next = null;
        }
    }
}
