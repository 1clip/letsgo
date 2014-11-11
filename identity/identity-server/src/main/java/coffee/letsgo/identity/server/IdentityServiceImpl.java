package coffee.letsgo.identity.server;

import coffee.letsgo.common.Common;
import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.client.IceflakeClient;
import coffee.letsgo.identity.*;
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
    public User createUser(User newUser)
            throws InvalidUserDataException, UserProcessException, TException {
        User user = new User();
        try {
            BeanUtils.copyProperties(user, newUser);
        } catch (Exception ex) {
            logger.error("failed to clone user", ex);
            UserProcessException userProcessException = new UserProcessException();
            userProcessException.setMsg(String.format("not able to clone user, detailed exception %s", ex));
            throw userProcessException;
        }
        long id = IceflakeClientHolder.Instance.getId(IdType.ACCT_ID);
        user.setId(id);

        UserData userData = toUserData(user);
        try {
            IdentityStoreHolder.instance.createUser(userData).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted creating user, %s", ex);
            logger.error(msg);
            UserProcessException userProcessException = new UserProcessException();
            userProcessException.setMsg(msg);
            throw userProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure creating user, %s", ex);
            logger.error(msg);
            UserProcessException userProcessException = new UserProcessException();
            userProcessException.setMsg(msg);
            throw userProcessException;
        }
        return user;
    }

    @Override
    public User getUser(long userId)
            throws UserNotFoundException, UserProcessException, TException {

        UserData userData;
        try {
            userData = IdentityStoreHolder.instance.getUser(userId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting user data for id %d", userId);
            logger.error(msg, ex);
            UserProcessException userProcessException = new UserProcessException();
            userProcessException.setMsg(msg);
            throw userProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting user data for id %d", userId);
            logger.error(msg, ex);
            UserProcessException userProcessException = new UserProcessException();
            userProcessException.setMsg(msg);
            throw userProcessException;
        }

        return toUser(userData);
    }

    @Override
    public Map<Long, User> getUsers(Set<Long> ids)
            throws UserNotFoundException, UserProcessException, TException {

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
            userData.setAvatarId(user.getAvatarInfo() == null ? -1L : user.getAvatarInfo().getAvatarId());
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
            String msg = String.format("failed to convert user data, %s", ex);
            logger.error(msg);
            InvalidUserDataException invalidUserDataException = new InvalidUserDataException();
            invalidUserDataException.setMsg(msg);
            throw invalidUserDataException;
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