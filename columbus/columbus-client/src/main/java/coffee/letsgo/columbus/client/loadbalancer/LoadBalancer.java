package coffee.letsgo.columbus.client.loadbalancer;

import coffee.letsgo.columbus.client.exception.LoadBalancerException;
import coffee.letsgo.columbus.service.AvailabilitySet;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/7/14.
 */
public abstract class LoadBalancer {
    private final AvailabilitySet availabilitySet;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public LoadBalancer(AvailabilitySet availabilitySet) {
        this.availabilitySet = availabilitySet;
        this.availabilitySet.addActiveChangedListener(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    updateActiveMembers();
                } catch (Exception ex) {
                    logger.error("failed to update load balancer with new availability", ex);
                }
                return null;
            }
        });
        updateActiveMembers();
    }

    protected AtomicReference<ImmutableList<String>> activeList =
            new AtomicReference<ImmutableList<String>>();

    private void updateActiveMembers() {
        Set<String> actives = availabilitySet.getActives();
        activeList.set(FluentIterable.from(actives).toSortedList(String.CASE_INSENSITIVE_ORDER));
    }

    public String next() throws LoadBalancerException {
        try {
            ImmutableList<String> activeSnapshot = activeList.get();
            verifyNotNull(activeSnapshot, "load balancer not initialized");
            verify(!activeSnapshot.isEmpty(), "no active node");
            return nextUri(activeSnapshot);
        } catch (Exception ex) {
            logger.error("failed to get next available tunnel", ex);
            throw new LoadBalancerException("failed to get next available tunnel", ex);
        }
    }

    protected abstract String nextUri(ImmutableList<String> candidates);
}
