package coffee.letsgo.gateway.util;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by xbwu on 9/29/14.
 */
public class RequestMatcher {
    private final Set<HttpMethod> methods;
    private final List<Pattern> patterns = new ArrayList<Pattern>(2);

    public RequestMatcher(String prefix) {
        this(prefix, new HashSet<HttpMethod>());
    }

    public RequestMatcher(String prefix, Set<HttpMethod> methods) {
        prefix = verifyNotNull(prefix, "prefix").trim().toLowerCase();
        if (prefix.endsWith("/")) {
            patterns.add(Pattern.compile("^" + prefix));
        } else {
            patterns.add(Pattern.compile("^" + prefix + "$"));
            patterns.add(Pattern.compile("^" + prefix + "/"));
        }
        this.methods = methods;
    }

    public boolean match(HttpRequest request) {
        if (!methods.isEmpty() && !methods.contains(request.getMethod())) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(request.getUri()).matches()) {
                return true;
            }
        }
        return false;
    }
}
