package coffee.letsgo.gateway.processor;

import coffee.letsgo.gateway.exception.GatewayException;
import coffee.letsgo.hangout.Hangout;
import coffee.letsgo.hangout.HangoutService;
import coffee.letsgo.hangout.client.HangoutClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.thrift.TException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xbwu on 10/4/14.
 */
public class HangoutProcessor extends RequestProcessor {
    private final Pattern hangoutIdPattern = Pattern.compile("^/hangouts/(\\d+)$");
    private final HangoutService hangoutClient = HangoutClient.getInstance();

    @Override
    public String process(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        long uid = Long.parseLong(req.headers().get("user-id"));
        if (req.getMethod() == HttpMethod.GET) {
            Matcher matcher = hangoutIdPattern.matcher(req.getUri());
            if (matcher.matches()) {
                long hid = Long.parseLong(matcher.group(1));
                return gson.toJson(getHangout(uid, hid));
            }
            throw new GatewayException("failed to process");
        } else if (req.getMethod() == HttpMethod.POST) {
            Hangout hangout = decodeRequestBody(req, Hangout.class);
            return gson.toJson(createHangout(uid, hangout));
        } else {
            throw new GatewayException("failed to process");
        }
    }

    private Hangout getHangout(long uid, long hid) throws TException {
        return hangoutClient.getHangoutById(uid, hid);
    }

    private Hangout createHangout(long uid, Hangout hangout) throws TException {
        return hangoutClient.createHangout(uid, hangout);
    }
}
