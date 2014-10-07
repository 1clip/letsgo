package coffee.letsgo.identity.server;

import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.client.IceflakeClient;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.thrift.TException;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yfang on 9/25/14.
 */
@Singleton
public class IdentityServiceImpl implements IdentityService {
    private final ConcurrentHashMap<Long, User> usersDB = new ConcurrentHashMap<>();

    private static class IceflakeClientHolder {
        private static Iceflake Instance = IceflakeClient.getInstance();
    }

    @Inject
    public IdentityServiceImpl() { }

    @Override
    public User createUser(User newUser) throws TException {
        User user = new User();
        try {
            BeanUtils.copyProperties(user, newUser);
        } catch (Exception ex) {
           throw new TException("failed to clone user");
        }
        long id = IceflakeClientHolder.Instance.getId(IdType.ACCT_ID);
        user.setUserId(id);
        usersDB.put(id, user);
        return usersDB.get(id);
    }

    @Override
    public User getUser(long userId) throws TException {
        return usersDB.get(userId);
    }
}