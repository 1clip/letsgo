package coffee.letsgo.gateway.processor;

import coffee.letsgo.gateway.model.AbstractResponse;
import coffee.letsgo.identity.client.IdentityClient;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by xbwu on 9/30/14.
 */
public class UserProcessor extends RequestProcessor {
    public UserProcessor(Gson gson) {
        super(gson);
    }

    @Override
    public AbstractResponse process(ChannelHandlerContext ctx, HttpRequest req) {
        IdentityClient client = IdentityClient.getInstance();
        return null;
    }
}
