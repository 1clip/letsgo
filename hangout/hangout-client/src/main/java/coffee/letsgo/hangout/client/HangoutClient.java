package coffee.letsgo.hangout.client;

import coffee.letsgo.columbus.client.swift.SwiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import coffee.letsgo.hangout.HangoutService;

/**
 * Created by xbwu on 10/4/14.
 */
public class HangoutClient {

    private static class HangoutProxyHolder {
        private static final HangoutService Instance = buildServiceProxy();
    }

    private static final Logger logger = LoggerFactory.getLogger(HangoutProxyHolder.class);

    public static HangoutService getInstance() {
        if (HangoutProxyHolder.Instance == null) {
            throw new HangoutClientException("crap hangout client");
        }
        return HangoutProxyHolder.Instance;
    }

    private HangoutClient() {
    }

    private static HangoutService buildServiceProxy() {
        try {
            return new SwiftClient<HangoutService>("hangout")
                    .createClient(HangoutService.class).get();
        } catch (Exception ex) {
            logger.error("failed to create client proxy", ex);
            return null;
        }
    }
}
