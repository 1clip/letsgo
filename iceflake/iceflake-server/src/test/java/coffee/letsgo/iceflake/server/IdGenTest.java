package coffee.letsgo.iceflake.server;

import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.config.IceflakeConfigException;
import coffee.letsgo.iceflake.config.IceflakeWorkerConfig;
import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.net.HostAndPort;
import org.apache.thrift.TException;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.concurrent.*;

/**
 * Created by xbwu on 8/30/14.
 */
public class IdGenTest {
    private IceflakeWorkerConfig config;
    private IceflakeServer server;
    private IdType testIdType = IdType.ACCT_ID;

    @Test
    public void testIdGeneration() throws ExecutionException, InterruptedException, TException {
        Iceflake client = buildClient();
        Assert.assertEquals(client.getWorkerId(), config.getId());
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
        HashSet<Long> hs = new HashSet<>(bucket);
        Assert.assertEquals(hs.size(), threads * tries);
    }

    @BeforeTest
    public void setup() throws IceflakeConfigException, UnknownHostException {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        config = new IceflakeWorkerConfig("localhost", 7609, 11);
        server = new IceflakeServer(config.getId(), config.getPort());
        server.start();
    }

    @AfterTest
    public void teardown() {
        server.shutdown();
    }

    private Iceflake buildClient() throws ExecutionException, InterruptedException {
        ThriftClientManager clientManager = new ThriftClientManager();
        return clientManager.createClient(
                new FramedClientConnector(HostAndPort.fromParts(
                        config.getWorker(),
                        config.getPort())),
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
