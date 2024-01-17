package org.reol.spring.redis.locks;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 首先，为了确保分布式锁可用，我们至少要确保锁的实现同时满足以下四个条件：
 * 互斥性。**在任意时刻，只有一个客户端能持有锁。
 * 不会发生死锁。**即使有一个客户端在持有锁的期间崩溃而没有主动解锁，也能保证后续其他客户端能加锁。
 * 具有容错性。**只要大部分的Redis节点正常运行，客户端就可以加锁和解锁。
 * 解铃还须系铃人。**加锁和解锁必须是同一个客户端，客户端自己不能把别人加的锁给解了。
 */
@Service
public class DistributedLocksSample {

    private static final Long UNLOCK_SUCCESS = 1L;
    @Resource
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 错误示范1
     * @param key
     * @param requestId
     * @return
     */
    public Boolean tryLockWrong1(String key,String requestId,int expireTime) {

        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, requestId);
        if (result) {
            // 若在这里程序突然崩溃，则无法设置过期时间，将发生死锁
            // 所以加锁操作必须是原子性的，可以使用上面的案例或者使用lua脚本
            redisTemplate.expire(key, expireTime,TimeUnit.SECONDS);
        }
        return result;
    }

    /**
     * 错误示范2
     * @param key
     * @param requestId
     * @return
     */
    public Boolean tryLockWrong2(String key,String requestId,int expireTime) {

        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, requestId);
        if (result) {
            // 若在这里程序突然崩溃，则无法设置过期时间，将发生死锁
            redisTemplate.expire(key, expireTime,TimeUnit.SECONDS);
        }
        return result;
    }

    /**
     * @problem:
     * 1.获取锁一次就返回false 无法重试
     * 2.无法重入，同一个线程无法多次获取同一把锁
     * 3.超时释放虽然可以避免死锁，但是如果业务耗时较长也会导致锁释放，存在安全隐患
     * 4.主从一致性问题：主从存在延迟，主宕机，如果从并未同步主的锁数据，可能出现锁失效
     * @param key 我们使用key来当锁，因为key是唯一的,一般key包含某个要锁住的资源的唯一编号 如lock:product_number
     * @param requestId key对应的value，我们传的是requestId，很多童鞋可能不明白，有key作为锁不就够了吗，为什么还要用到value？原因就是我们在上面讲到可靠性时，分布式锁要满足第四个条件解铃还须系铃人，通过给value赋值为requestId，我们就知道这把锁是哪个请求加的了，在解锁的时候就可以有依据。requestId可以使用线程id + UUID.randomUUID().toString()方法生成。
     * @param expireTime 代表key的过期时间，防止死锁
     * @return
     */
    public Boolean tryLock(String key,String requestId,int expireTime) {
        /**
         * setIfAbsent() 如果为空就set值，并返回 true；如果存在(不为空)不进行操作，并返回 false
         */
        return redisTemplate.opsForValue().setIfAbsent(key, requestId, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 错误解锁1，不管锁的拥有者直接解锁，导致任何客户端都可以随时进行解锁
     * @param key
     * @return
     */
    private void unlockWrong(String key) {
        redisTemplate.delete(key);
    }
    /**
     * 错误解锁2，
     * 如代码注释，问题在于如果调用delete()方法的时候，这把锁已经不属于当前客户端的时候会解除他人加的锁。那么是否真的有这种场景？
     * 答案是肯定的，比如客户端A加锁，一段时间之后客户端A解锁，
     * 在执行delete()之前，锁突然过期了，此时客户端B尝试加锁成功，然后客户端A再执行delete()方法，则将客户端B的锁给解除了。
     * @param key
     * @return
     */
    private void unlockWrong2(String key,String requestId) {
        // 判断加锁与解锁是不是同一个客户端
        if (requestId.equals(redisTemplate.opsForValue().get(key))) {
            // 若在此时，锁突然过期了，这把锁突然不是这个客户端的，则会误解锁
            redisTemplate.delete(key);
        }
    }

    public boolean unlock(String key,String requestId) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then " +
                        "return redis.call('del', KEYS[1]) " +
                "else " +
                        "return 0 end";

        RedisScript<Long> stringRedisScript = new DefaultRedisScript<>(script);

        Long result = redisTemplate.execute(stringRedisScript, Collections.singletonList(key), requestId);

        return UNLOCK_SUCCESS.equals(result);

    }

}
