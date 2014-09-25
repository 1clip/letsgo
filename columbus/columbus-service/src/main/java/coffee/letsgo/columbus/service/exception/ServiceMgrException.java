package coffee.letsgo.columbus.service.exception;

import java.util.Objects;

/**
 * Created by xbwu on 9/14/14.
 */
public class ServiceMgrException extends ServiceException {
    public ServiceMgrException(){
        super();
    }

    public ServiceMgrException(String msg) {
        super(msg);
    }

    public ServiceMgrException(Throwable inner) {
        super(inner);
    }

    public ServiceMgrException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ServiceMgrException(String mask, Object... args) {
        super(mask, args);
    }
}
