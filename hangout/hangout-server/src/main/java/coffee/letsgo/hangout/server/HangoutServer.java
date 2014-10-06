package coffee.letsgo.hangout.server;

import coffee.letsgo.columbus.server.swift.SwiftServer;
import com.facebook.swift.service.guice.ThriftServiceExporter;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

/**
 * Created by yfang on 10/4/14.
 */
@Singleton
public class HangoutServer {
    public static final String serviceName = "hangout";
    public static final int defaultServerPort = 7167;
    private final int serverPort;
    private SwiftServer server;

    public HangoutServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        server = new SwiftServer(serviceName, serverPort, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(HangoutServiceImpl.class).in(Scopes.SINGLETON);
                ThriftServiceExporter.thriftServerBinder(binder).exportThriftService(HangoutServiceImpl.class);
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
        new HangoutServer(defaultServerPort).start();
    }
}
