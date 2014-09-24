package coffee.letsgo.columbus.client.swift;

import coffee.letsgo.columbus.client.ClumbusClient;
import coffee.letsgo.columbus.client.loadbalancer.LoadBalancer;
import coffee.letsgo.columbus.client.loadbalancer.RoundrobinBalancer;
import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.base.Function;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Created by xbwu on 9/23/14.
 */
public class SwiftClient<C> {
    private final String serviceName;

    public SwiftClient(String serviceName) {
        this.serviceName = serviceName;
    }

    public ListenableFuture<C> createClient(final Class<C> cliClazz) {
        return createClient(cliClazz, RoundrobinBalancer.class);
    }

    public ListenableFuture<C> createClient(final Class<C> cliClazz,
                                            final Class<? extends LoadBalancer> lbClazz) {
        return new ClumbusClient(serviceName).createClient(cliClazz,
                lbClazz,
                new Function<String, ListenableFuture>() {
                    @Override
                    public ListenableFuture apply(String uri) {
                        return new ThriftClientManager().createClient(
                                new FramedClientConnector(HostAndPort.fromString(uri)),
                                cliClazz);
                    }
                });
    }
}
