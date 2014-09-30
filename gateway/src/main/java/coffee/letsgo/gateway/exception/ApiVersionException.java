package coffee.letsgo.gateway.exception;

/**
 * Created by xbwu on 9/29/14.
 */
public class ApiVersionException extends GatewayException {
    public ApiVersionException(String msg) {
        super(msg);
    }

    public ApiVersionException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
