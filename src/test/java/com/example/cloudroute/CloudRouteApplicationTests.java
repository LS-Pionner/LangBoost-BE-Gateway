package com.example.cloudroute;

import com.example.cloudroute.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CloudRouteApplicationTests {

	@Autowired
	private RedisUtil redisUtil;  // RedisUtil을 주입받아 사용

	@Test
	void contextLoads() {
	}

	@Test
	void testRedisConnection() {
		// Redis에 데이터를 넣고 가져오는 테스트
		String testKey = "myKey";
		String testValue = "testValue";

		// Redis에 데이터를 저장 (60초 동안 유효)
		redisUtil.setDataExpire(testKey, testValue, 60);

		// Redis에서 값을 가져와서 확인
		String value = redisUtil.getData(testKey);

		// 테스트: Redis에서 값을 제대로 가져왔는지 확인
		assertEquals(testValue, value);
	}
}
