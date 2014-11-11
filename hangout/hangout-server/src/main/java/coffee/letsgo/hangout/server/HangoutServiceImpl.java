package coffee.letsgo.hangout.server;

import coffee.letsgo.common.Common;
import coffee.letsgo.hangout.*;
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
import coffee.letsgo.identity.UserNotFoundException;
import coffee.letsgo.identity.client.IdentityClient;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    public Hangout createHangout(long userId, Hangout hangOut)
            throws InvalidHangoutDataException, HangoutProcessException, TException {

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
    public Hangout getHangoutById(long userId, long hangOutId)
            throws HangoutNotFoundException, HangoutProcessException, TException {

        HangoutData hangoutData;
        try {
            hangoutData = HangoutStoreHolder.instance.getHangout(hangOutId).get();
        } catch (InterruptedException ex) {
            String msg = String.format("interrupted getting hangout data for id %d", hangOutId);
            logger.error(msg, ex);
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangout data for id %d", hangOutId);
            logger.error(msg, ex);
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        }
        if (!checkHangoutVisibility(hangoutData, userId)) {
            String msg = String.format("hangout %d not visible to user %d", hangOutId, userId);
            logger.error(msg);
            HangoutNotFoundException hangoutNotFoundException = new HangoutNotFoundException();
            hangoutNotFoundException.setMsg(msg);
            throw hangoutNotFoundException;
        }

        return composeHangout(hangoutData);
    }

    @Override
    public List<HangoutSummary> getHangoutByStatus(long userId, final HangoutState status)
            throws HangoutProcessException, TException {

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
                String msg = String.format(
                        "failed to get organizer for hangout id %d", hangoutData.getHangoutId());
                logger.error(msg);
                HangoutProcessException hangoutProcessException = new HangoutProcessException();
                hangoutProcessException.setMsg(msg);
                throw hangoutProcessException;
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
    public void updateHangout(long userId, long hangOutId, Hangout hangout)
            throws InvalidHangoutDataException, HangoutNotFoundException, HangoutProcessException, TException {
        HangoutFolkData hangoutFolkData = getHangoutFolk(hangOutId, userId);
        if (hangoutFolkData == null) {
            String msg = String.format("hangout %d not found for user %d", hangOutId, userId);
            logger.error(msg);
            HangoutNotFoundException hangoutNotFoundException = new HangoutNotFoundException();
            hangoutNotFoundException.setMsg(msg);
            throw hangoutNotFoundException;
        }
        if (hangout.getParticipators() == null || hangout.getParticipators().size() != 1) {
            InvalidHangoutDataException invalidHangoutDataException = new InvalidHangoutDataException();
            invalidHangoutDataException.setMsg("exactly 1 participator required");
            throw invalidHangoutDataException;
        }
        Participator participator = hangout.getParticipators().get(0);
        hangoutFolkData.setState(HangoutParticipatorStateData.valueOf(participator.getState().name()));
        hangoutFolkData.setComment(participator.getComment());
        if (hangoutFolkData.getRole() == HangoutParticipatorRoleData.ORGANIZER &&
                hangoutFolkData.getState() != HangoutParticipatorStateData.ACCEPTED) {
            InvalidHangoutDataException invalidHangoutDataException = new InvalidHangoutDataException();
            invalidHangoutDataException.setMsg("organizer could not change status to any other then ACCEPTED");
            throw invalidHangoutDataException;
        }
        HangoutStoreHolder.instance.setHangoutFolk(hangoutFolkData);
        if (hangoutFolkData.getRole() != HangoutParticipatorRoleData.ORGANIZER) {
            return;
        }
        if (hangout.getActivity() == null ||
                hangout.getSubject() == null ||
                hangout.getLocation() == null ||
                hangout.getStartTime() == null ||
                hangout.getEndTime() == null ||
                hangout.getState() == null) {

            logger.debug("start updating hangout info of {} by user {}", hangOutId, userId);
            HangoutData hangoutData = getHangout(hangOutId);
            if (hangout.getActivity() != null) {
                hangoutData.setActivity(hangout.getActivity());
            }
            if (hangout.getSubject() != null) {
                hangoutData.setSubject(hangout.getSubject());
            }
            if (hangout.getLocation() != null) {
                hangoutData.setLocation(hangout.getLocation());
            }
            if (hangout.getStartTime() != null) {
                try {
                    hangoutData.setStartTime(Common.simpleDateFormat.parse(hangout.getStartTime()));
                } catch (ParseException ex) {
                    InvalidHangoutDataException invalidHangoutDataException = new InvalidHangoutDataException();
                    invalidHangoutDataException.setMsg(String.format(
                            "unsupported format of start_time %s", hangout.getStartTime()));
                    throw invalidHangoutDataException;
                }
            }
            if (hangout.getEndTime() != null) {
                try {
                    hangoutData.setEndTime(Common.simpleDateFormat.parse(hangout.getEndTime()));
                } catch (ParseException ex) {
                    InvalidHangoutDataException invalidHangoutDataException = new InvalidHangoutDataException();
                    invalidHangoutDataException.setMsg(String.format(
                            "unsupported format of end_time %s", hangout.getEndTime()));
                    throw invalidHangoutDataException;
                }
            }
            if (hangout.getState() != null) {
                if (hangout.getState() == HangoutState.CANCELED) {
                    hangoutData.setCanceled(true);
                } else {
                    logger.info("ignore updating state of hangout {} to {}", hangOutId, hangout.getState().name());
                }
            }
            HangoutStoreHolder.instance.setHangout(hangoutData);
        }
        if (hangout.getParticipators() != null) {
            //TODO: atomically update participators
            throw new NotImplementedException();
        }
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
        try {
            return IdentityClientHolder.instance.getUsers(ids);
        } catch (UserNotFoundException ex) {
            String msg = String.format("user not found, %s", ex);
            logger.error(msg);
            InvalidHangoutDataException invalidHangoutDataException = new InvalidHangoutDataException();
            invalidHangoutDataException.setMsg(msg);
            throw invalidHangoutDataException;
        }
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
            String msg = String.format("failed to parse datetime, %s", ex);
            logger.error(msg);
            InvalidHangoutDataException invalidHangoutDataException = new InvalidHangoutDataException();
            invalidHangoutDataException.setMsg(msg);
            throw invalidHangoutDataException;
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
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangout folks for hangout id %d", hangoutId);
            logger.error(msg, ex);
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
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
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangouts for user %d", userId);
            logger.error(msg, ex);
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
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
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format("execution failure getting hangout with id %d", hangoutId);
            logger.error(msg, ex);
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
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
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        } catch (ExecutionException ex) {
            String msg = String.format(
                    "execution failure getting hangout folk with hangout id %d, user id %d",
                    hangoutId, userId);
            logger.error(msg, ex);
            HangoutProcessException hangoutProcessException = new HangoutProcessException();
            hangoutProcessException.setMsg(msg);
            throw hangoutProcessException;
        }
        return hangoutFolkData;
    }

    private Map<Long, User> getUsersDict(HangoutData hangoutData) throws TException {
        Set<Long> ids = hangoutData.getParticipators();
        ids.add(hangoutData.getCreatorId());
        return IdentityClientHolder.instance.getUsers(ids);
    }
}
