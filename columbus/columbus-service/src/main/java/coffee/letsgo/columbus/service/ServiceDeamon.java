package coffee.letsgo.columbus.service;

import coffee.letsgo.columbus.service.exception.ServiceManagerRuntimeException;
import coffee.letsgo.columbus.service.manager.ServiceManager;
import coffee.letsgo.columbus.service.model.ServiceContext;
import coffee.letsgo.columbus.service.model.ServiceEvent;
import com.google.common.base.Function;
import com.google.common.util.concurrent.*;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/6/14.
 */
public class ServiceDeamon {
    private final String serviceName;
    private final ServiceManager manager;
    private final AvailabilitySet availabilitySet = new AvailabilitySet();
    private final AtomicBoolean startedSwitch = new AtomicBoolean(false);
    private final Logger logger = LoggerFactory.getLogger(ServiceDeamon.class);
    private final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private CountDownLatch latch;

    private final ServiceContext serviceContext = new ServiceContext();
    private final Set<String> activatedByMe = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Inject
    public ServiceDeamon(@Named("service name") String serviceName, ServiceManager manager) {
        this.serviceName = verifyNotNull(serviceName, "service name");
        this.manager = verifyNotNull(manager, "cluster manager");
        this.manager.addEventHandler(eventHandler);
    }

    public String addMember(int port) throws UnknownHostException {
        String uri = localUriWithPort(port);
        addMember(uri);
        return uri;
    }

    private final DoIfStarted<String, Void> addMemberActor =
            new DoIfStarted<String, Void>(new Function<String, Void>() {
                @Override
                public Void apply(String uri) {
                    manager.addNode(uri);
                    return null;
                }
            });

    public String addMember(String uri) {
        addMemberActor.apply(uri);
        return uri;
    }

    public String removeMember(int port) throws UnknownHostException {
        String uri = localUriWithPort(port);
        removeMember(uri);
        return uri;
    }

    private final DoIfStarted<String, Void> removeMemberActor =
            new DoIfStarted<String, Void>(new Function<String, Void>() {
                @Override
                public Void apply(String uri) {
                    manager.removeNode(uri);
                    return null;
                }
            });

    public String removeMember(String uri) {
        removeMemberActor.apply(uri);
        return uri;
    }

    public boolean isMember(int port) throws UnknownHostException {
        return isMember(localUriWithPort(port));
    }

    private final DoIfStarted<String, Boolean> isMemberActor =
            new DoIfStarted<String, Boolean>(new Function<String, Boolean>() {
                @Override
                public Boolean apply(String uri) {
                    return availabilitySet.isMember(uri);
                }
            });

    public boolean isMember(String uri) {
        return isMemberActor.apply(uri);
    }

    public String activate(int port) throws UnknownHostException {
        String uri = localUriWithPort(port);
        activate(uri);
        return uri;
    }

    private final DoIfStarted<String, Void> activateActor =
            new DoIfStarted<String, Void>(new Function<String, Void>() {
                @Override
                public Void apply(String uri) {
                    manager.activateNode(uri);
                    return null;
                }
            });

    public String activate(String uri) {
        activateActor.apply(uri);
        activatedByMe.add(uri);
        return uri;
    }

    public String deactivate(int port) throws UnknownHostException {
        String uri = localUriWithPort(port);
        deactivate(uri);
        return uri;
    }

    private final DoIfStarted<String, Void> deactivateActor =
            new DoIfStarted<String, Void>(new Function<String, Void>() {
                @Override
                public Void apply(String uri) {
                    manager.deactivateNode(uri);
                    return null;
                }
            });

    public String deactivate(String uri) {
        deactivateActor.apply(uri);
        activatedByMe.remove(uri);
        return uri;
    }

    public boolean isActive(int port) throws UnknownHostException {
        return isActive(localUriWithPort(port));
    }

    private final DoIfStarted<String, Boolean> isActiveActor =
            new DoIfStarted<String, Boolean>(new Function<String, Boolean>() {
                @Override
                public Boolean apply(String uri) {
                    return availabilitySet.isActive(uri);
                }
            });

    public boolean isActive(String uri) {
        return isActiveActor.apply(uri);
    }

    private String localUriWithPort(int port) throws UnknownHostException {
        return String.format("%s:%d",
                InetAddress.getLocalHost().getCanonicalHostName(),
                port);
    }

    public AvailabilitySet getAvailabilitySet() {
        return availabilitySet;
    }

    private final DoIfNotStarted<Void, Void> startActor =
            new DoIfNotStarted<Void, Void>(new Function<Void, Void>() {
                @Override
                public Void apply(Void input) {
                    if (startedSwitch.compareAndSet(false, true)) {
                        logger.info("starting deamon");
                        latch = new CountDownLatch(1);
                        manager.start();
                    }
                    return null;
                }
            });

    public void start() {
        startActor.apply(null);
    }

    private final DoIfStarted<Void, Void> shutdownActor =
            new DoIfStarted<Void, Void>(new Function<Void, Void>() {
                @Override
                public Void apply(Void input) {
                    if (startedSwitch.compareAndSet(true, false)) {
                        availabilitySet.disableAvailabilitySet();
                        logger.info("shutting down deamon");
                        scheduler.shutdown();
                        manager.stop();
                        availabilitySet.clear();
                        latch = null;
                    }
                    return null;
                }
            });

    public void shutdown() {
        shutdownActor.apply(null);
    }

    public void awaitInitialized() {
        awaitInitialized(5 * 60 * 1000);
    }

    private final DoIfStarted<Long, Void> awaitInitializedActor =
            new DoIfStarted<Long, Void>(new Function<Long, Void>() {
                @Override
                public Void apply(Long timeoutMillis) {
                    Uninterruptibles.awaitUninterruptibly(latch, timeoutMillis, TimeUnit.MILLISECONDS);
                    return null;
                }
            });

    public void awaitInitialized(long timeoutMillis) {
        awaitInitializedActor.apply(timeoutMillis);
    }

    public long getAvailabilitySetLastUpdated() {
        return availabilitySet.getLastUpdated();
    }

    private final Callable<AvailabilitySummary> loadAll = new Callable<AvailabilitySummary>() {
        @Override
        public AvailabilitySummary call() throws Exception {
            Set<String> members = manager.getAllNodes();
            Set<String> actives = manager.getActives();
            availabilitySet.updateMemberList(members);
            availabilitySet.updateActives(actives);
            return new AvailabilitySummary(actives.size(), members.size());
        }
    };

    private final Callable<AvailabilitySummary> loadMembers = new Callable<AvailabilitySummary>() {
        @Override
        public AvailabilitySummary call() throws Exception {
            Set<String> members = manager.getAllNodes();
            availabilitySet.updateMemberList(members);
            return new AvailabilitySummary(availabilitySet.countActives(), members.size());
        }
    };

    private final Callable<AvailabilitySummary> loadActives = new Callable<AvailabilitySummary>() {
        @Override
        public AvailabilitySummary call() throws Exception {
            Set<String> actives = manager.getActives();
            availabilitySet.updateActives(actives);
            return new AvailabilitySummary(actives.size(), availabilitySet.countMembers());
        }
    };

    private final FutureCallback<AvailabilitySummary> loadAllCallback =
            new FutureCallback<AvailabilitySummary>() {
                @Override
                public void onSuccess(AvailabilitySummary result) {
                    logger.debug("[{}] availability set all loaded, {}/{} node(s) active",
                            serviceName, result.getActives(), result.getTotal());
                    serviceContext.process(ServiceEvent.UPDATED_ALL);
                    availabilitySet.enableAvailablilitySet();
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.error("[{}] failed to load-all availability set", serviceName, t);
                    serviceContext.process(ServiceEvent.UPDATE_FAILED);
                }
            };

    private final FutureCallback<AvailabilitySummary> loadAvailabilityCallback =
            new FutureCallback<AvailabilitySummary>() {
                @Override
                public void onSuccess(AvailabilitySummary summary) {
                    logger.debug("[{}] availability set loaded, {}/{} node(s) active",
                            serviceName, summary.getActives(), summary.getTotal());
                    serviceContext.process(ServiceEvent.UPDATED);
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.error("[{}] failed to load availability set", serviceName, t);
                    serviceContext.process(ServiceEvent.UPDATE_FAILED);
                }
            };

    private void activateAll() {
        for (String uri : activatedByMe) {
            activate(uri);
        }
    }

    private void processLoadAll() {
        serviceContext.process(ServiceEvent.UPDATING_ALL);
        ListenableFuture<AvailabilitySummary> reload = executor.submit(loadAll);
        Futures.addCallback(reload, loadAllCallback);
    }

    private void processMemberChanged() {
        serviceContext.process(ServiceEvent.UPDATING);
        ListenableFuture<AvailabilitySummary> reload = executor.submit(loadMembers);
        Futures.addCallback(reload, loadAvailabilityCallback);
    }

    private void processActiveChanged() {
        serviceContext.process(ServiceEvent.UPDATING);
        ListenableFuture<AvailabilitySummary> reload = executor.submit(loadActives);
        Futures.addCallback(reload, loadAvailabilityCallback);
    }

    private final Function<ServiceEvent, Void> eventHandler = new Function<ServiceEvent, Void>() {
        @Override
        public Void apply(ServiceEvent event) {
            logger.debug("service mgr event received: {}", event.name());
            serviceContext.process(event);
            switch (event) {
                case CONNECTED:
                    processLoadAll();
                    activateAll();
                    break;
                case MEMBER_CHANGED:
                    processMemberChanged();
                    break;
                case ACTIVE_CHANGED:
                    processActiveChanged();
                    break;
            }
            return null;
        }
    };

    class DoIfStarted<T, F> {
        private Function<T, F> func;

        public DoIfStarted(Function<T, F> func) {
            this.func = func;
        }

        F apply(T input) {
            if (!startedSwitch.get()) {
                throw new ServiceManagerRuntimeException("deamon not started");
            }
            return func.apply(input);
        }
    }

    class DoIfNotStarted<T, F> {
        private Function<T, F> func;

        private DoIfNotStarted(Function<T, F> func) {
            this.func = func;
        }

        F apply(T input) {
            if (startedSwitch.get()) {
                throw new ServiceManagerRuntimeException("deamon is started");
            }
            return func.apply(input);
        }
    }
}
