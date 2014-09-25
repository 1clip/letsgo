package coffee.letsgo.columbus.service.model;

import java.util.HashMap;

/**
 * Created by xbwu on 9/7/14.
 */
public enum NodeStatus {
    ACTIVE(0),
    INACTIVE(1),
    UNKNOWN(-1);

    private int statusCode;

    NodeStatus(int code) {
        this.statusCode = code;
    }

    int getStatusCode() {
        return statusCode;
    }

    private static HashMap<Integer, NodeStatus> hm;

    static {
        hm = new HashMap<Integer, NodeStatus>();
        for (NodeStatus st : NodeStatus.values()) {
            hm.put(st.getStatusCode(), st);
        }
    }

    public static NodeStatus of(int statusCode) {
        return hm.containsKey(statusCode) ? hm.get(statusCode) : NodeStatus.UNKNOWN;
    }
}
