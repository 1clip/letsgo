package coffee.letsgo.hangout.store.model;

import java.util.Date;

/**
 * Created by xbwu on 10/18/14.
 */
public class HangoutFolkData {

    public long userId;

    public long hangoutId;

    public HangoutParticipatorRoleData role;

    public HangoutParticipatorStateData state;

    public String comment;

    public Date updateTime;
}
