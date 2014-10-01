package coffee.letsgo.iceflake.client;

import coffee.letsgo.columbus.client.swift.SwiftClient;
import coffee.letsgo.iceflake.Iceflake;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xbwu on 9/4/14.
 */
public class IceflakeClient {
    private static class IceflakeProxyHolder {
        private static final Iceflake Instance = buildServiceProxy();
    }

    private static final Logger logger = LoggerFactory.getLogger(IceflakeClient.class);

    public static Iceflake getInstance() {
        if (IceflakeProxyHolder.Instance == null) {
            throw new IceflakeClientException("crap iceflake client");
        }
        return IceflakeProxyHolder.Instance;
    }

    private IceflakeClient() {
    }

    private static Iceflake buildServiceProxy() {
        try {
            return new SwiftClient<Iceflake>("iceflake")
                    .createClient(Iceflake.class).get();
        } catch (Exception ex) {
            logger.error("failed to create client proxy", ex);
            return null;
        }
    }
}
