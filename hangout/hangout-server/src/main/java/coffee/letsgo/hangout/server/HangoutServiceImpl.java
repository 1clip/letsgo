package coffee.letsgo.hangout.server;

import coffee.letsgo.common.Common;
import coffee.letsgo.hangout.*;
import coffee.letsgo.hangout.exception.DataFormatException;
import coffee.letsgo.hangout.store.HangoutStore;
import coffee.letsgo.hangout.store.HangoutStoreCassandraImpl;
import coffee.letsgo.hangout.store.model.HangoutData;
import coffee.letsgo.hangout.store.model.HangoutFolkData;
import coffee.letsgo.hangout.store.model.HangoutParticipatorRoleData;
import coffee.letsgo.hangout.store.model.HangoutParticipatorStateData;
import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.client.IceflakeClient;
import coffee.letsgo.identity.IdentityService;
import coffee.letsgo.identity.User;
import coffee.letsgo.identity.client.IdentityClient;
import com.facebook.swift.codec.ThriftField;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yfang on 10/4/14.
 */
public class HangoutServiceImpl implements HangoutService {
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, UserHangoutInfo>> userHangOutsDB = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, HangoutInfo> hangOutsDB = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(HangoutServiceImpl.class);


    private static class HangoutStoreHolder {
        private static final HangoutStore instance = new HangoutStoreCassandraImpl();
    }

    private static class IceflakeClientHolder {
        private static Iceflake Instance = IceflakeClient.getInstance();
    }

    private static class IdentityClientHolder {
        private static IdentityService instance = IdentityClient.getInstance();
    }

    @Override
    public Hangout createHangout(
            @ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId,
            @ThriftField(value = 2, name = "hangOut", requiredness = ThriftField.Requiredness.NONE) Hangout hangOut)
            throws TException {

        verifyUser(userId);
        verifyParticipators(hangOut.getParticipators());
        logger.debug("creating hangout for user %d", userId);
        long hangOutId = IceflakeClientHolder.Instance.getId(IdType.HANGOUT_ID);
        logger.debug("new hangout id: %s", hangOutId);
        hangOut.setId(hangOutId);
        HangoutData hangoutData = toHangoutData(hangOut, userId);
        Date now = new Date();
        hangoutData.setCreatTime(now);
        hangoutData.setUpdateTime(now);
        Set<HangoutFolkData> hangoutFolkDataSet = createHangoutFolkDataSet(hangoutData, userId);
        HangoutStoreHolder.instance.setHangout(hangoutData);
        HangoutStoreHolder.instance.setHangoutFolks(hangoutFolkDataSet);
        return hangOut;
    }

    @Override
    public Hangout getHangoutById(
            @ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId,
            @ThriftField(value = 2, name = "hangOutId", requiredness = ThriftField.Requiredness.NONE) long hangOutId)
            throws TException {

        verifyUser(userId);
        HangoutStoreHolder.instance.getHangout(hangOutId);
        ConcurrentHashMap<Long, UserHangoutInfo> uhs = userHangOutsDB.get(userId);
        if (uhs != null && uhs.get(hangOutId) != null) {
            HangoutInfo hangoutInfo = hangOutsDB.get(hangOutId);
            List<Long> participators = hangoutInfo.getParticipators();
            if (participators == null || !participators.contains(userId)) {
                return null;
            }
            Hangout hangout = new Hangout();
            hangout.setId(hangOutId);
            hangout.setActivity(hangoutInfo.getActivity());
            hangout.setStartTime(hangoutInfo.getStartTime());
            hangout.setEndTime(hangoutInfo.getEndTime());
            hangout.setLocation(hangoutInfo.getLocation());
            hangout.setState(hangoutInfo.getState());
            hangout.setSubject(hangoutInfo.getSubject());
            hangout.setParticipators(new ArrayList<Participator>());

            for (Long pId : participators) {
                User p = IdentityClientHolder.instance.getUser(pId);
                if (p == null) {
                    return null;
                }
                if (userHangOutsDB.get(pId) == null || userHangOutsDB.get(pId).get(hangOutId) == null) {
                    return null;
                }
                UserHangoutInfo userHangoutInfo = userHangOutsDB.get(pId).get(hangOutId);
                Participator participator = new Participator();
                participator.setId(pId);
                participator.setState(userHangoutInfo.getState());
                participator.setRole(userHangoutInfo.getRole());
                participator.setComment(userHangoutInfo.getComment());
                participator.setLoginName(p.getLoginName());
                participator.setFriendlyName(p.getFriendlyName());
                participator.setAvatarInfo(p.getAvatarInfo());
                hangout.getParticipators().add(participator);
            }
            return hangout;
        }
        return null;
    }

    @Override
    public List<HangoutSummary> getHangoutByStatus(@ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId, @ThriftField(value = 2, name = "status", requiredness = ThriftField.Requiredness.NONE) String status) throws TException {
        User userInfo = IdentityClientHolder.instance.getUser(userId);
        if (userInfo == null) {
            return null;
        }
        HangoutState hangoutState;
        try {
            hangoutState = HangoutState.valueOf(status.toUpperCase());
        } catch (Exception ex) {
            return null;
        }

        ConcurrentHashMap<Long, UserHangoutInfo> uhs = userHangOutsDB.get(userId);
        List<HangoutSummary> hangoutSummaries = new ArrayList<>();
        if (uhs != null) {
            for (Map.Entry<Long, UserHangoutInfo> kvp : uhs.entrySet()) {
                Long hangOutId = kvp.getKey();
                HangoutInfo hangOutInfo = hangOutsDB.get(hangOutId);
                if (hangOutInfo == null) {
                    return null;
                }
                if (hangOutInfo.getState() == hangoutState) {
                    HangoutSummary hangOutSummary = new HangoutSummary();
                    hangOutSummary.setId(hangOutId);
                    hangOutSummary.setSubject(hangOutInfo.getSubject());
                    hangOutSummary.setActivity(hangOutInfo.getActivity());
                    hangOutSummary.setState(hangoutState);
                    hangOutSummary.setNumPending(hangOutInfo.getNumPending());
                    hangOutSummary.setNumAccepted(hangOutInfo.getNumAccepted());
                    hangOutSummary.setNumRejected(hangOutInfo.getNumRejected());

                    userInfo = IdentityClientHolder.instance.getUser(hangOutInfo.getOrganizerId());
                    if (userInfo == null) {
                        return null;
                    }
                    Participator organizer = new Participator();
                    organizer.setId(hangOutInfo.getOrganizerId());
                    organizer.setLoginName(userInfo.getLoginName());
                    organizer.setFriendlyName(userInfo.getFriendlyName());
                    organizer.setAvatarInfo(userInfo.getAvatarInfo());
                    hangOutSummary.setOrganizer(organizer);

                    hangoutSummaries.add(hangOutSummary);
                }
            }
            return hangoutSummaries;
        }
        return null;
    }

    @Override
    public void updateHangoutStatus(@ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId, @ThriftField(value = 2, name = "hangOutId", requiredness = ThriftField.Requiredness.NONE) long hangOutId, @ThriftField(value = 3, name = "participators", requiredness = ThriftField.Requiredness.NONE) Hangout hangout) throws TException {
        List<Participator> participators = hangout.getParticipators();
        if (participators == null || participators.size() < 1) {
            return;
        }
        User userInfo = IdentityClientHolder.instance.getUser(userId);
        if (userInfo == null) {
            return;
        }

        UserHangoutInfo userHangoutInfo = userHangOutsDB.get(userId) == null ? null : userHangOutsDB.get(userId).get(hangOutId);
        HangoutInfo hangOutInfo = hangOutsDB.get(hangOutId);
        if (userHangoutInfo == null || hangOutInfo == null) {
            return;
        }
        Participator p = participators.get(0);
        ParticipatorState originState = userHangoutInfo.getState();
        userHangoutInfo.setComment(p.getComment());
        userHangoutInfo.setState(p.getState());
        userHangOutsDB.get(userId).put(hangOutId, userHangoutInfo);

        int accepted = hangOutInfo.getNumAccepted();
        int pending = hangOutInfo.getNumPending();
        int reject = hangOutInfo.getNumRejected();

        switch (p.getState()) {
            default:
            case ACCEPT:
                accepted++;
                break;
            case PENDING:
                pending++;
                break;
            case REJECT:
                reject++;
                break;
        }

        switch (originState) {
            case ACCEPT:
                accepted--;
                break;
            case REJECT:
                reject--;
                break;
            default:
            case PENDING:
                pending--;
                break;
        }
        hangOutInfo.setNumAccepted(accepted);
        hangOutInfo.setNumPending(pending);
        hangOutInfo.setNumRejected(reject);

        hangOutsDB.put(hangOutId, hangOutInfo);
    }

    private User verifyUser(long userId) throws TException {
        return IdentityClientHolder.instance.getUser(userId);
    }

    private Map<Long, User> verifyParticipators(List<Participator> participators) throws TException {
        if (participators == null || participators.isEmpty()) {
            return new HashMap<>();
        }
        Set<Long> ids = new HashSet<>();
        for (Participator p : participators) {
            ids.add(p.getId());
        }
        return IdentityClientHolder.instance.getUsers(ids);
    }

    private HangoutData toHangoutData(Hangout hangout, long organizerId) {
        if (hangout == null) {
            return null;
        }
        HangoutData hangoutData = new HangoutData();
        hangoutData.setHangoutId(hangout.getId());
        hangoutData.setCreatorId(organizerId);
        hangoutData.setActivity(hangout.getActivity());
        hangoutData.setSubject(hangout.getSubject());
        hangoutData.setLocation(hangout.getLocation());
        try {
            hangoutData.setStartTime(Common.simpleDateFormat.parse(hangout.getStartTime()));
            hangoutData.setEndTime(Common.simpleDateFormat.parse(hangout.getEndTime()));
        } catch (ParseException ex) {
            logger.error("failed to parse datetime", ex);
            throw new DataFormatException("failed to parse hangout datetime", ex);
        }
        Set<Long> participators = new HashSet<>();
        if (hangout.getParticipators() != null) {
            for (Participator participator : hangout.getParticipators()) {
                if (participator.getId() != organizerId) {
                    participators.add(participator.getId());
                }
            }
        }
        hangoutData.setParticipators(participators);
        return hangoutData;
    }

    private Set<HangoutFolkData> createHangoutFolkDataSet(
            final HangoutData hangoutData,
            final long organizerId) {

        Set<HangoutFolkData> folkSet = new HashSet<>();
        folkSet.add(createHangoutFolkData(organizerId, hangoutData.getHangoutId(), true, hangoutData.getUpdateTime()));
        folkSet.addAll(Collections2.transform(
                hangoutData.getParticipators(),
                new Function<Long, HangoutFolkData>() {
                    @NotNull
                    @Override
                    public HangoutFolkData apply(@NotNull Long id) {
                        return createHangoutFolkData(id, hangoutData.getHangoutId(), false, hangoutData.getUpdateTime());
                    }
                }));
        return folkSet;
    }

    private HangoutFolkData createHangoutFolkData(
            final long userId,
            final long hangoutId,
            final boolean isOrganizer,
            final Date dt) {

        HangoutFolkData hangoutFolkData = new HangoutFolkData();
        hangoutFolkData.setUserId(userId);
        hangoutFolkData.setHangoutId(hangoutId);
        hangoutFolkData.setRole(isOrganizer ?
                HangoutParticipatorRoleData.ORGANIZER :
                HangoutParticipatorRoleData.INVITEE);
        hangoutFolkData.setState(isOrganizer ?
                HangoutParticipatorStateData.ACCEPTED :
                HangoutParticipatorStateData.PENDING);
        hangoutFolkData.setUpdateTime(dt);
        return hangoutFolkData;
    }

    private HangoutInfo toHangoutInfo(Hangout hangOut, Participator organizer) {
        if (hangOut == null || organizer == null) {
            return null;
        }
        HangoutInfo hangoutInfo = new HangoutInfo();
        hangoutInfo.setStartTime(hangOut.getStartTime());
        hangoutInfo.setEndTime(hangOut.getEndTime());
        hangoutInfo.setState(HangoutState.ACTIVE);
        hangoutInfo.setActivity(hangOut.getActivity());
        hangoutInfo.setLocation(hangOut.getLocation());
        hangoutInfo.setSubject(hangOut.getSubject());
        hangoutInfo.setParticipators(new ArrayList<Long>());

        if (hangOut.getParticipators() != null) {
            for (Participator p : hangOut.getParticipators()) {
                if (p.getId() == organizer.getId()) {
                    return null;
                }
                hangoutInfo.getParticipators().add(p.getId());
            }
        }
        hangoutInfo.getParticipators().add(organizer.getId());
        hangoutInfo.setNumAccepted(0);
        hangoutInfo.setNumPending(hangoutInfo.getParticipators().size() - 1);
        return hangoutInfo;
    }

    private List<UserHangoutInfo> toUserHangoutInfos(Hangout hangout, Participator organizer) {
        if (hangout == null || organizer == null) {
            return null;
        }
        List<Participator> participators = new ArrayList<>();
        participators.add(organizer);
        participators.addAll(hangout.getParticipators());

        List<UserHangoutInfo> userHangoutInfos = new ArrayList<>();
        for (Participator p : participators) {
            if (p != null) {
                UserHangoutInfo userHangoutInfo = new UserHangoutInfo();
                userHangoutInfo.setHangoutId(hangout.getId());
                userHangoutInfo.setState(p.getState() == null ? ParticipatorState.PENDING : p.getState());
                userHangoutInfo.setComment(p.getComment());
                userHangoutInfo.setRole(p.getRole() == null ? Role.INVITEE : p.getRole());
                userHangoutInfo.setUserId(p.getId());
                userHangoutInfos.add(userHangoutInfo);
            }
        }
        return userHangoutInfos;
    }
}
