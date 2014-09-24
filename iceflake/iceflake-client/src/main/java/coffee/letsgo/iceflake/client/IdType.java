package coffee.letsgo.iceflake.client;

/**
 * Created by xbwu on 9/4/14.
 */
public enum IdType {
    ACCT_ID(0),
    TAG_ID(1),
    HANGOUT_ID(2);

    private int _value;

    IdType(int value) {
        _value = value;
    }

    int get_value() {
        return _value;
    }
}
