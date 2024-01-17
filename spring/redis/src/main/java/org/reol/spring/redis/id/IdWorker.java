package org.reol.spring.redis.id;

import org.springframework.stereotype.Component;

@Component
public class IdWorker {
    public long generateId(String keyPrefix) {
        return 1l;
    }
}
