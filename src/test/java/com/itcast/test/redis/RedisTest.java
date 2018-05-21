package com.itcast.test.redis;

import org.junit.Test;

import redis.clients.jedis.Jedis;

public class RedisTest {

	@Test
	public void f1(){
		Jedis jedis = new Jedis("localhost");
		jedis.setex("name", 10, "周杰伦");
		System.err.println(jedis.get("name"));
	}
	
	
}
