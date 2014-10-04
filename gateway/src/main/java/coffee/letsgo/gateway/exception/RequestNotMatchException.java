package coffee.letsgo.gateway.exception;

/**
 * Created by xbwu on 9/30/14.
 */
public class RequestNotMatchException extends BadRequestException {
    public RequestNotMatchException(String msg) {
        super(msg);
    }

    public RequestNotMatchException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
