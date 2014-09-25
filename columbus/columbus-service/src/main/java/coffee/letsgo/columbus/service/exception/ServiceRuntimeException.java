package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/15/14.
 */
public class ServiceRuntimeException extends RuntimeException {
    public ServiceRuntimeException() {
        super();
    }

    public ServiceRuntimeException(String msg) {
        super(msg);
    }

    public ServiceRuntimeException(Throwable inner) {
        super(inner);
    }

    public ServiceRuntimeException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ServiceRuntimeException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
