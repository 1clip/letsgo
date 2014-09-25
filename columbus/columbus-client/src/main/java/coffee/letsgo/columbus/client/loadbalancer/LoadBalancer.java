package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.client.exception.LoadBalancerException;
import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Verify.verify;

/**
 * Created by xbwu on 9/7/14.
 */
public abstract class LoadBalancer<C> {
    private final AvailabilitySet availabilitySet;
    private final Function<String, ListenableFuture> tunnelCreator;
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock rlock = rwlock.readLock();
    private final Lock wlock = rwlock.writeLock();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public LoadBalancer(AvailabilitySet availabilitySet, Function<String, ListenableFuture> tunnelCreator) {
        this.availabilitySet = availabilitySet;
        this.tunnelCreator = tunnelCreator;
        this.availabilitySet.addActiveChangedListener(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    updateActiveMembers();
                } catch (Exception ex) {
                    logger.error("failed to update load balancer with new availability", ex);
                }
                return null;
            }
        });
        updateActiveMembers();
    }

    protected final Map<String, ListenableFuture<C>> tunnelDict = new HashMap<String, ListenableFuture<C>>();
    protected ImmutableList<String> activeList = ImmutableList.of();

    private void updateActiveMembers() {
        Set<String> actives = availabilitySet.getActives();
        Set<String> toRemove = new HashSet<String>();
        for (String uri : activeList) {
            if (!actives.contains(uri)) {
                toRemove.add(uri);
            }
        }
        wlock.lock();
        try {
            for (String uri : toRemove) {
                tunnelDict.remove(uri);
            }
            for (String uri : actives) {
                if (!tunnelDict.containsKey(uri)) {
                    tunnelDict.put(uri, tunnelCreator.apply(uri));
                }
            }
            activeList = FluentIterable.from(actives).toSortedList(String.CASE_INSENSITIVE_ORDER);
        } finally {
            wlock.unlock();
        }
    }

    public C next() throws LoadBalancerException {
        rlock.lock();
        try {
            verify(!activeList.isEmpty(), "no active node");
            String uri = nextUri();
            if (!tunnelDict.containsKey(uri)) {
                tunnelDict.put(uri, tunnelCreator.apply(uri));
            }
            return tunnelDict.get(uri).get();
        } catch (Exception ex) {
            logger.error("failed to get next available tunnel", ex);
            throw new LoadBalancerException("failed to get next available tunnel", ex);
        } finally {
            rlock.unlock();
        }
    }

    protected abstract String nextUri();
}
