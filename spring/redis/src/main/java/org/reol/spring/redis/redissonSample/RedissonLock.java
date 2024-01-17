package org.reol.spring.redis.redissonSample;


import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.redisson.RedissonBaseLock;
import org.redisson.RedissonLockEntry;
import org.redisson.api.RFuture;
import org.redisson.client.RedisException;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.pubsub.LockPubSub;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link java.util.concurrent.locks.Lock} 的分布式实现
 * 实现可重入锁
 * 如果客户端断开连接，锁定将自动删除。
 * 实施非公平锁定，因此不保证收购订单。
 * @author reol
 */
public class RedissonLock extends RedissonBaseLock {
    // 默认锁的释放时间 30s 定义来自Config文件中看门狗的时间定义 lockWatchdogTimeout = 30 * 1000;
    protected long internalLockLeaseTime;
    // 一个用于订阅redis锁释放的类
    protected final LockPubSub pubSub;

    final CommandAsyncExecutor commandExecutor;

    public RedissonLock(CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.commandExecutor = commandExecutor;
        this.internalLockLeaseTime = commandExecutor.getConnectionManager().getCfg().getLockWatchdogTimeout();
        this.pubSub = commandExecutor.getConnectionManager().getSubscribeService().getLockPubSub();
    }

    String getChannelName() {
        return prefixName("redisson_lock__channel", getRawName());
    }

    /**
     * 无参数lock，内部调用一个永久保存的
     */
    @Override
    public void lock() {
        try {
            lock(-1, null, false);
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
    }

    /**
     *
     * @param leaseTime 获取锁后持有锁的最长时间，
     * 如果尚未通过调用 unlock() 来释放它。
     * 如果 leaseTime 为 -1，则按住锁直到显式解锁。
     * @param unit 单位为时间单位
     */
    @Override
    public void lock(long leaseTime, TimeUnit unit) {
        try {
            lock(leaseTime, unit, false);
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {
        lock(-1, null, true);
    }

    @Override
    public void lockInterruptibly(long leaseTime, TimeUnit unit) throws InterruptedException {
        lock(leaseTime, unit, true);
    }

    /**
     *
     * @param leaseTime 锁多长时间 -1 表示使用看门狗自动续期，默认是30S钟
     * @param unit
     * @param interruptibly
     * @throws InterruptedException
     */
    private void lock(long leaseTime, TimeUnit unit, boolean interruptibly) throws InterruptedException {
        long threadId = Thread.currentThread().getId();
        Long ttl = tryAcquire(-1, leaseTime, unit, threadId);
        // lock acquired
        if (ttl == null) {
            return;
        }

        CompletableFuture<RedissonLockEntry> future = subscribe(threadId);
        pubSub.timeout(future);
        RedissonLockEntry entry;
        if (interruptibly) {
            entry = commandExecutor.getInterrupted(future);
        } else {
            entry = commandExecutor.get(future);
        }

        try {
            while (true) {
                ttl = tryAcquire(-1, leaseTime, unit, threadId);
                // lock acquired
                if (ttl == null) {
                    break;
                }

                // waiting for message
                if (ttl >= 0) {
                    try {
                        entry.getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        if (interruptibly) {
                            throw e;
                        }
                        entry.getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                    }
                } else {
                    if (interruptibly) {
                        entry.getLatch().acquire();
                    } else {
                        entry.getLatch().acquireUninterruptibly();
                    }
                }
            }
        } finally {
            unsubscribe(entry, threadId);
        }
//        get(lockAsync(leaseTime, unit));
    }

    private Long tryAcquire(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        return get(tryAcquireAsync(waitTime, leaseTime, unit, threadId));
    }

    private RFuture<Boolean> tryAcquireOnceAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
        RFuture<Boolean> acquiredFuture;

        if (leaseTime > 0) {
            // 异步执行
            acquiredFuture = tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_NULL_BOOLEAN);
        } else {
            acquiredFuture = tryLockInnerAsync(waitTime, internalLockLeaseTime,
                    TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_NULL_BOOLEAN);
        }

        CompletionStage<Boolean> f = acquiredFuture.thenApply(acquired -> {
            // lock acquired
            if (acquired) {
                if (leaseTime > 0) {
                    // 如果存活时间大于0 把返回的锁的剩余存活时间返回 给内部锁的存活时间
                    internalLockLeaseTime = unit.toMillis(leaseTime);
                } else {
                    scheduleExpirationRenewal(threadId);
                }
            }
            return acquired;
        });
        return new CompletableFutureWrapper<>(f);
    }

    private <T> RFuture<Long> tryAcquireAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {

        RFuture<Long> ttlRemainingFuture;
        if (leaseTime > 0) {
            // 锁的最大时间 大于0 走这个
            ttlRemainingFuture = tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
        } else {
            // 否则 走看门狗 自动续期
            ttlRemainingFuture = tryLockInnerAsync(waitTime, internalLockLeaseTime,
                    TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);
        }
        CompletionStage<Long> f = ttlRemainingFuture.thenApply(ttlRemaining -> {
            // lock acquired
            //ttlRemaining == null 说明加锁成功了
            if (ttlRemaining == null) {
                if (leaseTime > 0) {
                    // 如果是锁时间大于0 那么将锁内部的时间设置为传入的最大存活时间值
                    internalLockLeaseTime = unit.toMillis(leaseTime);
                } else {
                    // 否则就是走看门狗，下面是自动续期逻辑
                    scheduleExpirationRenewal(threadId);
                }
            }
            return ttlRemaining;
        });
        return new CompletableFutureWrapper<>(f);
    }

    @Override
    public boolean tryLock() {
        return get(tryLockAsync());
    }

    /**
     *  使用lua脚本获取锁
     * @param waitTime
     * @param leaseTime
     * @param unit
     * @param threadId
     * @param command
     * @return
     * @param <T>
     */
    <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
        return evalWriteAsync(getRawName(), LongCodec.INSTANCE, command,
                // 1 看看是否存在这个锁
                "if (redis.call('exists', KEYS[1]) == 0) then " +
                        // 1.1 如果结果等于0 说明不存在，那么 hincrby（这个命令如果不存在 则创建该key并增加）
                        // 为什么使用hash结构 因为除了要存锁的名称，锁的创建者（线程id+uuid）还要存锁了几次 可重入锁的概念
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                        // 1.2 设置锁的存活时间
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return nil; " +
                        "end; " +
                        // 2.1 如果结果等于1 说明该锁被当前线程已经获取过一次了，
                        // 2.2 第二次获取 等于可重入，所以要再计数一次
                        "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                        "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                        "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                        "return nil; " +
                        "end; " +
                        // 如果都不是 锁已经被别的线程获取了 那就返回该锁的剩余时间(pttl 命令以毫秒返回该key的剩余时间)
                        "return redis.call('pttl', KEYS[1]);",
                Collections.singletonList(getRawName()), unit.toMillis(leaseTime), getLockName(threadId));
    }

    /**
     *
     * @param waitTime 获取一个锁最长的等待时间
     * @param leaseTime 锁的存活时间
     * @param unit 时间单位
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        // 时间转成毫秒
        long time = unit.toMillis(waitTime);
        long current = System.currentTimeMillis();
        // 获取当前的线程ID
        long threadId = Thread.currentThread().getId();
        // 1 尝试获取锁
        Long ttl = tryAcquire(waitTime, leaseTime, unit, threadId);
        // 如果返回的是 null 则获取锁成功 可以见尝试获取锁的lua脚本内容

        if (ttl == null) {
            return true;
        }
        // 查看是否还有足够的等待时间
        time -= System.currentTimeMillis() - current;
        if (time <= 0) {
            acquireFailed(waitTime, unit, threadId);
            return false;
        }

        current = System.currentTimeMillis();
        // 订阅别人释放锁的信号 释放锁的时候 lua中有一个publish 发布了释放锁的信号
        CompletableFuture<RedissonLockEntry> subscribeFuture = subscribe(threadId);
        try {
            // 阻塞等待 等待时间为 剩余的等待时间
            subscribeFuture.get(time, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {

            if (!subscribeFuture.cancel(false)) {
                subscribeFuture.whenComplete((res, ex) -> {
                    if (ex == null) {
                        // 等待超时 就取消订阅
                        unsubscribe(res, threadId);
                    }
                });
            }
            acquireFailed(waitTime, unit, threadId);
            return false;
        } catch (ExecutionException e) {
            acquireFailed(waitTime, unit, threadId);
            return false;
        }

        try {
            // 如果在时间内等到了锁的释放信号 则继续尝试
            // 也就是上面的 subscribeFuture.get(time, TimeUnit.MILLISECONDS) 没有发生超时异常
            // 继续判断时间是否还有
            time -= System.currentTimeMillis() - current;
            if (time <= 0) {
                acquireFailed(waitTime, unit, threadId);
                return false;
            }

            while (true) {
                long currentTime = System.currentTimeMillis();
                ttl = tryAcquire(waitTime, leaseTime, unit, threadId);
                // lock acquired
                if (ttl == null) {
                    return true;
                }

                time -= System.currentTimeMillis() - currentTime;
                if (time <= 0) {
                    acquireFailed(waitTime, unit, threadId);
                    return false;
                }

                // waiting for message
                currentTime = System.currentTimeMillis();
                if (ttl >= 0 && ttl < time) {
                    //  等待锁释放 如果ttl小于time 那就等待ttl后 再尝试获取
                    commandExecutor.getNow(subscribeFuture).getLatch().tryAcquire(ttl, TimeUnit.MILLISECONDS);
                } else {
                    //  等待锁释放 如果ttl>time 那就等待time 后 再尝试获取 如果此时还没释放 那就没必要等了
                    commandExecutor.getNow(subscribeFuture).getLatch().tryAcquire(time, TimeUnit.MILLISECONDS);
                }
                // 第一种情况下 等待了 ttl秒 还没有获取到 此时再看看是否还有时间 再进行重试
                time -= System.currentTimeMillis() - currentTime;
                if (time <= 0) {
                    acquireFailed(waitTime, unit, threadId);
                    return false;
                }
            }
        } finally {
            unsubscribe(commandExecutor.getNow(subscribeFuture), threadId);
        }
//        return get(tryLockAsync(waitTime, leaseTime, unit));
    }

    protected CompletableFuture<RedissonLockEntry> subscribe(long threadId) {
        return pubSub.subscribe(getEntryName(), getChannelName());
    }

    protected void unsubscribe(RedissonLockEntry entry, long threadId) {
        pubSub.unsubscribe(entry, getEntryName(), getChannelName());
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        return tryLock(waitTime, -1, unit);
    }

    @Override
    public void unlock() {
        try {
            get(unlockAsync(Thread.currentThread().getId()));
        } catch (RedisException e) {
            if (e.getCause() instanceof IllegalMonitorStateException) {
                throw (IllegalMonitorStateException) e.getCause();
            } else {
                throw e;
            }
        }

//        Future<Void> future = unlockAsync();
//        future.awaitUninterruptibly();
//        if (future.isSuccess()) {
//            return;
//        }
//        if (future.cause() instanceof IllegalMonitorStateException) {
//            throw (IllegalMonitorStateException)future.cause();
//        }
//        throw commandExecutor.convertException(future);
    }

    @Override
    public boolean forceUnlock() {
        return get(forceUnlockAsync());
    }

    @Override
    public RFuture<Boolean> forceUnlockAsync() {
        cancelExpirationRenewal(null);
        return evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                "if (redis.call('del', KEYS[1]) == 1) then "
                        + "redis.call('publish', KEYS[2], ARGV[1]); "
                        + "return 1 "
                        + "else "
                        + "return 0 "
                        + "end",
                Arrays.asList(getRawName(), getChannelName()), LockPubSub.UNLOCK_MESSAGE);
    }

    protected RFuture<Boolean> unlockInnerAsync(long threadId) {
        return evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                // 如果当前用当前线程id匹配查询key 不存在 则直接返回
                "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
                        "return nil;" +
                        "end; " +
                        // 将当前锁的重载次数-1
                        "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
                        "if (counter > 0) then " +
                        // 如果将当前锁的重载次数-1后 还大于0 说明重载过 同时刷新剩余锁的时间
                        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                        "return 0; " +
                        "else " +
                        // 如果将当前锁的重载次数-1后 等于0 说明当前线程的可重入锁已经释放完了 那就完全解除占用
                        "redis.call('del', KEYS[1]); " +
                        // 解除占用后，发布一个消息 通知其他线程可以来获取锁了
                        "redis.call('publish', KEYS[2], ARGV[1]); " +
                        "return 1; " +
                        "end; " +
                        "return nil;",
                Arrays.asList(getRawName(), getChannelName()), LockPubSub.UNLOCK_MESSAGE, internalLockLeaseTime, getLockName(threadId));
    }

    @Override
    public RFuture<Void> lockAsync() {
        return lockAsync(-1, null);
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit) {
        long currentThreadId = Thread.currentThread().getId();
        return lockAsync(leaseTime, unit, currentThreadId);
    }

    @Override
    public RFuture<Void> lockAsync(long currentThreadId) {
        return lockAsync(-1, null, currentThreadId);
    }

    @Override
    public RFuture<Void> lockAsync(long leaseTime, TimeUnit unit, long currentThreadId) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        RFuture<Long> ttlFuture = tryAcquireAsync(-1, leaseTime, unit, currentThreadId);
        ttlFuture.whenComplete((ttl, e) -> {
            if (e != null) {
                result.completeExceptionally(e);
                return;
            }

            // lock acquired
            if (ttl == null) {
                if (!result.complete(null)) {
                    unlockAsync(currentThreadId);
                }
                return;
            }

            CompletableFuture<RedissonLockEntry> subscribeFuture = subscribe(currentThreadId);
            pubSub.timeout(subscribeFuture);
            subscribeFuture.whenComplete((res, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(ex);
                    return;
                }

                lockAsync(leaseTime, unit, res, result, currentThreadId);
            });
        });

        return new CompletableFutureWrapper<>(result);
    }

    private void lockAsync(long leaseTime, TimeUnit unit,
                           RedissonLockEntry entry, CompletableFuture<Void> result, long currentThreadId) {
        RFuture<Long> ttlFuture = tryAcquireAsync(-1, leaseTime, unit, currentThreadId);
        ttlFuture.whenComplete((ttl, e) -> {
            if (e != null) {
                unsubscribe(entry, currentThreadId);
                result.completeExceptionally(e);
                return;
            }

            // lock acquired
            if (ttl == null) {
                unsubscribe(entry, currentThreadId);
                if (!result.complete(null)) {
                    unlockAsync(currentThreadId);
                }
                return;
            }

            if (entry.getLatch().tryAcquire()) {
                lockAsync(leaseTime, unit, entry, result, currentThreadId);
            } else {
                // waiting for message
                AtomicReference<Timeout> futureRef = new AtomicReference<>();
                Runnable listener = () -> {
                    if (futureRef.get() != null) {
                        futureRef.get().cancel();
                    }
                    lockAsync(leaseTime, unit, entry, result, currentThreadId);
                };

                entry.addListener(listener);

                if (ttl >= 0) {
                    Timeout scheduledFuture = commandExecutor.getConnectionManager().newTimeout(timeout -> {
                        if (entry.removeListener(listener)) {
                            lockAsync(leaseTime, unit, entry, result, currentThreadId);
                        }
                    }, ttl, TimeUnit.MILLISECONDS);
                    futureRef.set(scheduledFuture);
                }
            }
        });
    }

    @Override
    public RFuture<Boolean> tryLockAsync() {
        return tryLockAsync(Thread.currentThread().getId());
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long threadId) {
        return tryAcquireOnceAsync(-1, -1, null, threadId);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, TimeUnit unit) {
        return tryLockAsync(waitTime, -1, unit);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit) {
        long currentThreadId = Thread.currentThread().getId();
        return tryLockAsync(waitTime, leaseTime, unit, currentThreadId);
    }

    @Override
    public RFuture<Boolean> tryLockAsync(long waitTime, long leaseTime, TimeUnit unit,
                                         long currentThreadId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        AtomicLong time = new AtomicLong(unit.toMillis(waitTime));
        long currentTime = System.currentTimeMillis();
        RFuture<Long> ttlFuture = tryAcquireAsync(waitTime, leaseTime, unit, currentThreadId);
        ttlFuture.whenComplete((ttl, e) -> {
            if (e != null) {
                result.completeExceptionally(e);
                return;
            }

            // lock acquired
            if (ttl == null) {
                if (!result.complete(true)) {
                    unlockAsync(currentThreadId);
                }
                return;
            }

            long el = System.currentTimeMillis() - currentTime;
            time.addAndGet(-el);

            if (time.get() <= 0) {
                trySuccessFalse(currentThreadId, result);
                return;
            }

            long current = System.currentTimeMillis();
            AtomicReference<Timeout> futureRef = new AtomicReference<Timeout>();
            CompletableFuture<RedissonLockEntry> subscribeFuture = subscribe(currentThreadId);
            pubSub.timeout(subscribeFuture, time.get());
            subscribeFuture.whenComplete((r, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(ex);
                    return;
                }

                if (futureRef.get() != null) {
                    futureRef.get().cancel();
                }

                long elapsed = System.currentTimeMillis() - current;
                time.addAndGet(-elapsed);

                tryLockAsync(time, waitTime, leaseTime, unit, r, result, currentThreadId);
            });
            if (!subscribeFuture.isDone()) {
                Timeout scheduledFuture = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
                    @Override
                    public void run(Timeout timeout) throws Exception {
                        if (!subscribeFuture.isDone()) {
                            subscribeFuture.cancel(false);
                            trySuccessFalse(currentThreadId, result);
                        }
                    }
                }, time.get(), TimeUnit.MILLISECONDS);
                futureRef.set(scheduledFuture);
            }
        });


        return new CompletableFutureWrapper<>(result);
    }

    private void tryLockAsync(AtomicLong time, long waitTime, long leaseTime, TimeUnit unit,
                              RedissonLockEntry entry, CompletableFuture<Boolean> result, long currentThreadId) {
        if (result.isDone()) {
            unsubscribe(entry, currentThreadId);
            return;
        }

        if (time.get() <= 0) {
            unsubscribe(entry, currentThreadId);
            trySuccessFalse(currentThreadId, result);
            return;
        }

        long curr = System.currentTimeMillis();
        RFuture<Long> ttlFuture = tryAcquireAsync(waitTime, leaseTime, unit, currentThreadId);
        ttlFuture.whenComplete((ttl, e) -> {
            if (e != null) {
                unsubscribe(entry, currentThreadId);
                result.completeExceptionally(e);
                return;
            }

            // lock acquired
            if (ttl == null) {
                unsubscribe(entry, currentThreadId);
                if (!result.complete(true)) {
                    unlockAsync(currentThreadId);
                }
                return;
            }

            long el = System.currentTimeMillis() - curr;
            time.addAndGet(-el);

            if (time.get() <= 0) {
                unsubscribe(entry, currentThreadId);
                trySuccessFalse(currentThreadId, result);
                return;
            }

            // waiting for message
            long current = System.currentTimeMillis();
            if (entry.getLatch().tryAcquire()) {
                tryLockAsync(time, waitTime, leaseTime, unit, entry, result, currentThreadId);
            } else {
                AtomicBoolean executed = new AtomicBoolean();
                AtomicReference<Timeout> futureRef = new AtomicReference<>();
                Runnable listener = () -> {
                    executed.set(true);
                    if (futureRef.get() != null) {
                        futureRef.get().cancel();
                    }

                    long elapsed = System.currentTimeMillis() - current;
                    time.addAndGet(-elapsed);

                    tryLockAsync(time, waitTime, leaseTime, unit, entry, result, currentThreadId);
                };
                entry.addListener(listener);

                long t = time.get();
                if (ttl >= 0 && ttl < time.get()) {
                    t = ttl;
                }
                if (!executed.get()) {
                    Timeout scheduledFuture = commandExecutor.getConnectionManager().newTimeout(timeout -> {
                        if (entry.removeListener(listener)) {
                            long elapsed = System.currentTimeMillis() - current;
                            time.addAndGet(-elapsed);

                            tryLockAsync(time, waitTime, leaseTime, unit, entry, result, currentThreadId);
                        }
                    }, t, TimeUnit.MILLISECONDS);
                    futureRef.set(scheduledFuture);
                }
            }
        });
    }


}
