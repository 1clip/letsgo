package coffee.letsgo.gateway.processor;

import coffee.letsgo.gateway.exception.GatewayException;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.User;
import coffee.letsgo.identity.client.IdentityClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.thrift.TException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xbwu on 9/30/14.
 */
public class UserProcessor extends RequestProcessor {
    private final Pattern userIdPattern = Pattern.compile("^/users/(\\d+)$");
    private final IdentityService identityClient = IdentityClient.getInstance();

    @Override
    public String process(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        if (req.getMethod() == HttpMethod.GET) {
            Matcher matcher = userIdPattern.matcher(req.getUri());
            if (matcher.matches()) {
                long id = Long.parseLong(matcher.group(1));
                return gson.toJson(getUser(id));
            }
            throw new GatewayException("failed to process");
        } else if (req.getMethod() == HttpMethod.POST) {
            User newUser = decodeRequestBody(req, User.class);
            return gson.toJson(createUser(newUser));
        } else {
            throw new GatewayException("failed to process");
        }
    }

    private User getUser(long userId) throws TException {
        return identityClient.getUser(userId);
    }

    private User createUser(User newUser) throws TException {

        return identityClient.createUser(newUser);
    }
}
