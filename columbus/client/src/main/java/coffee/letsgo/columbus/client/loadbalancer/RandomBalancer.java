package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;

import java.util.Random;

/**
 * Created by xbwu on 9/7/14.
 */
public class RandomBalancer<C> extends LoadBalancer<C> {
    private final Random random = new Random(System.currentTimeMillis());

    @Inject
    public RandomBalancer(AvailabilitySet availabilitySet,
                          Function<String, ListenableFuture> tunnelCreator) {
        super(availabilitySet, tunnelCreator);
    }

    @Override
    protected String nextUri() {
        return activeList.get(random.nextInt(activeList.size()));
    }
}
