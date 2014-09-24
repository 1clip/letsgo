package coffee.letsgo.columbus.service.manager;

import coffee.letsgo.columbus.service.model.ServiceEvent;
import com.google.common.base.Function;

import java.util.Set;

/**
 * Created by xbwu on 9/6/14.
 */
public interface ServiceManager {
    boolean addNode(String uri);

    boolean removeNode(String uri);

    boolean isMember(String uri);

    boolean activateNode(String uri);

    boolean deactivateNode(String uri);

    boolean isActive(String uri);

    Set<String> getAllNodes();

    Set<String> getActives();

    void start();

    void stop();

    void addEventHandler(Function<ServiceEvent, Void> handler);
}
