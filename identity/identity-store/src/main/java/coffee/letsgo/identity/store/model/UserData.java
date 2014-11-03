package coffee.letsgo.identity.store.model;

import coffee.letsgo.identity.store.Constants;
import com.datastax.driver.mapping.EnumType;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;

/**
 * Created by xbwu on 10/18/14.
 */
@Table(keyspace = Constants.keyspace, name = Constants.userTableName)
public class UserData {
    @PartitionKey
    @Column(name = "user_id")
    private long userId;

    @Column(name = "avatar_id")
    private long avatarId;

    @Column(name = "login_name")
    private String loginName;

    @Column(name = "friendly_name")
    private String friendlyName;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    private Date dob;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "sign_up_type")
    private SignupType signupType;

    @Column(name = "sign_up_token")
    private String signupToken;

    @Column(name = "locale")
    private String locale;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(long avatarId) {
        this.avatarId = avatarId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public SignupType getSignupType() {
        return signupType;
    }

    public void setSignupType(SignupType signupType) {
        this.signupType = signupType;
    }

    public String getSignupToken() {
        return signupToken;
    }

    public void setSignupToken(String signupToken) {
        this.signupToken = signupToken;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }


}
