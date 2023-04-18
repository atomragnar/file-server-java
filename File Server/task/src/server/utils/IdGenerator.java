package server.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private static AtomicLong counter = new AtomicLong(0);

    public static long nextId() {
        return counter.incrementAndGet();
    }

}
