package com.example.cloudroute.service;

import com.example.cloudroute.util.RedisUtil;
import org.springframework.stereotype.Service;

@Service
public class RedisConnectionTestService {

    private final RedisUtil redisUtil;

    public RedisConnectionTestService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public String testRedisConnection() {
        try {
            // Redis에 임시 키-값 쌍 저장
            String testKey = "testKey";
            String testValue = "testValue";
            redisUtil.setDataExpire(testKey, testValue, 60);  // 60초 동안 유효

            // Redis에서 값 읽기
            String value = redisUtil.getData(testKey);

            // 값 확인
            if (testValue.equals(value)) {
                return "Redis connection test successful!";
            } else {
                return "Redis connection test failed. Value mismatch.";
            }
        } catch (Exception e) {
            // 예외 발생 시
            return "Redis connection test failed. Error: " + e.getMessage();
        }
    }
}
