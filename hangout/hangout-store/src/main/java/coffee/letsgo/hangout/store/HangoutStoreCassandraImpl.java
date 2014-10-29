package coffee.letsgo.hangout.store;

import coffee.letsgo.hangout.store.model.HangoutData;
import coffee.letsgo.hangout.store.model.HangoutFolkData;
import coffee.letsgo.hangout.store.model.HangoutParticipatorRoleData;
import coffee.letsgo.hangout.store.model.HangoutParticipatorStateData;
import coffee.letsgo.storeland.CassandraSessionBuilder;
import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by xbwu on 10/19/14.
 */
public class HangoutStoreCassandraImpl implements HangoutStore {

    private final Session session =
            new CassandraSessionBuilder("hangout").build();

    private final Mapper<HangoutData> hangoutMapper =
            new MappingManager(session).mapper(HangoutData.class);

    private final Mapper<HangoutFolkData> hangoutFolkMapper =
            new MappingManager(session).mapper(HangoutFolkData.class);

    private final PreparedStatement getHangoutsByUserIdStatement =
            session.prepare(String.format(
                    "SELECT * FROM %s.%s WHERE user_id = ?;",
                    Constants.keyspace, Constants.hangoutFolkTableName));

    private static final Function<Object, Void> NOOP = Functions.constant(null);

    private final Executor executor = Executors.newFixedThreadPool(10);

    @Override
    public ListenableFuture<Void> setHangout(HangoutData hangoutData) {
        return hangoutMapper.saveAsync(hangoutData);
    }

    @Override
    public ListenableFuture<Void> setHangoutFolk(HangoutFolkData hangoutFolkData) {
        return hangoutFolkMapper.saveAsync(hangoutFolkData);
    }

    @Override
    public ListenableFuture<Void> setHangoutFolks(Set<HangoutFolkData> hangoutFolks) {
        BatchStatement batch = new BatchStatement();
        for (HangoutFolkData hangoutFolk : hangoutFolks) {
            batch.add(hangoutFolkMapper.saveQuery(hangoutFolk));
        }
        return Futures.transform(session.executeAsync(batch), NOOP);
    }

    @Override
    public ListenableFuture<HangoutData> getHangout(long hangoutId) {
        return hangoutMapper.getAsync(hangoutId);
    }

    @Override
    public ListenableFuture<Collection<HangoutFolkData>> getHangoutFolks(final long hangoutId) {
        return Futures.transform(getHangout(hangoutId), new Function<HangoutData, Collection<HangoutFolkData>>() {
            @Override
            public Collection<HangoutFolkData> apply(HangoutData hangoutData) {
                Set<Long> participatorIds = hangoutData.getParticipators();
                participatorIds.add(hangoutData.getCreatorId());
                Select.Where select = QueryBuilder.select()
                        .all()
                        .from(Constants.keyspace, Constants.hangoutFolkTableName)
                        .where(QueryBuilder.in("user_id", participatorIds.toArray()))
                        .and(QueryBuilder.eq("hangout_id", hangoutId));
                ResultSet resultSet = session.execute(select);
                return Lists.transform(
                        resultSet.all(),
                        new Function<Row, HangoutFolkData>() {
                            @Override
                            public HangoutFolkData apply(Row row) {
                                HangoutFolkData hangoutFolkData = new HangoutFolkData();
                                hangoutFolkData.setUserId(row.getLong("user_id"));
                                hangoutFolkData.setHangoutId(row.getLong("hangout_id"));
                                hangoutFolkData.setRole(HangoutParticipatorRoleData.valueOf(row.getString("role")));
                                hangoutFolkData.setState(HangoutParticipatorStateData.valueOf(row.getString("state")));
                                hangoutFolkData.setComment(row.getString("comment"));
                                hangoutFolkData.setUpdateTime(row.getDate("update_time"));
                                return hangoutFolkData;
                            }
                        });
            }
        }, executor);
    }

    @Override
    public ListenableFuture<HangoutFolkData> getHangoutFolk(long hangoutId, long userId) {
        return hangoutFolkMapper.getAsync(hangoutId, userId);
    }

    @Override
    public ListenableFuture<Collection<HangoutData>> getHangouts(long userId) {
        return Futures.transform(
                session.executeAsync(getHangoutsByUserIdStatement.bind(userId)),
                new Function<ResultSet, Collection<HangoutData>>() {
                    @Override
                    public Collection<HangoutData> apply(ResultSet resultSet) {
                        return Collections2.transform(
                                resultSet.all(),
                                new Function<Row, HangoutData>() {
                                    @Override
                                    public HangoutData apply(Row row) {
                                        return hangoutMapper.get(row.getLong("hangout_id"));
                                    }
                                }
                        );
                    }
                });
    }
}
