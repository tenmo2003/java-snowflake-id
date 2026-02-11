package com.tenmo2003.snowflakeid;

/**
 * A distributed ID generator based on Twitter's Snowflake ID.
 * Specification: https://en.wikipedia.org/wiki/Snowflake_ID
 *
 * @author anhvn
 * @since 2026-02-10
 */
public class SnowflakeIdGenerator {

    private static final int SIGN_BIT = 1;
    private static final int TIMESTAMP_BIT = 41;
    private static final int MACHINE_ID_BIT = 10;
    private static final int SEQUENCE_BIT = 12;

    private static final int MAX_MACHINE_ID = (1 << MACHINE_ID_BIT) - 1;
    private static final int MAX_SEQUENCE = (1 << SEQUENCE_BIT) - 1;

    private final int machineId;
    private final long chosenEpoch;

    private int sequence = 0;
    private long lastTimestamp = 0;

    public SnowflakeIdGenerator(int machineId, long chosenEpoch) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("machineId must be between 0 and " + MAX_MACHINE_ID);
        }
        if (chosenEpoch < 0) {
            throw new IllegalArgumentException("chosenEpoch must be greater than 0");
        }
        this.machineId = machineId;
        this.chosenEpoch = chosenEpoch;
    }

    public SnowflakeIdGenerator(long chosenEpoch) {
        if (chosenEpoch < 0) {
            throw new IllegalArgumentException("chosenEpoch must be greater than 0");
        }

        this.machineId = WorkerIdHelper.getMachineIdBasedOnMAC(MAX_MACHINE_ID);
        this.chosenEpoch = chosenEpoch;
    }

    public synchronized long generateId() {
        long timestamp = getCurrentTimestamp();
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitTillNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        return (timestamp << (MACHINE_ID_BIT + SEQUENCE_BIT))
            | (machineId << SEQUENCE_BIT)
            | sequence;
    }

    private long waitTillNextMillis(long lastTimestamp) {
        while (true) {
            Thread.yield();
            long timestamp = getCurrentTimestamp();
            if (timestamp != lastTimestamp) {
                return timestamp;
            }
        }
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis() - chosenEpoch;
    }
}
