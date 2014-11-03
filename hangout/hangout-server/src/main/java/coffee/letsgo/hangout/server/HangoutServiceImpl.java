package coffee.letsgo.hangout.server;

import coffee.letsgo.common.Common;
import coffee.letsgo.hangout.*;
import coffee.letsgo.hangout.exception.BadRequestException;
import coffee.letsgo.hangout.exception.DataFormatException;
import coffee.letsgo.hangout.exception.HangoutInternalException;
import coffee.letsgo.hangout.exception.HangoutNotFoundException;
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
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by yfang on 10/4/14.
 */
public class HangoutServiceImpl implements HangoutService {
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

        HangoutData hangoutData;
        try {
            hangoutData = HangoutStoreHolder.instance.getHangout(hangOutId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting hangout data for id %d", hangOutId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangout data for id %d", hangOutId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        }
        if (!checkHangoutVisibility(hangoutData, userId)) {
            logger.error("hangout id {} is not visible to user {}", hangOutId, userId);
            throw new HangoutNotFoundException(String.format(
                    "hangout id %d not found for user %d", hangOutId, userId));
        }

        return composeHangout(hangoutData);
    }

    @Override
    public List<HangoutSummary> getHangoutByStatus(
            @ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId,
            @ThriftField(value = 2, name = "status", requiredness = ThriftField.Requiredness.NONE) final HangoutState status)
            throws TException {

        Collection<HangoutData> hangouts = Collections2.filter(
                getHangouts(userId),
                new Predicate<HangoutData>() {
                    @Override
                    public boolean apply(@NotNull HangoutData hangoutData) {
                        return getHangoutState(hangoutData) == status;
                    }
                }
        );
        List<HangoutSummary> hangoutSummaries = new ArrayList<>();
        for (HangoutData hangoutData : hangouts) {
            List<Participator> participators = getParticipators(hangoutData);
            Participator organizer = null;
            int numAccepted = 0, numPending = 0, numRejected = 0;
            for (Participator participator : participators) {
                switch (participator.getState()) {
                    case ACCEPTED:
                        ++numAccepted;
                        break;
                    case PENDING:
                        ++numPending;
                        break;
                    case REJECTED:
                        ++numRejected;
                        break;
                    default:
                        logger.error("unrecognized participator state {}", participator.getState().name());
                }
                if (participator.getId() == hangoutData.getCreatorId()) {
                    organizer = participator;
                }
            }
            if (organizer == null) {
                throw new HangoutInternalException(String.format(
                        "failed to get organizer for hangout id %d", hangoutData.getHangoutId()));
            }
            HangoutSummary hangoutSummary = new HangoutSummary();
            hangoutSummary.setId(hangoutData.getHangoutId());
            hangoutSummary.setActivity(hangoutData.getActivity());
            hangoutSummary.setSubject(hangoutData.getSubject());
            hangoutSummary.setOrganizer(organizer);
            hangoutSummary.setState(getHangoutState(hangoutData));
            hangoutSummary.setNumAccepted(numAccepted);
            hangoutSummary.setNumPending(numPending);
            hangoutSummary.setNumRejected(numRejected);
            hangoutSummaries.add(hangoutSummary);
        }
        return hangoutSummaries;
    }

    @Override
    public void updateHangoutStatus(
            @ThriftField(value = 1, name = "userId", requiredness = ThriftField.Requiredness.NONE) long userId,
            @ThriftField(value = 2, name = "hangOutId", requiredness = ThriftField.Requiredness.NONE) long hangOutId,
            @ThriftField(value = 3, name = "participators", requiredness = ThriftField.Requiredness.NONE) Hangout hangout)
            throws TException {
        HangoutFolkData hangoutFolkData = getHangoutFolk(hangOutId, userId);
        if(hangoutFolkData == null) {
            throw new HangoutNotFoundException(String.format(
                    "hangout %d not found for user %d",
                    hangOutId, userId));
        }
        if(hangout.getParticipators() == null || hangout.getParticipators().size() != 1) {
            throw new BadRequestException("exactly 1 participator required");
        }
        Participator participator = hangout.getParticipators().get(0);
        hangoutFolkData.setState(HangoutParticipatorStateData.valueOf(participator.getState().name()));
        hangoutFolkData.setComment(participator.getComment());
        if(hangoutFolkData.getRole() == HangoutParticipatorRoleData.ORGANIZER &&
                hangoutFolkData.getState() != HangoutParticipatorStateData.ACCEPTED) {
            throw new BadRequestException("organizer could not change status to any other then ACCEPTED");
        }
        HangoutStoreHolder.instance.setHangoutFolk(hangoutFolkData);
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

    private boolean checkHangoutVisibility(HangoutData hangoutData, long userId) {
        return (hangoutData != null && (
                hangoutData.getCreatorId() == userId || hangoutData.getParticipators().contains(userId)));
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
            hangoutData.setStartTime(Common.simpleDateTimeFormat.parse(hangout.getStartTime()));
            hangoutData.setEndTime(Common.simpleDateTimeFormat.parse(hangout.getEndTime()));
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

    private Hangout composeHangout(HangoutData hangoutData) throws TException {

        Hangout hangout = new Hangout();
        hangout.setId(hangoutData.getHangoutId());
        hangout.setActivity(hangoutData.getActivity());
        hangout.setSubject(hangoutData.getSubject());
        hangout.setLocation(hangoutData.getLocation());
        hangout.setStartTime(Common.simpleDateTimeFormat.format(hangoutData.getStartTime()));
        hangout.setEndTime(Common.simpleDateTimeFormat.format(hangoutData.getEndTime()));
        hangout.setState(getHangoutState(hangoutData));
        hangout.setParticipators(getParticipators(hangoutData));
        return hangout;
    }

    private List<Participator> getParticipators(HangoutData hangoutData) throws TException {
        final Collection<HangoutFolkData> hangoutFolkDataSet = getHangoutFolks(hangoutData.getHangoutId());
        final Map<Long, User> usersDict = getUsersDict(hangoutData);
        List<Participator> participators = new ArrayList<>(Collections2.transform(
                hangoutFolkDataSet,
                new Function<HangoutFolkData, Participator>() {
                    @NotNull
                    @Override
                    public Participator apply(@NotNull final HangoutFolkData hangoutFolkData) {
                        long uid = hangoutFolkData.getUserId();
                        Participator participator = new Participator();
                        participator.setId(uid);
                        participator.setRole(Role.valueOf(hangoutFolkData.getRole().name()));
                        participator.setState(ParticipatorState.valueOf(hangoutFolkData.getState().name()));
                        User user = usersDict.get(uid);
                        participator.setLoginName(user.getLoginName());
                        participator.setFriendlyName(user.getFriendlyName());
                        participator.setAvatarInfo(user.getAvatarInfo());
                        return participator;
                    }
                }
        ));
        return participators;
    }

    private HangoutState getHangoutState(HangoutData hangoutData) {
        if (hangoutData.isCanceled()) {
            return HangoutState.CANCELED;
        }
        Date now = new Date();
        if (hangoutData.getEndTime().before(now)) {
            return HangoutState.OVERDUE;
        }
        if (hangoutData.getStartTime().after(now)) {
            return HangoutState.ACTIVE;
        }
        return HangoutState.HAPPENING;
    }

    private Collection<HangoutFolkData> getHangoutFolks(long hangoutId) {
        Collection<HangoutFolkData> hangoutFolks;
        try {
            hangoutFolks = HangoutStoreHolder.instance.getHangoutFolks(hangoutId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting hangout folks for hangout id %d", hangoutId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangout folks for hangout id %d", hangoutId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        }
        return hangoutFolks;
    }

    private Collection<HangoutData> getHangouts(long userId) {
        Collection<HangoutData> hangouts;
        try {
            hangouts = HangoutStoreHolder.instance.getHangouts(userId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting hangouts for user %d", userId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangouts for user %d", userId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        }
        return hangouts;
    }

    private HangoutData getHangout(long hangoutId) {
        HangoutData hangout;
        try {
            hangout = HangoutStoreHolder.instance.getHangout(hangoutId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting hangout with id %d", hangoutId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangout with id %d", hangoutId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        }
        return hangout;
    }

    private HangoutFolkData getHangoutFolk(long hangoutId, long userId) {
        HangoutFolkData hangoutFolkData;
        try {
            hangoutFolkData = HangoutStoreHolder.instance.getHangoutFolk(userId, hangoutId).get();
        } catch (InterruptedException ex) {
            String msg = String.format(
                    "interrupted getting hangout folk with hangout id %d, user id %d",
                    hangoutId, userId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        } catch (ExecutionException ex) {
            String msg = String.format(
                    "execution failure getting hangout folk with hangout id %d, user id %d",
                    hangoutId, userId);
            logger.error(msg, ex);
            throw new HangoutInternalException(msg, ex);
        }
        return hangoutFolkData;
    }

    private Map<Long, User> getUsersDict(HangoutData hangoutData) throws TException {
        Set<Long> ids = hangoutData.getParticipators();
        ids.add(hangoutData.getCreatorId());
        return IdentityClientHolder.instance.getUsers(ids);
    }
}
