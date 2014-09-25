package coffee.letsgo.columbus.service.model;

/**
 * Created by xbwu on 9/16/14.
 */
public enum ServiceEvent {
    CONNECTED,
    DISCONNECTED,
    EXPIRED,
    MEMBER_CHANGED,
    ACTIVE_CHANGED,

    UPDATING_ALL,
    UPDATED_ALL,
    UPDATING,
    UPDATED,
    UPDATE_FAILED
}
