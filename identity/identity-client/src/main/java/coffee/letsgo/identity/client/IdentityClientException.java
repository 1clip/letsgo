package coffee.letsgo.identity.client;

/**
 * Created by xbwu on 9/21/14.
 */
public class IdentityClientException extends RuntimeException {
    public IdentityClientException() {
        super();
    }

    public IdentityClientException(String msg) {
        super(msg);
    }

    public IdentityClientException(Throwable inner) {
        super(inner);
    }

    public IdentityClientException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public IdentityClientException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
