package coffee.letsgo.hangout.store.model;

import java.util.Date;
import java.util.Set;

/**
 * Created by xbwu on 10/18/14.
 */
public class HangoutData {

    public long hangoutId;

    public long creatorId;

    public String activity;

    public String subject;

    public String location;

    public Date startTime;

    public Date endTime;

    public Date creatTime;

    public Date updateTime;

    public Set<Long> participators;
}
