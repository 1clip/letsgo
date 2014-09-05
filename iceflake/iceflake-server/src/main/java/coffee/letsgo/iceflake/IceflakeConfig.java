package coffee.letsgo.iceflake;

/**
 * Created by xbwu on 8/29/14.
 */
public abstract class IceflakeConfig {

    public IceflakeServer apply() {
        return new IceflakeServer(
                getWorkerId(),
                getServerPort());
    }

    public abstract int getWorkerId();

    public abstract int getServerPort();
}

class DevConfig extends IceflakeConfig {

    @Override
    public int getWorkerId() {
        return 12;
    }

    @Override
    public int getServerPort() {
        return 7609;
    }
}
