package coffee.letsgo.identity.server;

import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.User;
import coffee.letsgo.iceflake.client.IceflakeClient;
import org.apache.thrift.TException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yfang on 9/25/14.
 */
public class IdentityServiceImpl implements IdentityService {
    private ConcurrentHashMap<Long, User> usersDB = new ConcurrentHashMap<>();

    private Iceflake iceflakeClient = IceflakeClient.getInstance();

    @Override
    public long createUser(User user) throws TException {
        long id = iceflakeClient.getId(IdType.ACCT_ID);
        usersDB.put(id, user);
        return id;
    }

    @Override
    public User getUser(long userId) throws TException {
        return usersDB.get(userId);
    }
}