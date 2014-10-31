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
    private long user_id;

    @Column(name = "avatar_id")
    private String avatarId;

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
}
