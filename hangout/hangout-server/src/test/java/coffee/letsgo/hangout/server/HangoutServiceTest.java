package coffee.letsgo.hangout.server;

import coffee.letsgo.avatar.AvatarInfo;
import coffee.letsgo.hangout.*;
import coffee.letsgo.hangout.client.HangoutClient;
import coffee.letsgo.iceflake.client.IceflakeClientException;
import coffee.letsgo.iceflake.config.IceflakeConfigException;
import coffee.letsgo.iceflake.server.IceflakeServer;
import coffee.letsgo.identity.Gender;
import coffee.letsgo.identity.SignupType;
import coffee.letsgo.identity.User;
import coffee.letsgo.identity.client.IdentityClient;
import coffee.letsgo.identity.server.IdentityServer;
import org.apache.thrift.TException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

/**
 * Created by yfang on 10/6/14.
 */
public class HangoutServiceTest {
    private final int serverId = 9;
    private final int serverPort = 8089;
    private IceflakeServer iceflakeServer;
    private IdentityServer identityServer;
    private HangoutServer hangoutServer;
    private Random random;

    @Test
    public void TestHangoutFlow() throws TException {
        // post user u1
        User u1 = createUser();
        Assert.assertNotNull(u1.getId());

        // post user u2
        User u2 = createUser();
        Assert.assertNotNull(u2.getId());

        // post hangout
        Hangout hangout = createHangout(u1.getId(), u2.getId());
        Assert.assertNotNull(hangout);
        Assert.assertNotNull(hangout.getId());

        // get hangout by id
        Hangout hangout1 = getHangoutById(u1.getId(), hangout.getId());
        Hangout hangout2 = getHangoutById(u2.getId(), hangout.getId());
        Assert.assertNotNull(hangout1);
        Assert.assertNotNull(hangout2);

        // get hangout summary by status
        List<HangoutSummary> hangoutSummaries = getHangoutByStatus(u1.getId(), HangoutState.ACTIVE);
        Assert.assertNotNull(hangoutSummaries);
        Assert.assertEquals(hangoutSummaries.size(), 1);
        HangoutSummary hangoutSummary = hangoutSummaries.get(0);
        Assert.assertEquals(hangoutSummary.getNumAccepted(), 1);
        Assert.assertEquals(hangoutSummary.getNumPending(), 1);
        Assert.assertEquals(hangoutSummary.getNumRejected(), 0);

        // patch hangout status
        updateHangoutStatus(u2.getId(), hangout.getId());

        // get hangout summary by status again
        hangoutSummaries = getHangoutByStatus(u1.getId(), HangoutState.ACTIVE);
        Assert.assertNotNull(hangoutSummaries);
        Assert.assertEquals(hangoutSummaries.size(), 1);
        hangoutSummary = hangoutSummaries.get(0);
        Assert.assertEquals(hangoutSummary.getNumAccepted(), 2);
        Assert.assertEquals(hangoutSummary.getNumPending(), 0);
        Assert.assertEquals(hangoutSummary.getNumRejected(), 0);

        Hangout hangout_new = createHangout(u2.getId(), u1.getId());
        Assert.assertNotNull(hangout_new);
        Assert.assertNotNull(hangout_new.getId());

        // get hangout summary by status
        hangoutSummaries = getHangoutByStatus(u2.getId(), HangoutState.ACTIVE);
        Assert.assertNotNull(hangoutSummaries);
        Assert.assertEquals(hangoutSummaries.size(), 2);
    }

    private Hangout createHangout(long organizerId, long... userIds) throws TException {
        Hangout hangout = new Hangout();
        hangout.setActivity("Dinning");
        hangout.setSubject("birthday");
        hangout.setLocation("zixing rd");
        hangout.setStartTime("2014-12-12T09:00:00.000+0800");
        hangout.setEndTime("2014-12-12T12:00:00.000+0800");
        hangout.setParticipators(new ArrayList<Participator>());
        if (userIds != null && userIds.length > 0) {
            for (long userId : userIds) {
                Participator p = new Participator();
                p.setId(userId);
                hangout.getParticipators().add(p);
            }
        }
        return HangoutClient.getInstance().createHangout(organizerId, hangout);
    }

    private Hangout getHangoutById(long userId, long hangoutId) throws TException {
        return HangoutClient.getInstance().getHangoutById(userId, hangoutId);
    }

    private List<HangoutSummary> getHangoutByStatus(long userId, HangoutState hangoutState) throws TException {
        return HangoutClient.getInstance().getHangoutByStatus(userId, hangoutState);
    }

    private void updateHangoutStatus(long userId, long hangoutId) throws TException {
        Participator p = new Participator();
        p.setState(ParticipatorState.ACCEPTED);
        p.setComment("i like it");

        List<Participator> participators = new ArrayList<>();
        participators.add(p);
        Hangout hangout = new Hangout();
        hangout.setParticipators(participators);
        HangoutClient.getInstance().updateHangoutStatus(userId, hangoutId, hangout);
    }

    private User createUser() throws TException {
        User user = new User();
        user.setLoginName("u" + random.nextLong());
        user.setFriendlyName("uf" + random.nextLong());
        user.setGender(Gender.MALE);
        user.setDateOfBirth("1985-01-01");
        user.setCellPhone(String.valueOf(random.nextLong()));
        user.setSignUpType(SignupType.CELL_PHONE);

        AvatarInfo avatarInfo = new AvatarInfo();
        avatarInfo.setAvatarId(random.nextLong());
        avatarInfo.setAvatarToken(random.nextLong());
        user.setAvatarInfo(avatarInfo);
        return IdentityClient.getInstance().createUser(user);
    }

    private User getUser(long userId) throws TException {
        return IdentityClient.getInstance().getUser(userId);
    }

    @BeforeTest
    public void setup() throws IceflakeClientException, IceflakeConfigException, UnknownHostException {
        iceflakeServer = new IceflakeServer(serverId, serverPort);
        iceflakeServer.start();

        identityServer = new IdentityServer();
        identityServer.start();

        hangoutServer = new HangoutServer();
        hangoutServer.start();

        random = new Random(System.currentTimeMillis());
    }

    @AfterTest
    public void teardown() {
        iceflakeServer.shutdown();
        identityServer.shutdown();
        hangoutServer.shutdown();
    }
}
