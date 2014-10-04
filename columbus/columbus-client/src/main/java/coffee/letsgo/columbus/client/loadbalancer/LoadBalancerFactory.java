package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;

/**
 * Created by xbwu on 9/21/14.
 */
public class LoadBalancerFactory {
    public LoadBalancer getLoadbalancer(final Class<? extends LoadBalancer> lbClazz,
                                        final AvailabilitySet availabilitySet) {
        return Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(LoadBalancer.class)
                        .to(lbClazz);
                binder.bind(AvailabilitySet.class)
                        .toInstance(availabilitySet);
            }
        }).getInstance(LoadBalancer.class);
    }
}
