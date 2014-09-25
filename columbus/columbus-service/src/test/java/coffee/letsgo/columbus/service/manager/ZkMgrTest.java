package coffee.letsgo.columbus.service.manager;

import coffee.letsgo.columbus.service.exception.ServiceMgrException;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;
import junit.framework.Assert;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by xbwu on 9/14/14.
 */
public class ZkMgrTest {
    private ServiceManager manager;
    private String zkConnectStr = "localhost:2181";
    private String svcName;
    private int zkSessionTimeout = 1800000;
    private Random random = new Random(System.currentTimeMillis());

    @Test
    public void testAddRemoveNode() throws ServiceMgrException {
        String node = randomNodeName();
        manager.addNode(node);
        Assert.assertTrue(manager.isMember(node));
        manager.addNode(node);
        Assert.assertTrue(manager.isMember(node));
        manager.removeNode(node);
        Assert.assertFalse(manager.isMember(node));
        manager.removeNode(node);
        Assert.assertFalse(manager.isMember(node));
    }

    @Test
    public void testEnableDisable() throws ServiceMgrException {
        String node = randomNodeName();
        manager.addNode(node);
        manager.activateNode(node);
        Assert.assertTrue(manager.isActive(node));
        manager.activateNode(node);
        Assert.assertTrue(manager.isActive(node));
        manager.deactivateNode(node);
        Assert.assertFalse(manager.isActive(node));
        manager.deactivateNode(node);
        Assert.assertFalse(manager.isActive(node));
    }

    @Test
    public void testAutoDisable() throws ServiceMgrException {
        String node = randomNodeName();
        manager.addNode(node);
        Assert.assertTrue(manager.isMember(node));
        manager.activateNode(node);
        Assert.assertTrue(manager.isActive(node));
        manager.stop();
        manager.start();
        Assert.assertFalse(manager.isActive(node));
        Assert.assertTrue(manager.isMember(node));
    }

    @Test
    public void testGetList() throws ServiceMgrException {
        cleanupService();
        Set<String> nodes = new HashSet<String>();
        Collections.addAll(nodes,
                "testnode_01:7981",
                "testnode_01:7982",
                "testnode_02:7981",
                "testnode_03:80");

        int cnt = 0;
        for (String node : nodes) {
            manager.addNode(node);
            Assert.assertEquals(++cnt, manager.getAllNodes().size());
            Assert.assertEquals(0, manager.getActives().size());
        }
        Assert.assertEquals(nodes, manager.getAllNodes());
        cnt = 0;
        for (String node : nodes) {
            manager.activateNode(node);
            Assert.assertEquals(nodes.size(), manager.getAllNodes().size());
            Assert.assertEquals(++cnt, manager.getActives().size());
        }
        Assert.assertEquals(nodes, manager.getActives());
        manager.stop();
        manager.start();
        Assert.assertEquals(0, manager.getActives().size());
        Assert.assertEquals(nodes.size(), manager.getAllNodes().size());
    }

    @BeforeTest
    public void setup() throws ServiceMgrException {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        svcName = randomServiceName();
        manager = Guice.createInjector(
                new Module() {
                    @Override
                    public void configure(Binder binder) {
                        binder.bind(ServiceManager.class).to(ServiceMgrZkImpl.class);
                        binder.bindConstant().annotatedWith(Names.named("service name")).to(svcName);
                        binder.bindConstant().annotatedWith(Names.named("connect string")).to(zkConnectStr);
                        binder.bindConstant().annotatedWith(Names.named("session timeout")).to(zkSessionTimeout);
                    }
                }
        ).getInstance(ServiceManager.class);
        manager.start();
    }

    @AfterTest
    public void teardown() throws ServiceMgrException {
        manager.stop();
    }

    private String randomServiceName() {
        return String.format("testsvc_%d", random.nextInt(10));
    }

    private String randomNodeName() {
        return String.format("testnode_%d:%d", random.nextInt(50), random.nextInt(100000));
    }

    private void cleanupService() throws ServiceMgrException {
        for (String uri : manager.getActives()) {
            manager.deactivateNode(uri);
        }
        for (String uri : manager.getAllNodes()) {
            manager.removeNode(uri);
        }
    }
}
