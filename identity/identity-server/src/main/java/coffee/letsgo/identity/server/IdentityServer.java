package coffee.letsgo.identity.server;

import coffee.letsgo.columbus.server.swift.SwiftServer;
import coffee.letsgo.identity.IdentityService;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

/**
 * Created by xbwu on 10/1/14.
 */
public class IdentityServer {
    private static final String serviceName = "identity";
    private static final int defaultServerPort = 7166;
    private final int serverPort;
    private SwiftServer server;

    public IdentityServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        server = new SwiftServer(serviceName, serverPort, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(IdentityServiceImpl.class).in(Scopes.SINGLETON);
                binder.bind(IdentityService.class).to(IdentityServiceImpl.class);
            }
        });
        server.start();
    }

    public void shutdown() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }

    public static void main(String[] args) {
        new IdentityServer(defaultServerPort).start();
    }
}
