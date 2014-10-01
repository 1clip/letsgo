package coffee.letsgo.identity.client;

import coffee.letsgo.columbus.client.swift.SwiftClient;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.User;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yfang on 9/4/14.
 */
public class IdentityClient {
    private static class IdentityClientHolder {
        private static final IdentityClient Instance = new IdentityClient();
    }

    private IdentityService clientProxy;
    private final Logger logger = LoggerFactory.getLogger(IdentityClient.class);

    public static IdentityClient getInstance()  {
        return IdentityClientHolder.Instance;
    }

    private IdentityClient() {
        try {
            clientProxy = new SwiftClient<IdentityService>("identity")
                    .createClient(IdentityService.class).get();
        } catch (Exception ex) {
            logger.error("failed to create client proxy", ex);
            clientProxy = null;
        }
    }

    public User getUser(long userId) throws TException {
        return clientProxy.getUser(userId);
    }

    public long postUser(User userInfo) throws TException {
        return clientProxy.postUser(userInfo);
    }
}
