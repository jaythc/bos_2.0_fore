package com.itcast.test.redis;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext.xml")
public class SpringDataRedisTest {

	//注入redis的模板
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Test
	public void testRedis(){
		redisTemplate.opsForValue().set("city", "武汉",10,TimeUnit.MINUTES);
		System.err.println(redisTemplate.opsForValue().get("city"));
	}
	
}
