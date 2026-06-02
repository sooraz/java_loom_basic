package com.yugi.loom.ratelimit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisRateLimiter {
    private final JedisPool jedisPool;
    private final int capacity; // max tokens
    private final double refillRate; // tokens per second

    // Lua script - atomic ga check + decrement
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refill_rate = tonumber(ARGV[2])
        local now = tonumber(ARGV[3])
        local requested = tonumber(ARGV[4])

        local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
        local tokens = tonumber(bucket[1])
        local last_refill = tonumber(bucket[2])

        if tokens == nil then
            tokens = capacity
            last_refill = now
        end

        local delta = math.max(0, now - last_refill)
        tokens = math.min(capacity, tokens + delta * refill_rate)

        local allowed = 0
        if tokens >= requested then
            allowed = 1
            tokens = tokens - requested
        end

        redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
        redis.call('EXPIRE', key, 60)

        return {allowed, tokens}
        """;

    public RedisRateLimiter(JedisPool pool, int capacity, double refillRate) {
        this.jedisPool = pool;
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    public RateLimitResult tryConsume(String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            long now = System.currentTimeMillis() / 1000;
            String key = "rate_limit:user:" + userId;
            System.out.println("sooraz before lau script");
            var result = (java.util.List<Long>) jedis.eval(
                LUA_SCRIPT,
                1,
                key,
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(now),
                "1" // 1 request consume
            );
            if(result.isEmpty())
            	return new RateLimitResult(false, 0);

            boolean allowed = result.get(0) == 1;
            long remaining = result.get(1);

            return new RateLimitResult(allowed, remaining);
        }
    }

    public record RateLimitResult(boolean allowed, long remainingTokens) {}
}
