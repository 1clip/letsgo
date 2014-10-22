package coffee.letsgo.hangout.store.model;

import coffee.letsgo.hangout.store.Constants;
import com.datastax.driver.mapping.EnumType;
import com.datastax.driver.mapping.annotations.*;

import java.util.Date;

/**
 * Created by xbwu on 10/18/14.
 */
@Table(keyspace = Constants.keyspace, name = Constants.hangoutFolkTableName)
public class HangoutFolkData {

    @PartitionKey
    @Column(name = "user_id")
    private long userId;

    @ClusteringColumn
    @Column(name = "hangout_id")
    private long hangoutId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role")
    private HangoutParticipatorRoleData role;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    private HangoutParticipatorStateData state;

    @Column(name = "comment")
    private String comment;

    @Column(name = "update_time")
    private Date updateTime;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getHangoutId() {
        return hangoutId;
    }

    public void setHangoutId(long hangoutId) {
        this.hangoutId = hangoutId;
    }

    public HangoutParticipatorRoleData getRole() {
        return role;
    }

    public void setRole(HangoutParticipatorRoleData role) {
        this.role = role;
    }

    public HangoutParticipatorStateData getState() {
        return state;
    }

    public void setState(HangoutParticipatorStateData state) {
        this.state = state;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HangoutFolkData)) return false;

        HangoutFolkData that = (HangoutFolkData) o;

        if (hangoutId != that.hangoutId) return false;
        if (userId != that.userId) return false;
        if (!comment.equals(that.comment)) return false;
        if (role != that.role) return false;
        if (state != that.state) return false;
        if (!updateTime.equals(that.updateTime)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (int) (hangoutId ^ (hangoutId >>> 32));
        result = 31 * result + role.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + comment.hashCode();
        result = 31 * result + updateTime.hashCode();
        return result;
    }
}
