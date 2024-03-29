package coffee.letsgo.gateway.util;

import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * Created by xbwu on 9/28/14.
 */
public class Constants {
    public static String apiVersionKeyName = "coffee.api.version";
    public static int gatewayPort = 8443;
    public static Charset defaultCharset = CharsetUtil.UTF_8;
    public static HttpVersion defaultHttpVersion = HttpVersion.HTTP_1_1;

}
