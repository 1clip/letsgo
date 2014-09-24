package coffee.letsgo.columbus.client.exception;

/**
 * Created by xbwu on 9/23/14.
 */
public class LoadBalancerException extends ClumbusClientException {
    public LoadBalancerException() {
        super();
    }

    public LoadBalancerException(String msg) {
        super(msg);
    }

    public LoadBalancerException(Throwable inner) {
        super(inner);
    }

    public LoadBalancerException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public LoadBalancerException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
