package coffee.letsgo.avatar.server;

import coffee.letsgo.columbus.server.swift.SwiftServer;
import com.facebook.swift.service.guice.ThriftServiceExporter;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

/**
 * Created by xbwu on 11/11/14.
 */
public class AvatarServer {
    private int serverPort;
    private SwiftServer server;

    public AvatarServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        server = new SwiftServer("avatar", serverPort, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(AvatarImpl.class).in(Scopes.SINGLETON);
                ThriftServiceExporter.thriftServerBinder(binder).exportThriftService(AvatarImpl.class);
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
        new AvatarServer(7788).start();
    }
}
