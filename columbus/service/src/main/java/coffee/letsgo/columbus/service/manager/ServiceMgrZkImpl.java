package coffee.letsgo.columbus.service.manager;

import coffee.letsgo.columbus.service.exception.ServiceMgrRuntimeException;
import coffee.letsgo.columbus.service.model.ServiceEvent;
import coffee.letsgo.columbus.service.util.Configuration;
import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/7/14.
 */
public class ServiceMgrZkImpl implements ServiceManager {
    private final String serviceName;
    private final String connectionString;
    private final int sessionTimeout;

    private final String zkColumbusRoot = "/columbus";
    private final String zkServicesRoot = zkColumbusRoot + "/services";
    private final String zkNodeService;
    private final String zkNodeMembers;
    private final String zkNodeActives;

    private final List<Function<ServiceEvent, Void>> eventHandlers =
            new ArrayList<Function<ServiceEvent, Void>>();

    private final AtomicBoolean zkConnected = new AtomicBoolean(false);

    private final Logger logger = LoggerFactory.getLogger(ServiceMgrZkImpl.class);

    private final String zkConnectStrKey = "columbus.service.zk.ConnectString";
    private final String zkConnectTimeout = "columbus.service.zk.ConnectTimeout";
    private final String zkConnectStrDefaultVal = "localhost:2181";
    private final String zkConnectTimeoutDefaultVal = "300000";

    private ZooKeeper zk = null;

    @Inject
    public ServiceMgrZkImpl(@Named("service name") String serviceName) {
        this.serviceName = serviceName;
        this.connectionString = verifyNotNull(Configuration.read(zkConnectStrKey, zkConnectStrDefaultVal));
        this.sessionTimeout = Integer.parseInt(verifyNotNull(Configuration.read(zkConnectTimeout, zkConnectTimeoutDefaultVal)));
        this.zkNodeService = this.zkServicesRoot + "/" + this.serviceName;
        this.zkNodeMembers = this.zkNodeService + "/mbr";
        this.zkNodeActives = this.zkNodeService + "/act";
    }

    @Override
    public boolean addNode(String uri) {
        return new DoWithZkConnected<String, Boolean>(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String uri) {
                String p = getMemberPath(uri);
                if (pathExists(p)) {
                    logger.debug("[{}] already a member of service [{}]", uri, serviceName);
                    return false;
                }
                logger.info("adding [{}] as a member of service [{}]", uri, serviceName);
                try {
                    zk.create(p, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    return true;
                } catch (Exception e) {
                    if (e instanceof KeeperException &&
                            ((KeeperException) e).code() == KeeperException.Code.NODEEXISTS) {
                        logger.debug("[{}] already a member of service [{}]", uri, serviceName);
                        return false;
                    } else {
                        throw new ServiceMgrRuntimeException(
                                String.format("failed to add [%s] as member of service [%s]", uri, serviceName),
                                e);
                    }
                }
            }
        }).apply(uri);
    }

    @Override
    public boolean removeNode(String uri) {
        return new DoWithZkConnected<String, Boolean>(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String uri) {
                String p = getMemberPath(uri);
                if (!pathExists(p)) {
                    logger.debug("[{}] not a member of service [{}]", uri, serviceName);
                    return false;
                }
                logger.info("deleting [{}] from service [{}]", uri, serviceName);
                try {
                    zk.delete(p, -1);
                    return true;
                } catch (Exception e) {
                    if (e instanceof KeeperException &&
                            ((KeeperException) e).code() == KeeperException.Code.NONODE) {
                        logger.debug("[{}] not a member of service [{}]", uri, serviceName);
                        return false;
                    } else {
                        throw new ServiceMgrRuntimeException(
                                String.format("failed to delete [%s] from service [%s]", uri, serviceName),
                                e);
                    }
                }
            }
        }).apply(uri);
    }

    @Override
    public boolean isMember(String uri) {
        return pathExists(getMemberPath(uri));
    }

    @Override
    public boolean activateNode(String uri) {
        return new DoWithZkConnected<String, Boolean>(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String uri) {
                if (!isMember(uri)) {
                    throw new ServiceMgrRuntimeException("[%s] is not a member of service [%s], could not activate",
                            uri, serviceName);
                }
                String p = getActivePath(uri);
                if (pathExists(p)) {
                    logger.debug("[{}] is already active for service [{}]", uri, serviceName);
                    return false;
                }
                logger.info("activate [{}] for service [{}]", uri, serviceName);
                try {
                    zk.create(p, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    return true;
                } catch (Exception e) {
                    if (e instanceof KeeperException &&
                            ((KeeperException) e).code() == KeeperException.Code.NODEEXISTS) {
                        logger.debug("[{}] is already active for service [{}]", uri, serviceName);
                        return false;
                    } else {
                        throw new ServiceMgrRuntimeException(
                                String.format("failed to activate [%s] for service [%s]", uri, serviceName),
                                e);
                    }
                }
            }
        }).apply(uri);
    }

    @Override
    public boolean deactivateNode(String uri) {
        return new DoWithZkConnected<String, Boolean>(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String uri) {
                if (!isMember(uri)) {
                    logger.warn("deactivating node [{}] from service [{}], which is not a member of",
                            uri, serviceName);
                }
                String p = getActivePath(uri);
                if (!pathExists(p)) {
                    logger.debug("[{}] is not active for service [{}]", uri, serviceName);
                    return false;
                }
                logger.info("deactivate [{}] from service [{}]", uri, serviceName);
                try {
                    zk.delete(p, -1);
                    return true;
                } catch (Exception e) {
                    if (e instanceof KeeperException &&
                            ((KeeperException) e).code() == KeeperException.Code.NONODE) {
                        logger.debug("[{}] is not active for service [{}]", uri, serviceName);
                        return false;
                    } else {
                        throw new ServiceMgrRuntimeException(
                                String.format("failed to deactivate [%s] from service [%s]", uri, serviceName),
                                e);
                    }
                }
            }
        }).apply(uri);
    }

    @Override
    public boolean isActive(String uri) {
        return pathExists(getActivePath(uri));
    }

    @Override
    public Set<String> getAllNodes() {
        return new DoWithZkConnected<Void, Set<String>>(new Function<Void, Set<String>>() {
            @Override
            public Set<String> apply(Void input) {
                List<String> children;
                try {
                    children = zk.getChildren(zkNodeMembers, true);
                } catch (Exception e) {
                    if (e instanceof KeeperException &&
                            ((KeeperException) e).code() == KeeperException.Code.NONODE) {
                        logger.debug("no member found for service [{}]", serviceName);
                        return new HashSet<String>();
                    }
                    throw new ServiceMgrRuntimeException(
                            String.format("failed to get member nodes of service [%s]", serviceName),
                            e);
                }
                return new HashSet<String>(children);
            }
        }).apply(null);
    }

    @Override
    public Set<String> getActives() {
        return new DoWithZkConnected<Void, Set<String>>(new Function<Void, Set<String>>() {
            @Override
            public Set<String> apply(Void input) {
                List<String> children;
                try {
                    children = zk.getChildren(zkNodeActives, true);
                } catch (Exception e) {
                    if (e instanceof KeeperException &&
                            ((KeeperException) e).code() == KeeperException.Code.NONODE) {
                        logger.debug("no active member found for service [{}]", serviceName);
                        return new HashSet<String>();
                    }
                    throw new ServiceMgrRuntimeException(
                            String.format("failed to get actives of service [%s]", serviceName),
                            e);
                }
                return new HashSet<String>(children);
            }
        }).apply(null);
    }

    @Override
    public void start() {
        logger.debug("starting service manager");
        try {
            zk = new ZooKeeper(connectionString, sessionTimeout, eventWatcher);
        } catch (Exception e) {
            throw new ServiceMgrRuntimeException(
                    String.format("failed to connect to zk server [%s]", connectionString),
                    e);
        }
        logger.info("service manager started");
    }

    @Override
    public void stop() {
        if (zk == null) {
            logger.error("service manager not started");
            return;
        }
        try {
            zk.close();
            zk = null;
        } catch (InterruptedException e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void addEventHandler(Function<ServiceEvent, Void> handler) {
        eventHandlers.add(handler);
    }

    private void verifyZkStructure() {
        for (String node : new String[]{
                zkColumbusRoot,
                zkServicesRoot,
                zkNodeService,
                zkNodeMembers,
                zkNodeActives}) {
            verifyZkPathExists(node);
        }
    }

    private void verifyZkPathExists(String path) {
        logger.debug("verify zk path [{}] exists", path);
        if (pathExists(path)) {
            logger.debug("zk path [{}] exists", path);
            return;
        }
        try {
            zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            if (e instanceof KeeperException &&
                    ((KeeperException) e).code() == KeeperException.Code.NODEEXISTS) {
                logger.debug("zk path [{}] already exists", path);
            } else {
                throw new ServiceMgrRuntimeException(
                        String.format("failed to create zk path [%s]", path),
                        e);
            }
        }
        logger.info("zk path [{}] created", path);
    }

    private String getMemberPath(String uri) {
        return zkNodeMembers + "/" + verifyNotNull(uri).trim().toLowerCase();
    }

    private String getActivePath(String uri) {
        return zkNodeActives + "/" + verifyNotNull(uri).trim().toLowerCase();
    }

    private boolean pathExists(String path) {
        try {
            return zk.exists(path, false) != null;
        } catch (Exception e) {
            logger.error("failed to verify zk path " + path, e);
            return false;
        }
    }

    private void notifyEvent(ServiceEvent event) {
        for (Function<ServiceEvent, Void> handler : eventHandlers) {
            try {
                handler.apply(event);
            } catch (Exception e) {
                logger.error("failed to handle event {}", event.name());
            }
        }
    }

    Watcher eventWatcher = new Watcher() {
        @Override
        public void process(WatchedEvent watchedEvent) {
            switch (watchedEvent.getType()) {
                case None:
                    processSystemEvent(watchedEvent);
                    break;
                case NodeCreated:
                    processNodeCreatedEvent(watchedEvent);
                    break;
                case NodeDeleted:
                    processNodeDeletedEvent(watchedEvent);
                    break;
                case NodeDataChanged:
                    processNodeDatachangedEvent(watchedEvent);
                    break;
                case NodeChildrenChanged:
                    processChildrenChangedEvent(watchedEvent);
                    break;
            }
        }

        private void processSystemEvent(WatchedEvent watchedEvent) {
            switch (watchedEvent.getState()) {
                case SyncConnected:
                    logger.info("zk connected");
                    zkConnected.set(true);
                    verifyZkStructure();
                    notifyEvent(ServiceEvent.CONNECTED);
                    break;
                case Disconnected:
                    logger.warn("zk disconnected");
                    zkConnected.set(false);
                    notifyEvent(ServiceEvent.DISCONNECTED);
                    break;
                case Expired:
                    logger.warn("zk connection expired");
                    zkConnected.set(false);
                    notifyEvent(ServiceEvent.EXPIRED);
                    break;
                default:
                    break;
            }
        }

        private void processNodeCreatedEvent(WatchedEvent watchedEvent) {

        }

        private void processNodeDeletedEvent(WatchedEvent watchedEvent) {

        }

        private void processNodeDatachangedEvent(WatchedEvent watchedEvent) {

        }

        private void processChildrenChangedEvent(WatchedEvent watchedEvent) {
            new DoWithZkConnected<WatchedEvent, Void>(new Function<WatchedEvent, Void>() {
                @Override
                public Void apply(WatchedEvent watchedEvent) {
                    if (watchedEvent.getPath().equalsIgnoreCase(zkNodeMembers)) {
                        logger.debug("member change event received");
                        notifyEvent(ServiceEvent.MEMBER_CHANGED);
                    } else if (watchedEvent.getPath().equalsIgnoreCase(zkNodeActives)) {
                        logger.debug("active change event received");
                        notifyEvent(ServiceEvent.ACTIVE_CHANGED);
                    }
                    return null;
                }
            }).apply(watchedEvent);
        }
    };

    class DoWithZkConnected<T, F> {
        private Function<T, F> func;
        private long timeoutMillis;
        private Set<KeeperException.Code> tolerableExceptions = new HashSet<KeeperException.Code>();

        public DoWithZkConnected(Function<T, F> func) {
            this(func, 10 * 1000);
        }

        public DoWithZkConnected(Function<T, F> func, long timeoutMillis) {
            this.func = func;
            this.timeoutMillis = timeoutMillis;
            tolerableExceptions.add(KeeperException.Code.SESSIONEXPIRED);
            tolerableExceptions.add(KeeperException.Code.CONNECTIONLOSS);
        }

        F apply(T input) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                if (zkConnected.get() && zk != null) {
                    try {
                        return func.apply(input);
                    } catch (Exception e) {
                        Throwable cause = e;
                        while (cause != null) {
                            if (cause instanceof KeeperException && tolerableExceptions.contains(
                                    ((KeeperException) cause).code())) {
                                break;
                            }
                            cause = cause.getCause();
                        }
                        if (cause != null) {
                            logger.info("connection not ready, retry...");
                        } else {
                            throw new ServiceMgrRuntimeException(e);
                        }
                    }
                }
            }
            throw new ServiceMgrRuntimeException("zk never connected");
        }
    }
}
