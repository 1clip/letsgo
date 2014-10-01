package coffee.letsgo.identity.client;

import coffee.letsgo.columbus.client.swift.SwiftClient;
import coffee.letsgo.identity.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yfang on 9/4/14.
 */
public class IdentityClient {
    private static class IdentityProxyHolder {
        private static final IdentityService Instance = buildIdentityProxy();
    }

    private static final Logger logger = LoggerFactory.getLogger(IdentityClient.class);

    public static IdentityService getInstance() {
        if(IdentityProxyHolder.Instance == null) {
            throw new IdentityClientException("crap identity client");
        }
        return IdentityProxyHolder.Instance;
    }

    private IdentityClient() { }

    private static IdentityService buildIdentityProxy() {
        try {
            return new SwiftClient<IdentityService>("identity")
                    .createClient(IdentityService.class).get();
        } catch (Exception ex) {
            logger.error("failed to create client proxy", ex);
            return null;
        }
    }
}
