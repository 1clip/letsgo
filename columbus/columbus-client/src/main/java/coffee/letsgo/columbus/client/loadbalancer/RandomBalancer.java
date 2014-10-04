package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.Random;

/**
 * Created by xbwu on 9/7/14.
 */
public class RandomBalancer extends LoadBalancer {
    private final Random random = new Random(System.currentTimeMillis());

    @Inject
    public RandomBalancer(AvailabilitySet availabilitySet) {
        super(availabilitySet);
    }

    @Override
    protected String nextUri(ImmutableList<String> candidates) {
        return candidates.get(random.nextInt(candidates.size()));
    }
}
