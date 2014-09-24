package coffee.letsgo.iceflake.config;

import com.google.inject.Singleton;

/**
 * Created by xbwu on 8/29/14.
 */
@Singleton
public class IceflakeConfig {

    public int getWorkerId() {
        return 7;
    }

    public int getServerPort() {
        return 7069;
    }

    public String getServerName() {
        return "localhost";
    }
}

