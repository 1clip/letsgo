package coffee.letsgo.iceflake.client;

/**
 * Created by xbwu on 9/21/14.
 */
public class IceflakeClientException extends RuntimeException {
    public IceflakeClientException() {
        super();
    }

    public IceflakeClientException(String msg) {
        super(msg);
    }

    public IceflakeClientException(Throwable inner) {
        super(inner);
    }

    public IceflakeClientException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public IceflakeClientException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
