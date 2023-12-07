package org.reol.spring.redis.service;

import org.reol.spring.redis.locks.DistributedLocksSample;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service
public class SaleService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    // 一个简单的分布式锁
    @Resource
    private DistributedLocksSample distributedLocksSample;

    private static final String CONSTANT_LOCK = "lock:";

    /**
     * 设置某个产品的库存
     *
     * @param productId 产品id
     * @param count     产品库存
     */
    public void setStore(String productId, int count) {
        redisTemplate.opsForValue().set(productId, count);
    }

    public void sale(String productId) {
        String requestId = UUID.randomUUID().toString();
        try {
            Boolean result = false;
            while (!result) { // 阻塞直到获取锁
                result = distributedLocksSample.tryLock(CONSTANT_LOCK + productId, requestId, 30);
            }

            Long count = redisTemplate.opsForValue().decrement(productId);
            System.out.println("售出一件您的产品，当前产品剩余" + count);

        }finally {
            distributedLocksSample.unlock(CONSTANT_LOCK + productId, requestId);
        }



    }

}
