package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Created by xbwu on 9/23/14.
 */
public class RoundrobinBalancer extends LoadBalancer {
    private int idx = 0;

    @Inject
    public RoundrobinBalancer(AvailabilitySet availabilitySet) {
        super(availabilitySet);
    }

    @Override
    protected String nextUri(ImmutableList<String> candidates) {
        int sz = candidates.size();
        synchronized (this) {
            idx = ++idx % sz;
            return candidates.get(idx);
        }
    }
}
