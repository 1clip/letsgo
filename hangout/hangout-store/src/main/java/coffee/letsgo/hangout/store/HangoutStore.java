package coffee.letsgo.hangout.store;

import coffee.letsgo.hangout.store.model.HangoutData;
import coffee.letsgo.hangout.store.model.HangoutFolkData;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * Created by xbwu on 10/19/14.
 */
public interface HangoutStore {
    ListenableFuture<Void> setHangout(HangoutData hangoutData);

    ListenableFuture<Void> setHangoutFolk(HangoutFolkData hangoutFolkData);

    ListenableFuture<Void> setHangoutFolks(List<HangoutFolkData> hangoutFolks);

    ListenableFuture<HangoutData> getHangout(long hangoutId);

    ListenableFuture<List<HangoutFolkData>> getHangoutFolks(long hangoutId);

    ListenableFuture<HangoutFolkData> getHangoutFolk(long hangoutId, long userId);

    ListenableFuture<List<HangoutData>> getHangouts(long userId);
}
