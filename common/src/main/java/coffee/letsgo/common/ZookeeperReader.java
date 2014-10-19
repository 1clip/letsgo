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
import java.util.List;

/**
 * Created by xbwu on 10/13/14.
 */
public class ZookeeperReader {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperReader.class);
    private final StorageManagement<String, String> dataCache = new StorageManagementLRUImpl<String, String>(4096);
    private final StorageManagement<String, List<String>> childrenCache = new StorageManagementLRUImpl<String, List<String>>(4096);
    private final ZookeeperClientHolder zookeeperClientHolder;

    public ZookeeperReader(String connectString) {
        zookeeperClientHolder = new ZookeeperClientHolder(connectString);
    }

    public String getData(String path) {
        String val;
        try {
            val = dataCache.get(path);
        } catch (NotFoundException ex) {
            val = readDataFromZookeeper(path);
            dataCache.put(path, val);
        }
        return val;
    }

    public List<String> getChildren(String path) {
        List<String> val;
        try {
            val = childrenCache.get(path);
        } catch (NotFoundException ex) {
            val = readChildrenFromZookeeper(path);
            childrenCache.put(path, val);
        }
        return val;
    }

    private String readDataFromZookeeper(final String path) {
        try {
            return new String(zookeeperClientHolder.getInstance().getData(path, true, null));
        } catch (KeeperException ex) {
            logger.error("failed to read data of path " + path, ex);
            throw new ZookeeperException("keeper exception caught", ex);
        } catch (InterruptedException ex) {
            logger.error("failed to read data of path " + path, ex);
            throw new ZookeeperException("interrupted exception caught", ex);
        }
    }

    private List<String> readChildrenFromZookeeper(final String path) {
        try {
            return zookeeperClientHolder.getInstance().getChildren(path, true);
        } catch (KeeperException ex) {
            logger.error("failed to read data of path " + path, ex);
            throw new ZookeeperException("keeper exception caught", ex);
        } catch (InterruptedException ex) {
            logger.error("failed to read data of path " + path, ex);
            throw new ZookeeperException("interrupted exception caught", ex);
        }
    }

    private class ZookeeperClientHolder {
        private final String connectString;
        private ZooKeeper zk = null;

        public ZookeeperClientHolder(String connectString) {
            this.connectString = connectString;
        }

        ZooKeeper getInstance() {
            if (zk == null) {
                synchronized (this) {
                    buildZookeeperClient();
                }
            }
            if (zk == null) {
                throw new ZookeeperException("not able to obtain zookeeper client instance");
            }
            return zk;
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
                            case NodeDataChanged:
                                processDataChanged(watchedEvent);
                                break;
                            case NodeDeleted:
                                processNodeDeleted(watchedEvent);
                                break;
                            case NodeChildrenChanged:
                                processChildrenChanged(watchedEvent);
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
                                dataCache.clear();
                                break;
                            case Expired:
                                logger.warn("zk connection expired");
                                dataCache.clear();
                                buildZookeeperClient();
                                break;
                            default:
                                break;
                        }
                    }

                    private void processDataChanged(WatchedEvent watchedEvent) {
                        String path = watchedEvent.getPath();
                        if (dataCache.contains(path)) {
                            try {
                                readDataFromZookeeper(path);
                            } catch (Exception ex) {
                                dataCache.invalid(path);
                            }
                        }
                    }

                    private void processNodeDeleted(WatchedEvent watchedEvent) {
                        String path = watchedEvent.getPath();
                        if (dataCache.contains(path)) {
                            dataCache.invalid(path);
                        }
                        if (childrenCache.contains(path)) {
                            childrenCache.invalid(path);
                        }
                    }

                    private void processChildrenChanged(WatchedEvent watchedEvent) {
                        String path = watchedEvent.getPath();
                        if (childrenCache.contains(path)) {
                            try {
                                readChildrenFromZookeeper(path);
                            } catch (Exception ex) {
                                childrenCache.invalid(path);
                            }
                        }
                    }
                });
            } catch (IOException ex) {
                logger.error("failed to establish zk connection", ex);
                zk = null;
            }
        }
    }
}
