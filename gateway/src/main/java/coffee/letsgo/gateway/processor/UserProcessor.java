package coffee.letsgo.gateway.processor;

import coffee.letsgo.gateway.exception.GatewayException;
import coffee.letsgo.gateway.model.AbstractResponse;
import coffee.letsgo.identity.*;
import coffee.letsgo.identity.client.IdentityClient;
import com.google.gson.Gson;
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
    private static final IdentityService identityClient = IdentityClient.getInstance();

    public UserProcessor(Gson gson) {
        super(gson);
    }

    @Override
    public String process(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        NewUser nu = new NewUser();
        nu.setLoginName("login_name");
        nu.setFriendlyName("friendly_name");
        nu.setGender(Gender.MALE);
        nu.setCellPhone("415.238.7173");
        nu.setDateOfBirth("1999-01-01");
        nu.setSignUpType(SignupType.CELL_PHONE);
        nu.setSignUpToken("token");
        System.out.println(gson.toJson(nu));
        if (req.getMethod() == HttpMethod.GET) {
            Matcher matcher = userIdPattern.matcher(req.getUri());
            if (matcher.matches()) {
                long id = Long.parseLong(matcher.group(2));
                return gson.toJson(getUser(id));
            }
            throw new GatewayException("failed to process");
        } else if (req.getMethod() == HttpMethod.POST) {
            NewUser newUser = decodeRequestBody(req, NewUser.class);
            return gson.toJson(createUser(newUser));
        } else {
            throw new GatewayException("failed to process");
        }
    }

    private UserInfo getUser(long userId) throws TException {
        return identityClient.getUser(userId);
    }

    private UserInfo createUser(NewUser newUser) throws TException {
        return identityClient.createUser(newUser);
    }
}
