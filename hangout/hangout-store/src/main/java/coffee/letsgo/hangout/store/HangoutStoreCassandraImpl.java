package coffee.letsgo.hangout.store;

import coffee.letsgo.hangout.store.model.HangoutData;
import coffee.letsgo.hangout.store.model.HangoutFolkData;
import coffee.letsgo.storeland.CassandraSessionBuilder;
import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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

    private final ListeningExecutorService executorPool =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    private static final Function<Object, Void> NOOP = Functions.constant(null);
    private static final Function<ResultSet, List<HangoutFolkData>> hangoutFolksFetcher =
            new Function<ResultSet, List<HangoutFolkData>>() {
                @Override
                public List<HangoutFolkData> apply(ResultSet input) {
                    return null;
                }
            };
    private static final Function<ResultSet, List<HangoutData>> hangoutsFetcher =
            new Function<ResultSet, List<HangoutData>>() {
                @Override
                public List<HangoutData> apply(ResultSet input) {
                    return null;
                }
            };

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
    public ListenableFuture<List<HangoutFolkData>> getHangoutFolks(long hangoutId) {
        final ListenableFuture<HangoutData> future = getHangout(hangoutId);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }, executorPool);
        return null;
    }

    @Override
    public ListenableFuture<HangoutFolkData> getHangoutFolk(long hangoutId, long userId) {
        return hangoutFolkMapper.getAsync(hangoutId, userId);
    }

    @Override
    public ListenableFuture<List<HangoutData>> getHangouts(long userId) {
        return null;
    }
}
