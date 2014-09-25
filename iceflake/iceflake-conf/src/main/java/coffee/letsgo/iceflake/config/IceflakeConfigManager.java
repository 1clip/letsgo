package coffee.letsgo.iceflake.config;

import coffee.letsgo.common.ConfigurationReader;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xbwu on 8/29/14.
 */
public class IceflakeConfigManager {
    private static class IceflakeConfigHolder {
        private static final IceflakeConfigManager Instance = new IceflakeConfigManager();
    }

    private IceflakeConfigManager() {
    }

    public static IceflakeConfigManager getInstance() {
        return IceflakeConfigHolder.Instance;
    }

    private final String serviceName = "iceflake";
    private final ConfigurationReader configurationReader = new ConfigurationReader(serviceName);
    private final String defaultZookeeperConnectString = "localhost:2181";
    private final String defaultZookeeperConnectTimeout = "30000";
    private final String zookeeperConnectStringPropertyKey = "iceflake.conf.zk.ConnectString";
    private final String zookeeperConnectTimeoutPropertyKey = "iceflake.conf.zk.ConnectTimeout";
    private final String iceflakeWorkersZookeeperPath = "/iceflake/workers";
    /**
     * zk path structure
     * /iceflake/workers/:worker_name/:server_port/:worker_id
     */

    private final Logger logger = LoggerFactory.getLogger(IceflakeConfigManager.class);
    private final ConcurrentMap<String, IceflakeWorkerConfig> configDict =
            new ConcurrentHashMap<String, IceflakeWorkerConfig>();

    public String getServiceName() {
        return serviceName;
    }

    public IceflakeWorkerConfig getWorkerConfig(String server) throws IceflakeConfigException {
        if (!configDict.containsKey(server)) {
            configDict.putIfAbsent(server, loadConfigFromZookeeper(server));
        }
        return configDict.get(server);
    }

    private IceflakeWorkerConfig loadConfigFromZookeeper(String server) throws IceflakeConfigException {
        String zkConnectString = configurationReader.read(
                zookeeperConnectStringPropertyKey, defaultZookeeperConnectString);
        int zkConnectTimeout = Integer.parseInt(configurationReader.read(
                zookeeperConnectTimeoutPropertyKey, defaultZookeeperConnectTimeout));
        ZooKeeper zk;
        try {
            zk = new ZooKeeper(zkConnectString, zkConnectTimeout, null);
        } catch (IOException ex) {
            throw new IceflakeConfigException("failed to obtain zookeeper connection", ex);
        }
        String serverPath = String.format("%s/%s", iceflakeWorkersZookeeperPath, server);
        List<String> strPorts = null;
        try {
            strPorts = zk.getChildren(serverPath, false);
        } catch (Exception ex) {
            throw new IceflakeConfigException("failed to get iceflake worker config from zookeeper for node " + server, ex);
        }
        if (strPorts == null || strPorts.isEmpty()) {
            throw new IceflakeConfigException("iceflake worker %s doesn't config with server port", server);
        }
        if (strPorts.size() > 1) {
            logger.warn("iceflake worker {} configured with multiple ports, using any of them -> {}", server, strPorts.get(0));
        }
        String strPort = strPorts.get(0);
        int port;
        try {
            port = Integer.parseInt(strPort);
        } catch (NumberFormatException ex) {
            throw new IceflakeConfigException("iceflake worker %s configured with not numeral port %s", server, strPort);
        }
        String portPath = String.format("%s/%s", serverPath, strPort);
        byte[] btId = null;
        try {
            btId = zk.getData(portPath, false, null);
        } catch (Exception ex) {
            throw new IceflakeConfigException("failed to get iceflake worker id for %s:%s", server, strPort);
        }
        int id;
        try {
            id = Integer.parseInt(new String(btId, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new IceflakeConfigException("iceflake worker id for %s:%s not encoded with UTF-8");
        } catch (NumberFormatException ex) {
            throw new IceflakeConfigException("iceflake worker id for %s:%s not numeral", server, strPort);
        }
        return new IceflakeWorkerConfig(server, port, id);
    }
}

