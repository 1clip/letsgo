package coffee.letsgo.iceflake.client;

import coffee.letsgo.columbus.client.swift.SwiftClient;
import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.config.IceflakeConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xbwu on 9/4/14.
 */
public class IceflakeClient {
    private static class IceflakeClientHolder {
        private static final IceflakeClient Instance = new IceflakeClient();
    }

    private Iceflake clientProxy;
    private final Logger logger = LoggerFactory.getLogger(IceflakeClient.class);

    public static IceflakeClient getInstance() throws IceflakeClientException {
        if(IceflakeClientHolder.Instance.isCrap()) {
            throw new IceflakeClientException("crap iceflake client");
        }
        return IceflakeClientHolder.Instance;
    }

    private IceflakeClient() {
        try {
            clientProxy = new SwiftClient<Iceflake>(IceflakeConstants.serviceName).createClient(Iceflake.class).get();
        } catch (Exception ex) {
            logger.error("failed to create client proxy", ex);
            clientProxy = null;
        }
    }

    private boolean isCrap() {
        return clientProxy == null;
    }

    public long id(IdType idType) throws TException {
        return clientProxy.getId(idType.get_value());
    }
}
