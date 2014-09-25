package coffee.letsgo.iceflake.server;

import coffee.letsgo.columbus.server.swift.SwiftServer;
import coffee.letsgo.iceflake.config.IceflakeConfigException;
import coffee.letsgo.iceflake.config.IceflakeConfigManager;
import coffee.letsgo.iceflake.config.IceflakeWorkerConfig;
import com.facebook.swift.service.guice.ThriftServiceExporter;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    public void start() throws UnknownHostException, IceflakeConfigException {
        server = new SwiftServer(IceflakeConfigManager.getInstance().getServiceName(), serverPort, new Module() {
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

    public static void main(String[] args) throws UnknownHostException, IceflakeConfigException {
        IceflakeWorkerConfig config = IceflakeConfigManager.getInstance()
                .getWorkerConfig(InetAddress.getLocalHost().getCanonicalHostName());
        new IceflakeServer(config.getId(), config.getPort()).start();
    }
}
