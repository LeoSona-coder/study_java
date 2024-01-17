package org.reol.spring.redis.redissonSample;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 *  redisson的入门案例
 */
@Service
public class LockSample {
    @Resource
    private RedissonClient redissonClient;

    public void testLock() {
        RLock lock = redissonClient.getLock("lock:" + "product_100");
        try {
            boolean isLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (isLock) {
                System.out.println("执行业务！");
            }
        } catch (InterruptedException e) {
            lock.unlock(); // 释放锁

        }
    }
}
