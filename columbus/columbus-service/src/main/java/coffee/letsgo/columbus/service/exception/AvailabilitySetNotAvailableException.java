package coffee.letsgo.columbus.service.exception;

/**
 * Created by xbwu on 9/21/14.
 */
public class AvailabilitySetNotAvailableException extends ServiceManagerRuntimeException {
    public AvailabilitySetNotAvailableException() {
        super();
    }

    public AvailabilitySetNotAvailableException(String msg) {
        super(msg);
    }

    public AvailabilitySetNotAvailableException(Throwable inner) {
        super(inner);
    }

    public AvailabilitySetNotAvailableException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public AvailabilitySetNotAvailableException(String mask, Object... args) {
        this(String.format(mask, args));
    }
}
