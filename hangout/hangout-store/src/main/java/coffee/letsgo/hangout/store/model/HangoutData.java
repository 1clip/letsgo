package coffee.letsgo.hangout.store.model;

import java.util.Date;
import java.util.Set;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

/**
 * Created by xbwu on 10/18/14.
 */
@Table(keyspace = "letsgo", name = "hangout")
public class HangoutData {
    @PartitionKey
    @Column(name = "hangout_id")
    private long hangoutId;

    @Column(name = "creator_id")
    private long creatorId;

    @Column(name = "activity")
    private String activity;

    @Column(name = "subject")
    private String subject;

    @Column(name = "location")
    private String location;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "create_time")
    private Date creatTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "participators")
    private Set<Long> participators;

    public Date getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    public long getHangoutId() {
        return hangoutId;
    }

    public void setHangoutId(long hangoutId) {
        this.hangoutId = hangoutId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Set<Long> getParticipators() {
        return participators;
    }

    public void setParticipators(Set<Long> participators) {
        this.participators = participators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HangoutData)) return false;

        HangoutData that = (HangoutData) o;

        if (creatorId != that.creatorId) return false;
        if (hangoutId != that.hangoutId) return false;
        if (!activity.equals(that.activity)) return false;
        if (!creatTime.equals(that.creatTime)) return false;
        if (!endTime.equals(that.endTime)) return false;
        if (!location.equals(that.location)) return false;
        if (!participators.equals(that.participators)) return false;
        if (!startTime.equals(that.startTime)) return false;
        if (!subject.equals(that.subject)) return false;
        if (!updateTime.equals(that.updateTime)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (hangoutId ^ (hangoutId >>> 32));
        result = 31 * result + (int) (creatorId ^ (creatorId >>> 32));
        result = 31 * result + activity.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + creatTime.hashCode();
        result = 31 * result + updateTime.hashCode();
        result = 31 * result + participators.hashCode();
        return result;
    }
}
