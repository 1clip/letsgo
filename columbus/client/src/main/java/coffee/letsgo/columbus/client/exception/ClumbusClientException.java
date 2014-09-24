package coffee.letsgo.columbus.client.exception;

/**
 * Created by xbwu on 9/21/14.
 */
public class ClumbusClientException extends Exception{
    public ClumbusClientException() {
        super();
    }

    public ClumbusClientException(String msg) {
        super(msg);
    }

    public ClumbusClientException(Throwable inner) {
        super(inner);
    }

    public ClumbusClientException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ClumbusClientException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
