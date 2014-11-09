package coffee.letsgo.identity.server;

import coffee.letsgo.common.Common;
import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.client.IceflakeClient;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.User;
import coffee.letsgo.identity.server.exception.BadRequestException;
import coffee.letsgo.identity.server.exception.IdentityInternalException;
import coffee.letsgo.identity.store.IdentityStore;
import coffee.letsgo.identity.store.IdentityStoreCassandraImpl;
import coffee.letsgo.identity.store.model.Gender;
import coffee.letsgo.identity.store.model.SignupType;
import coffee.letsgo.identity.store.model.UserData;
import com.facebook.swift.codec.ThriftField;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by yfang on 9/25/14.
 */
@Singleton
public class IdentityServiceImpl implements IdentityService {
    private static final Logger logger = LoggerFactory.getLogger(IdentityServiceImpl.class);

    private static class IdentityStoreHolder {
        private static final IdentityStore instance = new IdentityStoreCassandraImpl();
    }

    private static class IceflakeClientHolder {
        private static Iceflake Instance = IceflakeClient.getInstance();
    }


    @Inject
    public IdentityServiceImpl() {
    }

    @Override
    public User createUser(User newUser) throws TException {
        User user = new User();
        try {
            BeanUtils.copyProperties(user, newUser);
        } catch (Exception ex) {
            throw new TException("failed to clone user");
        }
        long id = IceflakeClientHolder.Instance.getId(IdType.ACCT_ID);
        user.setId(id);

        UserData userData = toUserData(user);
        try {
            IdentityStoreHolder.instance.createUser(userData).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User getUser(long userId) throws TException {
        UserData userData;
        try {
            userData = IdentityStoreHolder.instance.getUser(userId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting user data for id %d", userId);
            logger.error(msg, ex);
            throw new IdentityInternalException(msg, ex);
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting user data for id %d", userId);
            logger.error(msg, ex);
            throw new IdentityInternalException(msg, ex);
        }

        return toUser(userData);
    }

    @Override
    public Map<Long, User> getUsers(
            @ThriftField(value = 1, name = "ids", requiredness = ThriftField.Requiredness.NONE) Set<Long> ids) throws TException {
        Map<Long, User> userDict = new HashMap<>();
        for (long id : ids) {
            userDict.put(id, getUser(id));
        }
        return userDict;
    }

    private UserData toUserData(User user) {
        if (user == null) {
            return null;
        }

        try {
            UserData userData = new UserData();
            userData.setUserId(user.getId());
            userData.setAvatarId(user.getAvatarInfo() == null ? null : user.getAvatarInfo().getAvatarId());
            userData.setCellPhone(user.getCellPhone());
            if (user.getDateOfBirth() != null) {
                userData.setDob(Common.simpleDateFormat.parse(user.getDateOfBirth()));
            }
            userData.setFriendlyName(user.getFriendlyName());
            userData.setLoginName(user.getLoginName());
            userData.setLocale(user.getLocale());
            userData.setSignupToken(user.getSignUpToken());
            userData.setSignupType(user.getSignUpType() == null ? SignupType.UNKNOW :
                    Enum.valueOf(SignupType.class, user.getSignUpType().toUpperCase()));
            userData.setCreateTime(new Date());
            userData.setUpdateTime(new Date());
            userData.setGender(user.getGender() == null ? Gender.UNKNOW :
                    Enum.valueOf(Gender.class, user.getGender().toUpperCase()));

            return userData;
        } catch (Exception ex) {
            throw new BadRequestException("Failed to convert to user data", ex);
        }
    }

    private User toUser(UserData userData) {
        if (userData == null) {
            return null;
        }
        User user = new User();
        user.setId(userData.getUserId());
        user.setLocale(userData.getLocale());
        user.setLoginName(userData.getLoginName());
        user.setCellPhone(userData.getCellPhone());
        user.setDateOfBirth(Common.simpleDateFormat.format(userData.getDob()));
        user.setFriendlyName(userData.getFriendlyName());
        user.setGender(userData.getGender().name());
        user.setSignUpType(userData.getSignupType().name());
        user.setSignUpToken(userData.getSignupToken());
        return user;
    }
}