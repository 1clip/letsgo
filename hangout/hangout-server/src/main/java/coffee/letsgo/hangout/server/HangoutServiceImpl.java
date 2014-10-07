package coffee.letsgo.hangout.server;

import coffee.letsgo.hangout.*;
import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.client.IceflakeClient;
import coffee.letsgo.identity.*;
import coffee.letsgo.identity.client.IdentityClient;
import com.facebook.swift.codec.ThriftField;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yfang on 10/4/14.
 */
public class HangoutServiceImpl implements HangoutService {
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, UserHangoutInfo>> userHangOutsDB = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, HangoutInfo> hangOutsDB = new ConcurrentHashMap<>();

    private static class IceflakeClientHolder {
        private static Iceflake Instance = IceflakeClient.getInstance();
    }

    private static class IdentityClientHolder {
        private static IdentityService instance = IdentityClient.getInstance();
    }

    @Override
    public Hangout createHangout(@ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId, @ThriftField(value = 2, name = "hangOut", requiredness = ThriftField.Requiredness.NONE) Hangout hangOut) throws TException {
        User userInfo = IdentityClientHolder.instance.getUser(userId);
        if (userInfo == null) {
            return null;
        }
        long hangOutId = IceflakeClientHolder.Instance.getId(IdType.HANGOUT_ID);
        hangOut.setId(hangOutId);

        Participator organizer = new Participator();
        organizer.setId(userId);
        organizer.setRole(Role.ORGANIZER);
        organizer.setState(ParticipatorState.ACCEPT);

        HangoutInfo hangoutInfo = toHangoutInfo(hangOut, organizer);
        if (hangoutInfo == null) {
            return null;
        }
        hangoutInfo.setOrganizerId(organizer.getId());
        hangOutsDB.put(hangOutId, hangoutInfo);

        List<UserHangoutInfo> userHangoutInfos = toUserHangoutInfos(hangOut, organizer);
        for (UserHangoutInfo uh : userHangoutInfos) {
            ConcurrentHashMap<Long, UserHangoutInfo> uhs = userHangOutsDB.get(uh.getUserId());
            if (uhs == null) {
                uhs = new ConcurrentHashMap<>();
            }
            uhs.put(hangOutId, uh);
            userHangOutsDB.put(uh.getUserId(), uhs);
        }
        return hangOut;
    }

    @Override
    public Hangout getHangoutById(@ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId, @ThriftField(value = 2, name = "hangOutId", requiredness = ThriftField.Requiredness.NONE) long hangOutId) throws TException {
        User userInfo = IdentityClientHolder.instance.getUser(userId);
        if (userInfo == null) {
            return null;
        }
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
                participator.setAvatarInfo(p.getAvatar());
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
                    hangOutSummary.setNumDeclined(hangOutInfo.getNumDeclined());

                    userInfo = IdentityClientHolder.instance.getUser(hangOutInfo.getOrganizerId());
                    if (userInfo == null) {
                        return null;
                    }
                    Participator organizer = new Participator();
                    organizer.setId(hangOutInfo.getOrganizerId());
                    organizer.setLoginName(userInfo.getLoginName());
                    organizer.setFriendlyName(userInfo.getFriendlyName());
                    organizer.setAvatarInfo(userInfo.getAvatar());
                    hangOutSummary.setOrganizer(organizer);

                    hangoutSummaries.add(hangOutSummary);
                }
            }
            return hangoutSummaries;
        }
        return null;
    }

    @Override
    public void updateHangoutStatus(@ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId, @ThriftField(value = 2, name = "hangOutId", requiredness = ThriftField.Requiredness.NONE) long hangOutId, @ThriftField(value = 3, name = "participators", requiredness = ThriftField.Requiredness.NONE) List<Participator> participators) throws TException {
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
        int reject = hangOutInfo.getNumDeclined();

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
        hangOutInfo.setNumDeclined(reject);

        hangOutsDB.put(hangOutId, hangOutInfo);
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
