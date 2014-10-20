package coffee.letsgo.hangout.store;

import coffee.letsgo.hangout.store.model.HangoutData;
import coffee.letsgo.hangout.store.model.HangoutFolkData;

import java.util.List;

/**
 * Created by xbwu on 10/19/14.
 */
public interface HangoutStore {
    void setHangout(HangoutData hangoutData);

    void setHangoutFolk(HangoutFolkData hangoutFolkData);

    void setHangoutFolks(List<HangoutFolkData> hangoutFolks);

    HangoutData getHangout(long hangoutId);

    List<HangoutFolkData> getHangoutFolks(long hangoutId);

    HangoutFolkData getHangoutFolk(long hangoutId, long userId);
}
