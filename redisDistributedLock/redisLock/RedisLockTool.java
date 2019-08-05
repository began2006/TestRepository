package com.example.logindemo.redisLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;
import java.util.UUID;

/**
 * @description: redis分布式锁
 * @author: gk
 * @date: 2019/8/5 11:24
 * @since: jdk1.8
 *
 * 使用案例
 *      public List<CustomerInfo> selectById(Integer id) {
 *
 *         RedisLock redisLock = redisTool.getLock("selectById_" + id, 0);
 *
 *         List<CustomerInfo> customerInfoList = customerInfoDao.selectById(id);
 *
 *         redisLock.unlock();
 *
 *         return customerInfoList;
 *     }
 */

@Component
public class RedisLockTool {

    private static final String LOCK_PREFIX = "lock:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public RedisLock getLock(String key, int expireTime) {

        String value = UUID.randomUUID().toString();
        final String keyT = LOCK_PREFIX + key;
        System.out.println("--- uuid:" + value);

        boolean isSuccess;

        if (expireTime > 0) {
            String status = stringRedisTemplate.execute(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    Object nativeConnection = redisConnection.getNativeConnection();
                    String status = "";
                    // 集群
                    if (nativeConnection instanceof JedisCluster) {
                        status = (String) ((JedisCluster) nativeConnection).set(keyT, value, "NX", "PX", expireTime);
                    }
                    // 单点
                    if (nativeConnection instanceof Jedis) {
                        status = (String) ((Jedis) nativeConnection).set(keyT, value, "NX", "PX", expireTime);
                    }
                    return status;
                }
            });
            isSuccess = "OK".equals(status);
        } else {
            isSuccess = stringRedisTemplate.opsForValue().setIfAbsent(keyT, value);
        }

        if (isSuccess) {
            return new RedisLockInner(keyT, value);
        } else {
            return null;
        }
    }

    private class RedisLockInner implements RedisLock {

        private String key;
        private String expectValue;

        public RedisLockInner(String key, String expectValue) {
            this.key = key;
            this.expectValue = expectValue;
        }

        @Override
        public void unlock() {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Object result = stringRedisTemplate.execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    Object nativeConnection = redisConnection.getNativeConnection();
                    Object result = "";
                    System.out.println("==== " + nativeConnection.getClass());
                    // 集群
                    if (nativeConnection instanceof JedisCluster) {
                        result = ((JedisCluster) nativeConnection).eval(script, Collections.singletonList(key), Collections.singletonList(expectValue));
                    }
                    // 单点
                    if (nativeConnection instanceof Jedis) {
                        result = ((Jedis) nativeConnection).eval(script, Collections.singletonList(key), Collections.singletonList(expectValue));
                    }

                    /*if (nativeConnection instanceof RedisClusterAsyncCommands) {
                        System.out.println("------5----- key = " + key);
                        result = ((RedisClusterAsyncCommands) nativeConnection).del(key);
                    }*/

                    return result;
                }
            });
        }
    }
}
