package coffee.letsgo.columbus.server.swift;

import coffee.letsgo.columbus.server.ColumbusServer;
import com.facebook.swift.codec.guice.ThriftCodecModule;
import com.facebook.swift.service.ThriftServer;
import com.facebook.swift.service.guice.ThriftServerModule;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Stage;
import io.airlift.configuration.ConfigurationFactory;
import io.airlift.configuration.ConfigurationModule;

/**
 * Created by xbwu on 9/21/14.
 */
public class SwiftServer extends ColumbusServer {
    private ThriftServer server;
    private final Module serviceModule;

    public SwiftServer(String serviceName, int port, Module serviceModule) {
        this.serviceName = serviceName;
        this.port = port;
        this.serviceModule = serviceModule;
    }

    @Override
    protected void run() {
        server = Guice.createInjector(
                Stage.PRODUCTION,
                new ConfigurationModule(new ConfigurationFactory(
                        ImmutableMap.of(
                                "thrift.port", String.valueOf(port)
                        ))),
                new ThriftCodecModule(),
                new ThriftServerModule(),
                serviceModule).getInstance(ThriftServer.class);
        server.start();
    }

    @Override
    protected void stop() {
        if(server != null) {
            server.close();
            server = null;
        }
    }
}
