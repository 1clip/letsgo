package coffee.letsgo.hangout.store;

import coffee.letsgo.hangout.store.model.HangoutData;
import coffee.letsgo.hangout.store.model.HangoutFolkData;
import coffee.letsgo.storeland.CassandraSessionBuilder;
import com.datastax.driver.core.Session;

import java.util.List;

/**
 * Created by xbwu on 10/19/14.
 */
public class HangoutStoreCassandraImpl implements HangoutStore {

    @Override
    public void setHangout(HangoutData hangoutData) {

    }

    @Override
    public void setHangoutFolk(HangoutFolkData hangoutFolkData) {

    }

    @Override
    public void setHangoutFolks(List<HangoutFolkData> hangoutFolks) {

    }

    @Override
    public HangoutData getHangout(long hangoutId) {
        return null;
    }

    @Override
    public List<HangoutFolkData> getHangoutFolks(long hangoutId) {
        return null;
    }

    @Override
    public HangoutFolkData getHangoutFolk(long hangoutId, long userId) {
        return null;
    }

    private static class CassandraSessionHolder {
        private static Session session = new CassandraSessionBuilder("hangout").build();

        private static Session getSession() {
            return session;
        }
    }
}
