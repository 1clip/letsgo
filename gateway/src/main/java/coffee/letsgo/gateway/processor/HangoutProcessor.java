package coffee.letsgo.gateway.processor;

import coffee.letsgo.gateway.exception.GatewayException;
import coffee.letsgo.hangout.Hangout;
import coffee.letsgo.hangout.HangoutService;
import coffee.letsgo.hangout.HangoutSummary;
import coffee.letsgo.hangout.client.HangoutClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.thrift.TException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xbwu on 10/4/14.
 */
public class HangoutProcessor extends RequestProcessor {
    private final Pattern hangoutIdPattern = Pattern.compile("^/hangouts/(\\d+)$");
    private final Pattern hangoutStatusFilterPattern = Pattern.compile("^/hangouts\\?state=([a-zA-Z]*)$");
    private final HangoutService hangoutClient = HangoutClient.getInstance();

    @Override
    public String process(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        long uid = Long.parseLong(req.headers().get("user-id"));
        if (req.getMethod() == HttpMethod.GET) {
            Matcher idMatcher = hangoutIdPattern.matcher(req.getUri());
            Matcher stMatcher = hangoutStatusFilterPattern.matcher(req.getUri());
            if (idMatcher.matches()) {
                long hid = Long.parseLong(idMatcher.group(1));
                return gson.toJson(getHangout(uid, hid));
            }
            if(stMatcher.matches()) {
                String state = stMatcher.group(1);
                return gson.toJson(getHangoutsByStatus(uid, state));
            }
            throw new GatewayException("failed to process");
        } else if (req.getMethod() == HttpMethod.POST) {
            Hangout hangout = decodeRequestBody(req, Hangout.class);
            return gson.toJson(createHangout(uid, hangout));
        } else if (req.getMethod() == HttpMethod.PATCH) {
            throw new GatewayException("unimplemented patch");
        } else {
            throw new GatewayException("failed to process");
        }
    }

    private Hangout getHangout(long uid, long hid) throws TException {
        return hangoutClient.getHangoutById(uid, hid);
    }

    private List<HangoutSummary> getHangoutsByStatus(long uid, String state) throws TException {
        return hangoutClient.getHangoutByStatus(uid, state);
    }

    private Hangout createHangout(long uid, Hangout hangout) throws TException {
        return hangoutClient.createHangout(uid, hangout);
    }
}
