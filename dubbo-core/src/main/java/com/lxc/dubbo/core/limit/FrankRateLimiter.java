package com.lxc.dubbo.core.limit;

import java.util.concurrent.TimeUnit;

public class FrankRateLimiter {
    /**
     * 最大令牌数
     */
    private int maxPermits;

    /**
     * 下一次生产新令牌的时间
     */
//    private long nextNewPermitsTime = System.currentTimeMillis();
    private long nextNewPermitsTime =  System.nanoTime() / 1000;;

    /**
     * 剩余的令牌
     */
    private int storedPermits;

    /**
     * 每个令牌生产的时间
     */
    private int permitsProduceInterval;


    public FrankRateLimiter(int maxPermits) {
        this.maxPermits = maxPermits;
//        this.permitsProduceInterval = 1000 / maxPermits;
        this.permitsProduceInterval = 1000000 / maxPermits;

    }

    public boolean tryAcquire() {
//        long nowMills = System.currentTimeMillis();
        long nowMicros = System.nanoTime() / 1000;

//        if (!canAcquire(nowMicros, 0, TimeUnit.MILLISECONDS)) {
        if (!canAcquire(nowMicros, 0, TimeUnit.MICROSECONDS)) {
            return false;
        }
        acquire(1);
        return true;
    }

    public synchronized boolean tryAcquire(int permits, long timeout, TimeUnit timeUnit) {
//        long nowMills = System.currentTimeMillis();
        long nowMicros =  System.nanoTime() / 1000;

        if (!canAcquire(nowMicros, timeout, timeUnit)) {
            return false;
        }
        acquire(permits);
        return true;
    }

    /**
     * 判断是否可以获取
     * @param now
     * @param timeout
     * @param timeUnit
     * @return
     */
    private boolean canAcquire(long now, long timeout, TimeUnit timeUnit) {
//        return nextNewPermitsTime - now < timeUnit.toMillis(timeout);
        return nextNewPermitsTime - now < timeUnit.toMicros(timeout);
    }

    public long acquire(int permits) {
//        long nowMills = System.currentTimeMillis();
        long nowMicros =  System.nanoTime() / 1000;
        // 计算当前请求期望的执行时间
        long executeTime = calculateExpectedExecuteTime(permits, nowMicros);

        long waitTime = Math.max(executeTime - nowMicros, 0);
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime / 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return executeTime;
    }

    /**
     * 计算当前请求期望的执行时间
     *
     * @param permits
     * @param nowMicros
     * @return
     */
    public synchronized long calculateExpectedExecuteTime(int permits, long nowMicros) {
        long expectedExecuteTime = nextNewPermitsTime;
        // 如果当前时间 大于上个请求计算出来的 '下一个新令牌产生的时间'
        // 那么需要重新计算空余的令牌
        if (nowMicros > nextNewPermitsTime) {
            // 计算上一次更新空闲令牌，到现在总共生产了多少令牌
            storedPermits = (int) ((nowMicros - nextNewPermitsTime) / permitsProduceInterval);
            // 空闲令牌不会大于最大值
            storedPermits = Math.min(storedPermits, maxPermits);
            // 更新下一次新令牌生产的时间
            nextNewPermitsTime = nowMicros;
        }
        // 如果需要令牌数量大于当前空闲的令牌数量
        // 那么需要更新下一次新令牌生产的时间
        if (permits > storedPermits) {
            // 下一次生产新令牌的时间 + 需要额外的令牌数量 * 单个令牌生产的时间
            nextNewPermitsTime = nextNewPermitsTime + (long) (permits - storedPermits) * permitsProduceInterval;
        }

        // 更新空余令牌
        storedPermits = -permits;
        // 空闲令牌不允许小于0
        storedPermits = Math.max(storedPermits, 0);

        return expectedExecuteTime;

    }

    public Object getRate() {
        return maxPermits;
    }
}
