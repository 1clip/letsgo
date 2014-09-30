package coffee.letsgo.gateway.processor;

import coffee.letsgo.gateway.model.AbstractRequest;
import coffee.letsgo.gateway.model.AbstractResponse;
import coffee.letsgo.gateway.util.Constants;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/29/14.
 */
public abstract class RequestProcessor {
    private final Gson gson;

    public RequestProcessor(Gson gson) {
        this.gson = verifyNotNull(gson);
    }

    public abstract AbstractResponse process(ChannelHandlerContext ctx, HttpRequest req);

    protected <T extends AbstractRequest> T decodeRequestBody(HttpRequest req, Class<T> clazz) {
        HttpContent content = (HttpContent) req;
        return gson.fromJson(content.content().toString(Constants.defaultCharset), clazz);
    }

    protected List<String> getParameter(HttpRequest req, String param) {
        return new QueryStringDecoder(req.getUri(), Constants.defaultCharset).parameters().get(param);
    }
}
