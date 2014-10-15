package coffee.letsgo.common;

import coffee.letsgo.common.exception.NotFoundException;
import coffee.letsgo.common.StorageManagement.StorageManagement;
import coffee.letsgo.common.StorageManagement.StorageManagementLRUImpl;
import coffee.letsgo.common.exception.ZookeeperException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by xbwu on 10/13/14.
 */
public class ZookeeperReader {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperReader.class);
    private final StorageManagement<String, String> cache = new StorageManagementLRUImpl<String, String>(4096);
    private final String connectString;
    private ZooKeeper zk;

    public ZookeeperReader(String connectString) {
        this.connectString = connectString;
        buildZookeeperClient();
    }

    public String read(String path) {
        String val;
        try {
            val = cache.get(path);
        } catch (NotFoundException ex) {
            val = readFromZookeeper(path);
            cache.put(path, val);
        }
        return val;
    }

    private String readFromZookeeper(final String path) {
        if (zk == null) {
            synchronized (connectString) {
                if (zk == null) {
                    buildZookeeperClient();
                }
            }
        }
        if (zk == null) {
            throw new ZookeeperException("not able to obtain zookeeper client instance");
        }
        try {
            return new String(zk.getData(path, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeDataChanged ||
                            watchedEvent.getType() == Event.EventType.NodeDeleted) {
                        if (watchedEvent.getPath().equals(path)) {
                            logger.info("invalid data {}", path);
                            cache.invalid(path);
                        }
                    }
                }
            }, null));
        } catch (KeeperException ex) {
            logger.error("failed to read data of path " + path, ex);
            throw new ZookeeperException("keeper exception caught", ex);
        } catch (InterruptedException ex) {
            logger.error("failed to read data of path " + path, ex);
            throw new ZookeeperException("interrupted exception caught", ex);
        }
    }

    private void buildZookeeperClient() {
        logger.info("building zookeeper client");
        try {
            zk = new ZooKeeper(connectString, 30 * 60 * 1000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    logger.debug("zookeeper event: {}", watchedEvent.getType().name());
                    switch (watchedEvent.getType()) {
                        case None:
                            processSystemEvent(watchedEvent);
                            break;
                    }
                }

                private void processSystemEvent(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getState()) {
                        case SyncConnected:
                            logger.info("zk connected");
                            break;
                        case Disconnected:
                            logger.warn("zk disconnected");
                            cache.clear();
                            break;
                        case Expired:
                            logger.warn("zk connection expired");
                            cache.clear();
                            buildZookeeperClient();
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (IOException ex) {
            logger.error("failed to establish zk connection", ex);
            zk = null;
        }
    }
}
