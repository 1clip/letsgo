package coffee.letsgo.gateway.util;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/29/14.
 */
public class RequestMatcher {
    private final String prefix;
    private final Set<HttpMethod> methods;

    public RequestMatcher(String prefix) {
        this(prefix, new HashSet<HttpMethod>());
    }

    public RequestMatcher(String prefix, Set<HttpMethod> methods) {
        this.prefix = verifyNotNull(prefix, "prefix").trim().toLowerCase();
        this.methods = methods;
    }

    public boolean match(HttpRequest request) {
        return request.getUri().startsWith(prefix) &&
                (methods.isEmpty() || methods.contains(request.getMethod()));
    }
}
