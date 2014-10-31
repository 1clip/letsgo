package coffee.letsgo.identity.store;

import coffee.letsgo.identity.store.model.UserData;
import coffee.letsgo.storeland.CassandraSessionBuilder;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by xbwu on 10/18/14.
 */
public class IdentityStoreCassandraImpl implements IdentityStore {
    private final Session session =
            new CassandraSessionBuilder("user").build();

    private final Mapper<UserData> userMapper =
            new MappingManager(session).mapper(UserData.class);

    private static final Function<Object, Void> NOOP = Functions.constant(null);

    private final Executor executor = Executors.newFixedThreadPool(10);

    @Override
    public ListenableFuture<Void> createUser(UserData userData) {
        return userMapper.saveAsync(userData);
    }

    @Override
    public ListenableFuture<UserData> getUser(long userId) {
        return userMapper.getAsync(userId);
    }
}
