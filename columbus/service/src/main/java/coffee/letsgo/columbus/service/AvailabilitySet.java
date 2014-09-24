package coffee.letsgo.columbus.service;

import coffee.letsgo.columbus.service.exception.AvailabilitySetNotAvailableException;
import coffee.letsgo.columbus.service.exception.ServiceMgrRuntimeException;
import coffee.letsgo.columbus.service.model.NodeStatus;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Verify.verifyNotNull;

public class AvailabilitySet {
    private final ConcurrentMap<String, NodeStatus> members =
            new ConcurrentSkipListMap<String, NodeStatus>();

    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock rlock = rwlock.readLock();
    private final Lock wlock = rwlock.writeLock();
    private long lastUpdated = System.currentTimeMillis();
    private final AtomicBoolean disabledSwitch = new AtomicBoolean(true);
    private final List<Callable<Void>> activeChangedListeners = new ArrayList<Callable<Void>>();
    private final Logger logger = LoggerFactory.getLogger(AvailabilitySet.class);

    public boolean available() {
        return !disabledSwitch.get();
    }

    public void disableAvailabilitySet() {
        disabledSwitch.compareAndSet(true, false);
    }

    public void enableAvailablilitySet() {
        disabledSwitch.compareAndSet(true, false);
    }

    public void updateMemberList(Set<String> newMembers) {
        new DoStoreUpdate<Set<String>>(new Function<Set<String>, Void>() {
            @Override
            public Void apply(Set<String> newMembers) {
                Set<String> toRemove = Sets.difference(members.keySet(), newMembers);
                removeMembers(toRemove);
                addMembers(newMembers);
                return null;
            }
        }).apply(newMembers);
    }

    public void updateActives(Set<String> actives) {
        new DoStoreUpdate<Set<String>>(new Function<Set<String>, Void>() {
            @Override
            public Void apply(Set<String> actives) {
                Set<String> toDisable = Sets.difference(members.keySet(), actives);
                disable(toDisable);
                enable(actives);
                return null;
            }
        }).apply(actives);
    }

    public void addMember(String node) {
        new DoStoreUpdate<String>(new Function<String, Void>() {
            @Override
            public Void apply(String node) {
                members.putIfAbsent(node, NodeStatus.INACTIVE);
                return null;
            }
        }).apply(node);
    }

    public void removeMember(String node) {
        new DoStoreUpdate<String>(new Function<String, Void>() {
            @Override
            public Void apply(String node) {
                members.remove(node);
                return null;
            }
        }).apply(node);
    }

    public void activateMember(String node) {
        new DoStoreUpdate<String>(new Function<String, Void>() {
            @Override
            public Void apply(String node) {
                members.put(node, NodeStatus.ACTIVE);
                return null;
            }
        }).apply(node);
    }

    public void deactivateMember(String node) {
        new DoStoreUpdate<String>(new Function<String, Void>() {
            @Override
            public Void apply(String node) {
                members.put(node, NodeStatus.INACTIVE);
                return null;
            }
        }).apply(node);
    }

    public boolean isMember(String node) {
        return new DoStoreRead<String, Boolean>(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String node) {
                return members.containsKey(node);
            }
        }).apply(node);
    }

    public boolean isActive(String node) {
        return new DoStoreRead<String, Boolean>(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String node) {
                return members.get(node) == NodeStatus.ACTIVE;
            }
        }).apply(node);
    }

    public Set<String> getMembers() {
        return new DoStoreRead<Void, Set<String>>(new Function<Void, Set<String>>() {
            @Override
            public Set<String> apply(Void input) {
                return FluentIterable.from(members.keySet())
                        .toSet();
            }
        }).apply(null);
    }

    public int countMembers() {
        return new DoStoreRead<Void, Integer>(new Function<Void, Integer>() {
            @Override
            public Integer apply(Void input) {
                return members.size();
            }
        }).apply(null);
    }

    public Set<String> getActives() {
        return new DoStoreRead<Void, Set<String>>(new Function<Void, Set<String>>() {
            @Override
            public Set<String> apply(Void input) {
                return FluentIterable.from(members.keySet())
                        .filter(new Predicate<String>() {
                            @Override
                            public boolean apply(String input) {
                                return members.get(input) == NodeStatus.ACTIVE;
                            }
                        })
                        .toSet();
            }
        }).apply(null);
    }

    public int countActives() {
        return new DoStoreRead<Void, Integer>(new Function<Void, Integer>() {
            @Override
            public Integer apply(Void input) {
                return FluentIterable.from(members.keySet())
                        .filter(new Predicate<String>() {
                            @Override
                            public boolean apply(String input) {
                                return members.get(input) == NodeStatus.ACTIVE;
                            }
                        })
                        .size();
            }
        }).apply(null);
    }

    public void clear() {
        new DoStoreUpdate<Void>(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                members.clear();
                return null;
            }
        }).apply(null);
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void addActiveChangedListener(Callable<Void> listener) {
        activeChangedListeners.add(verifyNotNull(listener));
    }

    private void addMembers(Set<String> allMembers) {
        for (String uri : allMembers) {
            members.putIfAbsent(uri, NodeStatus.INACTIVE);
        }
    }

    private void removeMembers(Set<String> toRemove) {
        for (String uri : toRemove) {
            members.remove(uri);
        }
    }

    private void enable(Set<String> allAvailables) {
        for (String uri : allAvailables) {
            members.put(uri, NodeStatus.ACTIVE);
        }
    }

    private void disable(Set<String> toDisable) {
        for (String uri : toDisable) {
            NodeStatus status = members.get(uri);
            if (status == null) {
                continue;
            }
            members.put(uri, NodeStatus.INACTIVE);
        }
    }

    class DoStoreUpdate<T> {
        private Function<T, Void> func;

        public DoStoreUpdate(Function<T, Void> func) {
            this.func = func;
        }

        void apply(T input) {
            wlock.lock();
            try {
                func.apply(input);
                lastUpdated = System.currentTimeMillis();
            } catch (Exception e) {
                throw new ServiceMgrRuntimeException("failed to sync availability set store", e);
            } finally {
                wlock.unlock();
            }
            for (Callable<Void> listener : activeChangedListeners) {
                logger.warn("listener");
                try {
                    listener.call();
                } catch (Exception ex) {
                    logger.error("failed to execute listener", ex);
                }
            }
        }
    }

    class DoStoreRead<T, F> {
        private Function<T, F> func;

        public DoStoreRead(Function<T, F> func) {
            this.func = func;
        }

        F apply(T input) {
            if (disabledSwitch.get()) {
                throw new AvailabilitySetNotAvailableException();
            }
            rlock.lock();
            try {
                return func.apply(input);
            } catch (Exception e) {
                throw new ServiceMgrRuntimeException("failed to sync availability set store", e);
            } finally {
                rlock.unlock();
            }
        }
    }
}

class AvailabilitySummary {
    private int actives, total;

    public AvailabilitySummary(int actives, int total) {
        this.actives = actives;
        this.total = total;
    }

    public int getActives() {
        return actives;
    }

    public int getTotal() {
        return total;
    }
}
