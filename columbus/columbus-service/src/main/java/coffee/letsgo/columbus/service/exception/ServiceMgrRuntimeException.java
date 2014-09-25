package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/15/14.
 */
public class ServiceMgrRuntimeException extends ServiceRuntimeException {
    public ServiceMgrRuntimeException() {
        super();
    }

    public ServiceMgrRuntimeException(String msg) {
        super(msg);
    }

    public ServiceMgrRuntimeException(Throwable inner) {
        super(inner);
    }

    public ServiceMgrRuntimeException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ServiceMgrRuntimeException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
