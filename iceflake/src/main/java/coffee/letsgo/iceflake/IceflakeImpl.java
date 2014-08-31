package coffee.letsgo.iceflake;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Created by xbwu on 8/29/14.
 */
public class IceflakeImpl implements Iceflake {

    private long datacenterId, workerId;

    private static long epoch = 1409436471455L;

    private long lastTimestamp;
    private int sequence;

    /*
     * bits: 1                    41                       5     5        12
     *      +-+-----------------------------------------+-----+-----+------------+
     *      |0|              milliseconds               | dc  | wkr |  sequence  |
     *      |0|              since epoch                | id  | id  |   number   |
     *      +-+-----------------------------------------+-----+-----+------------+
     *       ^
     *       |
     *       +-- msb
     */

    private static int
            sequenceBits = 12,
            workerIdBits = 5,
            datacenterIdBits = 5,
            maxWorkerId = ~(-1 << workerIdBits),
            maxDatacenterId = ~(-1 << datacenterIdBits),
            workerIdShift = sequenceBits,
            datacenterIdShift = sequenceBits + workerIdBits,
            timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits,
            sequenceMask = ~(-1 << sequenceBits);

    @Inject
    public IceflakeImpl(@Named("datacenter id") int datacenterId,
                        @Named("worker id") int workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format(
                    "datacenter id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.lastTimestamp = -1L;
        this.sequence = 0;
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
    public long getId(String useragent) throws org.apache.thrift.TException {
        if (!validUseragent(useragent)) {
            throw new InvalidUserAgentError();
        }

        return nextId();
    }

    @Override
    public long getDatacenterId() throws org.apache.thrift.TException {
        return datacenterId;
    }

    private synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new InvalidSystemClock();
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        return ((timestamp - epoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
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

    private boolean validUseragent(String useragent) {
        return useragent.matches("^[a-zA-Z][a-zA-Z-0-9]*$");
    }
}
