package coffee.letsgo.iceflake.client;

import coffee.letsgo.iceflake.config.IceflakeConfigException;
import coffee.letsgo.iceflake.server.IceflakeServer;
import junit.framework.Assert;
import org.apache.thrift.TException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.UnknownHostException;

/**
 * Created by xbwu on 9/23/14.
 */
public class IceflakeClientTest {
    private final int serverId = 9;
    private final int serverPort = 8089;

    private IceflakeServer server;
    private IceflakeClient client;

    @Test
    public void testIdGeneration() throws TException {
        Assert.assertTrue(client.generateId(IdType.ACCT_ID) > 0);
    }

    @BeforeTest
    public void setup() throws IceflakeClientException, IceflakeConfigException, UnknownHostException {
        server = new IceflakeServer(serverId, serverPort);
        server.start();
        client = IceflakeClient.getInstance();
    }

    @AfterTest
    public void teardown() {
        server.shutdown();
        server = null;
        client = null;
    }
}
