package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.*;

/**
 * Created by xbwu on 9/21/14.
 */
public class LoadBalancerFactory<C> {
    public LoadBalancer<C> getLoadbalancer(final Class<? extends LoadBalancer> lbClazz,
                                           final AvailabilitySet availabilitySet,
                                           final Function<String, ListenableFuture> tunnelCreator) {
        return new RoundrobinBalancer<C>(availabilitySet, tunnelCreator);
        /*
        return Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(LoadBalancer.class)
                        .to(lbClazz);
                binder.bind(AvailabilitySet.class)
                        .toInstance(availabilitySet);
                binder.bind(new TypeLiteral<Function<String, ListenableFuture>>() { })
                        .toInstance(tunnelCreator);
            }
        }).getInstance(LoadBalancer.class);
        */
    }

}
