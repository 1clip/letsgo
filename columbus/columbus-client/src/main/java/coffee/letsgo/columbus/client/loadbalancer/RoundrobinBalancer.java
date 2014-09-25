package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;

/**
 * Created by xbwu on 9/23/14.
 */
public class RoundrobinBalancer<C> extends LoadBalancer<C> {
    private int idx = 0;

    @Inject
    public RoundrobinBalancer(AvailabilitySet availabilitySet,
                              Function<String, ListenableFuture> tunnelCreator) {
        super(availabilitySet, tunnelCreator);
    }

    @Override
    protected String nextUri() {
        int sz = activeList.size();
        synchronized (this) {
            idx = ++idx % sz;
            return activeList.get(idx);
        }
    }
}
