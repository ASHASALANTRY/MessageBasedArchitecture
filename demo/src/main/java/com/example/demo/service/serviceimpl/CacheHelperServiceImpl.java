package com.example.demo.service.serviceimpl;

import com.example.demo.exception.CacheOperationException;
import com.example.demo.service.CacheHelperService;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheHelperServiceImpl implements CacheHelperService {
    @Value("${servicebus.queue.employeeprocess:employeeprocessqueue}")
    private String employeeProcessQueueName;
    private final StringRedisTemplate stringRedisTemplate;
    private static final Logger log = LoggerFactory.getLogger(CacheHelperServiceImpl.class);
    @Override
    public void addMessageStatusToCache(String key, String messageStatus) {
        try {
            ValueOperations<String, String> ops = this.stringRedisTemplate.opsForValue();
            if (!stringRedisTemplate.hasKey(key)) {
                ops.set(key, messageStatus);
                log.info("added key to redis: {}", key);
                log.info("value for key {} is: {}", key, ops.get(key));
            }
        }catch (RedisConnectionException exception){
            throw new CacheOperationException("Failed to connect to Redis server",exception);
        }catch (RedisException exception){
            throw new CacheOperationException("Failed to perform Redis operation",exception);
        }
    }

    @Override
    public String getDataFromCache(String key) {
        try{
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        return ops.get(key);
        }catch (RedisConnectionException exception) {
            throw new CacheOperationException("Failed to connect to Redis server", exception);
        }catch (RedisException exception){
            throw new CacheOperationException("Failed to access data from redis", exception);
        }
    }
}
