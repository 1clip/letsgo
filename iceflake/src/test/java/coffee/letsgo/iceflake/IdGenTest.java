package coffee.letsgo.iceflake;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.swift.service.ThriftClientManager;
import com.facebook.swift.service.ThriftServer;
import com.google.common.net.HostAndPort;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.apache.thrift.TException;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.concurrent.*;

/**
 * Created by xbwu on 8/30/14.
 */
public class IdGenTest {
    private IceflakeConfig config;
    private ThriftServer server;
    private int testIdType = 89;

    @Test
    public void testIdGeneration() throws ExecutionException, InterruptedException, TException {
        Iceflake client = buildClient();
        Assert.assertEquals(client.getWorkerId(), config.getWorkerId());
        long t1 = System.currentTimeMillis();
        long t = client.getTimestamp();
        long t2 = System.currentTimeMillis();
        Assert.assertTrue(t >= t1 && t <= t2);
        Assert.assertTrue(client.getId(testIdType) > 0L);
    }

    @Test
    public void testIdUniqueness() throws InterruptedException {
        BlockingQueue<Long> bucket = new LinkedBlockingDeque<Long>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        int threads = 10, tries = 1000;
        for (int i = 0; i < threads; ++i) {
            pool.execute(new IdTester(bucket, tries));
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        Assert.assertEquals(bucket.size(), threads * tries);
        HashSet<Long> hs = new HashSet<>();
        while (!bucket.isEmpty()) {
            hs.add(bucket.poll());
        }
        Assert.assertEquals(hs.size(), threads * tries);
    }

    @Test
    public void testInvalidIdType() throws ExecutionException, InterruptedException {
        Iceflake client = buildClient();
        try{
            client.getId(-1);
            Assert.assertEquals(true, false);
        } catch (TException e) {
            Assert.assertEquals(e.getMessage(), "Internal error processing getId");
        }

        try{
            client.getId(1 << 7);
            Assert.assertEquals(true, false);
        } catch (TException e) {
            Assert.assertEquals(e.getMessage(), "Internal error processing getId");
        }
    }

    @BeforeTest
    public void setup() {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        config = Guice.createInjector(
                new Module() {
                    @Override
                    public void configure(Binder binder) {
                        binder.bind(IceflakeConfig.class).to(DevConfig.class);
                    }
                }
        ).getInstance(IceflakeConfig.class);

        server = config.apply().start();
    }

    @AfterTest
    public void teardown() {
        server.close();
    }

    private Iceflake buildClient() throws ExecutionException, InterruptedException {
        ThriftClientManager clientManager = new ThriftClientManager();
        return clientManager.createClient(
                new FramedClientConnector(HostAndPort.fromParts("localhost", config.getServerPort())),
                Iceflake.class).get();
    }

    class IdTester implements Runnable {
        private BlockingQueue<Long> bucket;
        private int tries;

        public IdTester(BlockingQueue<Long> bucket, int tries) {
            this.bucket = bucket;
            this.tries = tries;
        }

        @Override
        public void run() {
            Iceflake client;
            try {
                client = buildClient();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            for (int i = 0; i < tries; ++i) {
                long id;
                try {
                    id = client.getId(testIdType);
                } catch (TException e) {
                    e.printStackTrace();
                    continue;
                }
                bucket.offer(id);
            }
        }
    }
}
