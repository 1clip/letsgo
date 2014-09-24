package coffee.letsgo.iceflake.server;

import coffee.letsgo.columbus.server.swift.SwiftServer;
import coffee.letsgo.iceflake.config.IceflakeConfig;
import coffee.letsgo.iceflake.config.IceflakeConstants;
import com.facebook.swift.service.guice.ThriftServiceExporter;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Created by xbwu on 8/29/14.
 */
public class IceflakeServer {

    private int workerId, serverPort;
    private SwiftServer server;

    public IceflakeServer(int workerId,
                          int serverPort) {

        this.workerId = workerId;
        this.serverPort = serverPort;
    }

    public void start() {
        server = new SwiftServer(IceflakeConstants.serviceName, serverPort, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bindConstant().annotatedWith(Names.named("worker id")).to(workerId);
                binder.bind(IceflakeImpl.class).in(Scopes.SINGLETON);
                ThriftServiceExporter.thriftServerBinder(binder).exportThriftService(IceflakeImpl.class);
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

    protected static IceflakeServer apply(IceflakeConfig config) {
        return new IceflakeServer(
                config.getWorkerId(),
                config.getServerPort());
    }

    public static void main(String[] args) {
        apply(new IceflakeConfig()).start();
    }
}
