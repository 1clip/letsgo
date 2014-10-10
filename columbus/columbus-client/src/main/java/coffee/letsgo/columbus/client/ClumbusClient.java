package coffee.letsgo.columbus.client;

import coffee.letsgo.columbus.client.exception.ClumbusClientException;
import coffee.letsgo.columbus.client.loadbalancer.LoadBalancer;
import coffee.letsgo.columbus.client.loadbalancer.LoadBalancerFactory;
import coffee.letsgo.columbus.client.loadbalancer.RandomBalancer;
import coffee.letsgo.columbus.service.ServiceDeamon;
import coffee.letsgo.columbus.service.manager.ServiceManager;
import coffee.letsgo.columbus.service.manager.ServiceManagerZookeeperImpl;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/21/14.
 */
public class ClumbusClient {
    protected String serviceName;
    private final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    private final ServiceDeamon serviceDeamon;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    public ClumbusClient(final String serviceName) {
        this.serviceName = verifyNotNull(serviceName);
        serviceDeamon = createAndStartDeamon();
    }

    public <C> ListenableFuture<C> createClient(final Class<C> cliClazz,
                                                final Function<String, ListenableFuture<C>> tunnelCreator) {
        return createClient(cliClazz, tunnelCreator, 2);
    }

    public <C> ListenableFuture<C> createClient(final Class<C> cliClazz,
                                                final Function<String, ListenableFuture<C>> tunnelCreator,
                                                final int retries) {
        return createClient(cliClazz, RandomBalancer.class, tunnelCreator, retries);
    }

    public <C> ListenableFuture<C> createClient(final Class<C> cliClazz,
                                                final Class<? extends LoadBalancer> lbClazz,
                                                final Function<String, ListenableFuture<C>> tunnelCreator) {
        return createClient(cliClazz, lbClazz, tunnelCreator, 1);
    }

    public <C> ListenableFuture<C> createClient(final Class<C> cliClazz,
                                                final Class<? extends LoadBalancer> lbClazz,
                                                final Function<String, ListenableFuture<C>> tunnelCreator,
                                                final int retries) {
        return service.submit(new Callable<C>() {
            public C call() {
                serviceDeamon.awaitInitialized(5 * 60 * 1000);
                LoadBalancer lb = new LoadBalancerFactory()
                        .getLoadbalancer(lbClazz, serviceDeamon.getAvailabilitySet());
                return createClientProxy(lb, cliClazz, tunnelCreator, retries);
            }
        });
    }

    private ServiceDeamon createAndStartDeamon() {
        ServiceDeamon serviceDeamon = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ServiceManager.class).to(ServiceManagerZookeeperImpl.class);
                binder.bindConstant().annotatedWith(Names.named("service name")).to(serviceName);
            }
        }).getInstance(ServiceDeamon.class);
        serviceDeamon.start();
        return serviceDeamon;
    }

    private <C> C createClientProxy(final LoadBalancer lb,
                                    final Class<C> type,
                                    final Function<String, ListenableFuture<C>> tunnelCreator,
                                    final int retries) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),
                new Class[]{type},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        int attempts = 0;
                        while (++attempts <= retries) {
                            try {
                                String uri = lb.next();
                                return method.invoke(tunnelCreator.apply(uri).get(), args);
                            } catch (Exception ex) {
                                logger.error(String.format("client invocation failed %d/%d", attempts, retries), ex);
                            }
                        }
                        throw new ClumbusClientException("failed to process client invocation in all attempts");
                    }
                }));
    }
}
