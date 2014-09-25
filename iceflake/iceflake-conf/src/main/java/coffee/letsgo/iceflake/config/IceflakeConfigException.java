package coffee.letsgo.iceflake.config;

/**
 * Created by xbwu on 9/25/14.
 */
public class IceflakeConfigException extends Exception {
    public IceflakeConfigException() {
        super();
    }

    public IceflakeConfigException(String msg) {
        super(msg);
    }

    public IceflakeConfigException(Throwable inner) {
        super(inner);
    }

    public IceflakeConfigException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public IceflakeConfigException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
