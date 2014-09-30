package coffee.letsgo.gateway.exception;

/**
 * Created by xbwu on 9/29/14.
 */
public class UnsupportedRequestException extends GatewayException {
    public UnsupportedRequestException(String msg) {
        super(msg);
    }

    public UnsupportedRequestException(String msg, Throwable inner) {
        super(msg, inner);
    }
}
