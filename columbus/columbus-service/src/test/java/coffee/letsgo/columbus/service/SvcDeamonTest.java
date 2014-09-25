package coffee.letsgo.columbus.service;

import coffee.letsgo.columbus.service.exception.ServiceException;
import coffee.letsgo.columbus.service.manager.ServiceManager;
import coffee.letsgo.columbus.service.manager.ServiceManagerZookeeperImpl;
import com.google.common.base.Function;
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
public class SvcDeamonTest {
    private ServiceDeamon deamon;
    private AvailabilitySet availabilitySet;
    private String zkConnectStr = "localhost:2181";
    private String svcName;
    private int zkSessionTimeout = 1800000;
    private Random random = new Random(System.currentTimeMillis());

    private long untilTimeout = 2000;
    private UntilCondition untilMembership = new UntilCondition<String, Boolean>(new Function<String, Boolean>() {
        @Override
        public Boolean apply(String input) {
            return deamon.isMember(input);
        }
    }, untilTimeout);

    private UntilCondition untilActive = new UntilCondition<String, Boolean>(new Function<String, Boolean>() {
        @Override
        public Boolean apply(String input) {
            return availabilitySet.isActive(input);
        }
    }, untilTimeout);

    private UntilCondition untilCountMember = new UntilCondition<Void, Integer>(new Function<Void, Integer>() {
        @Override
        public Integer apply(Void input) {
            return availabilitySet.countMembers();
        }
    }, untilTimeout);

    private UntilCondition untilCountActive = new UntilCondition<Void, Integer>(new Function<Void, Integer>() {
        @Override
        public Integer apply(Void input) {
            return availabilitySet.countActives();
        }
    }, untilTimeout);

    private UntilCondition untilMembers = new UntilCondition<Void, Set<String>>(new Function<Void, Set<String>>() {
        @Override
        public Set<String> apply(Void input) {
            return availabilitySet.getMembers();
        }
    }, untilTimeout);

    private UntilCondition untilActives = new UntilCondition<Void, Set<String>>(new Function<Void, Set<String>>() {
        @Override
        public Set<String> apply(Void input) {
            return availabilitySet.getActives();
        }
    }, untilTimeout);

    @Test
    public void testActivate() throws ServiceException {
        String node = randomServiceName();
        deamon.addMember(node);
        Assert.assertTrue(untilMembership.get(node, true));
        deamon.activate(node);
        Assert.assertTrue(untilActive.get(node, true));
        deamon.deactivate(node);
        Assert.assertTrue(untilActive.get(node, false));
        deamon.removeMember(node);
        Assert.assertTrue(untilMembership.get(node, false));
    }

    @Test
    public void testAvailibility() throws ServiceException, InterruptedException {
        cleanupService();
        Set<String> nodes = new HashSet<String>();
        Collections.addAll(nodes,
                "testnode_01:7981",
                "testnode_01:7982",
                "testnode_02:7981",
                "testnode_03:80");
        for (String uri : nodes) {
            deamon.addMember(uri);
        }
        Assert.assertTrue(untilCountMember.get(null, nodes.size()));
        Assert.assertTrue(untilCountActive.get(null, 0));
        Assert.assertTrue(untilMembers.get(null, nodes));
        for (String uri : nodes) {
            deamon.activate(uri);
        }
        Assert.assertTrue(untilCountActive.get(null, nodes.size()));
        Assert.assertTrue(untilActives.get(null, nodes));
        deamon.shutdown();
        deamon.start();
        deamon.awaitInitialized();
        Assert.assertTrue(untilCountActive.get(null, 0));
        Assert.assertTrue(untilCountMember.get(null, nodes.size()));
    }

    @BeforeTest
    public void setup() throws ServiceException {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        svcName = randomServiceName();
        deamon = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ServiceManager.class).to(ServiceManagerZookeeperImpl.class);
                binder.bindConstant().annotatedWith(Names.named("service name")).to(svcName);
                binder.bindConstant().annotatedWith(Names.named("connect string")).to(zkConnectStr);
                binder.bindConstant().annotatedWith(Names.named("session timeout")).to(zkSessionTimeout);
            }
        }).getInstance(ServiceDeamon.class);
        deamon.start();
        deamon.awaitInitialized();
        availabilitySet = deamon.getAvailabilitySet();
    }

    @AfterTest
    public void teardown() throws ServiceException {
        deamon.shutdown();
    }

    private String randomServiceName() {
        return String.format("testsvc_%d", random.nextInt(10));
    }

    private String randomNodeName() {
        return String.format("testnode_%d:%d", random.nextInt(50), random.nextInt(100000));
    }

    private void cleanupService() throws ServiceException {
        for (String uri : availabilitySet.getActives()) {
            deamon.deactivate(uri);
        }
        for (String uri : availabilitySet.getMembers()) {
            deamon.removeMember(uri);
        }
        Assert.assertTrue(untilCountActive.get(null, 0));
        Assert.assertTrue(untilCountMember.get(null, 0));
    }

    class UntilCondition<T, F> {
        private Function<T, F> func;
        long timeoutMillis;

        public UntilCondition(Function<T, F> func, long timeoutMillis) {
            this.func = func;
            this.timeoutMillis = timeoutMillis;
        }

        public boolean get(T input, F expected) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                if (func.apply(input).equals(expected)) {
                    return true;
                }
            }
            return false;
        }
    }
}
