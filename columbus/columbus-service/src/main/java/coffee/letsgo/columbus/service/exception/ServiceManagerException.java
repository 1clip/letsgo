package coffee.letsgo.columbus.service.exception;

import java.util.Objects;

/**
 * Created by xbwu on 9/14/14.
 */
public class ServiceManagerException extends ServiceException {
    public ServiceManagerException(){
        super();
    }

    public ServiceManagerException(String msg) {
        super(msg);
    }

    public ServiceManagerException(Throwable inner) {
        super(inner);
    }

    public ServiceManagerException(String msg, Throwable inner) {
        super(msg, inner);
    }

    public ServiceManagerException(String mask, Object... args) {
        super(mask, args);
    }
}
