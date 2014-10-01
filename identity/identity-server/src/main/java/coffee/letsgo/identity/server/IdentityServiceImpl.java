package coffee.letsgo.identity.server;

import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.client.IceflakeClient;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.NewUser;
import coffee.letsgo.identity.UserInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.thrift.TException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yfang on 9/25/14.
 */
public class IdentityServiceImpl implements IdentityService {
    private ConcurrentHashMap<Long, UserInfo> usersDB = new ConcurrentHashMap<>();

    private Iceflake iceflakeClient = IceflakeClient.getInstance();

    @Override
    public UserInfo createUser(NewUser newUser) throws TException {
        long id = iceflakeClient.getId(IdType.ACCT_ID);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(id);
        try {
            BeanUtils.copyProperties(userInfo, newUser);
        } catch (Exception ex) {
           throw new TException("failed to clone user");
        }
        usersDB.put(id, userInfo);
        return usersDB.get(id);
    }

    @Override
    public UserInfo getUser(long userId) throws TException {
        return usersDB.get(userId);
    }
}