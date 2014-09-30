package coffee.letsgo.gateway.model;

import java.util.Date;

/**
 * Created by xbwu on 9/29/14.
 */
public class GetUserResponse extends AbstractResponse {
    public long id;
    public String login;
    public String friendlyName;
    public Gender gender;
    public Date birth;
    public AvatarInfo avatar;
    public String cellPhone;
}
