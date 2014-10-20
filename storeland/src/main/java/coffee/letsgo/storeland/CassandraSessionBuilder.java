package coffee.letsgo.storeland;

import coffee.letsgo.common.ConfigurationReader;
import coffee.letsgo.common.ZookeeperReader;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * Created by xbwu on 10/15/14.
 */
public class CassandraSessionBuilder {
    private final String clusterName;
    private final ConfigurationReader configurationReader;
    private final ZookeeperReader zookeeperReader;
    private final String cassandraPropFile = "cassandra";
    private final String zkCassandraClusterPath = "/cassandra/cluster";

    public CassandraSessionBuilder(String clusterName) {
        this.clusterName = clusterName;
        configurationReader = new ConfigurationReader(cassandraPropFile);
        zookeeperReader = new ZookeeperReader(configurationReader.read(zkCassandraClusterPath, "localhost:2181"));
    }

    public Session build() {
        return Cluster.builder()
                .addContactPoints(getNodes())
                .build()
                .newSession();
    }

    private String[] getNodes() {
        return (String[]) zookeeperReader.getChildren(
                String.format("%s/%s/nodes", zkCassandraClusterPath, clusterName)).toArray();
    }
}
