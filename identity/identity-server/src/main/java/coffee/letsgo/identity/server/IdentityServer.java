package coffee.letsgo.identity.server;

import coffee.letsgo.columbus.server.swift.SwiftServer;
import com.facebook.swift.service.guice.ThriftServiceExporter;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

/**
 * Created by xbwu on 10/1/14.
 */
public class IdentityServer {
    public static final String serviceName = "identity";
    public static final int defaultServerPort = 7166;
    private SwiftServer server;

    public void start() {
        server = new SwiftServer(serviceName, defaultServerPort, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(IdentityServiceImpl.class).in(Scopes.SINGLETON);
                ThriftServiceExporter.thriftServerBinder(binder).exportThriftService(IdentityServiceImpl.class);
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
        new IdentityServer().start();
    }
}
