package coffee.letsgo.identity.store;

import coffee.letsgo.identity.store.model.UserData;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Created by xbwu on 10/8/14.
 */
public interface IdentityStore {
    ListenableFuture<Void> createUser(UserData userData);

    ListenableFuture<UserData> getUser(long userId);
}
