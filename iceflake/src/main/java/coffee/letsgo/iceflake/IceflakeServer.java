package coffee.letsgo.iceflake;

import com.facebook.swift.codec.guice.ThriftCodecModule;
import com.facebook.swift.service.ThriftServer;
import com.facebook.swift.service.guice.ThriftServerModule;
import com.facebook.swift.service.guice.ThriftServiceExporter;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import com.google.inject.name.Names;
import io.airlift.configuration.ConfigurationFactory;
import io.airlift.configuration.ConfigurationModule;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

/**
 * Created by xbwu on 8/29/14.
 */
public class IceflakeServer {

    private int
            datacenterId,
            workerId,
            serverPort;

    public IceflakeServer(int datacenterId,
                          int workerId,
                          int serverPort) {

        this.datacenterId = datacenterId;
        this.workerId = workerId;
        this.serverPort = serverPort;
    }

    public ThriftServer start() {
        return Guice.createInjector(
                Stage.PRODUCTION,
                new ConfigurationModule(new ConfigurationFactory(
                        ImmutableMap.of(
                                "thrift.port", String.valueOf(serverPort)
                        ))),
                new ThriftCodecModule(),
                new ThriftServerModule(),
                new Module() {
                    @Override
                    public void configure(Binder binder) {
                        binder.bindConstant().annotatedWith(Names.named("worker id")).to(workerId);
                        binder.bindConstant().annotatedWith(Names.named("datacenter id")).to(datacenterId);
                        binder.bind(IceflakeImpl.class).in(Scopes.SINGLETON);
                        ThriftServiceExporter.thriftServerBinder(binder).exportThriftService(IceflakeImpl.class);
                    }
                }).getInstance(ThriftServer.class).start();
    }

    public static void main(String[] args) {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(IceflakeConfig.class).to(DevConfig.class);
            }
        }).getInstance(IceflakeConfig.class).apply().start();
    }
}
