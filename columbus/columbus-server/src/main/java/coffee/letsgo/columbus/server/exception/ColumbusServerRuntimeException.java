package coffee.letsgo.columbus.server.exception;

/**
 * Created by xbwu on 9/21/14.
 */
public class ColumbusServerRuntimeException extends RuntimeException {
    public ColumbusServerRuntimeException() {
        super();
    }

    public ColumbusServerRuntimeException(String msg) {
        super(msg);
    }

    public ColumbusServerRuntimeException(Throwable inner) {
        super(inner);
    }

    public ColumbusServerRuntimeException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ColumbusServerRuntimeException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
