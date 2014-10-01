package coffee.letsgo.iceflake.server;

import coffee.letsgo.iceflake.Iceflake;
import coffee.letsgo.iceflake.IdType;
import coffee.letsgo.iceflake.InvalidIdTypeError;
import coffee.letsgo.iceflake.InvalidSystemClock;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Created by xbwu on 8/29/14.
 */
public class IceflakeImpl implements Iceflake {

    private long workerId;
    private long[] sequences;
    private long[] lastTimestamps;

    private static long epoch = 1409436471455L;

    /*
     * bits: 1    7                        40                      6       10
     *      +-+-------+----------------------------------------+------+----------+
     *      |0|  id   |               milliseconds             |worker| sequence |
     *      |0| type  |               since epoch              |  id  |  number  |
     *      +-+-------|----------------------------------------+------+----------+
     *       ^
     *       |
     *       +-- msb
     *
     * supports 128 id types
     * 64 id generating servers at most
     * each id generating server generates 1024 ids at most per millisecond for each id type
     * id would be exhausted after 34 years
     */

    private static int
            sequenceBits = 10,
            workerIdBits = 6,
            timeStampBits = 40,
            idTypeBits = 7,
            workerIdShift = sequenceBits,
            timestampLeftShift = sequenceBits + workerIdBits,
            idTypeShift = sequenceBits + workerIdBits + timeStampBits;

    private static long
            maxWorkerId = ~(-1 << workerIdBits),
            sequenceMask = ~(-1 << sequenceBits),
            maxIdType = ~(-1 << idTypeBits);

    private Object[] locks;

    @Inject
    public IceflakeImpl(@Named("worker id") int workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker id can't be greater than %d or less than 0", maxWorkerId));
        }
        this.workerId = workerId;
        this.lastTimestamps = new long[(int) maxIdType];
        this.sequences = new long[(int) maxIdType];
        this.locks = new Object[(int) maxIdType];
        for(int i = 0; i < maxIdType; ++i) {
            lastTimestamps[i] = -1L;
            sequences[i] = 0L;
            locks[i] = new Object();
        }
    }

    @Override
    public long getWorkerId() throws org.apache.thrift.TException {
        return workerId;
    }

    @Override
    public long getTimestamp() throws org.apache.thrift.TException {
        return System.currentTimeMillis();
    }

    @Override
    public long getId(final IdType type) throws org.apache.thrift.TException {
        if (type.getValue() < 0 || type.getValue() > maxIdType) {
            throw new InvalidIdTypeError();
        }
        return nextId(type.getValue());
    }

    private long nextId(int type) {
        synchronized (locks[type]) {

            long timestamp = timeGen();

            if (timestamp < lastTimestamps[type]) {
                throw new InvalidSystemClock();
            }

            if (lastTimestamps[type] == timestamp) {
                sequences[type] = (sequences[type] + 1) & sequenceMask;
                if (sequences[type] == 0) {
                    timestamp = tilNextMillis(lastTimestamps[type]);
                }
            } else {
                sequences[type] = 0;
            }

            lastTimestamps[type] = timestamp;
            return ((long) type << idTypeShift |
                    (timestamp - epoch) << timestampLeftShift) |
                    (workerId << workerIdShift) |
                    sequences[type];
        }
    }

    private long tilNextMillis(long lastTimestamp) {

        long timestamp;
        do {
            timestamp = timeGen();
        } while (timestamp <= lastTimestamp);
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
