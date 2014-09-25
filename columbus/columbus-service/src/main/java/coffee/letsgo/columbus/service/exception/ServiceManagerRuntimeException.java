package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/15/14.
 */
public class ServiceManagerRuntimeException extends ServiceRuntimeException {
    public ServiceManagerRuntimeException() {
        super();
    }

    public ServiceManagerRuntimeException(String msg) {
        super(msg);
    }

    public ServiceManagerRuntimeException(Throwable inner) {
        super(inner);
    }

    public ServiceManagerRuntimeException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ServiceManagerRuntimeException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
