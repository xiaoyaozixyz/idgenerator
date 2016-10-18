package com.xyz.idgen.datacenter;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.config.Config;

public class RedisClientTest
{
	private static final String FILE_PATH = "conf/idgen.conf";
	private Config cfg = null;	
	private ConcurrentHashMap<String, RedisClient> rcMap = new ConcurrentHashMap<String, RedisClient>();//根据ip:port查redis对象
	
	private void loadCfg()
	{
		cfg = new Config();
		assertTrue(cfg.loadConfig(FILE_PATH));
	}
	
	@Test
	public void test_Init()
	{
		loadCfg();
		assertTrue(true);		
	}


	@Test
	public void test_GetId()
	{
		test_Init();
		String test_key = "test_key";
		RedisClient redisClient = rcMap.elements().nextElement();		
		redisClient.del(DefaultValues.LOG_INDEX_TEST, test_key);
		assertTrue(redisClient.incr(DefaultValues.LOG_INDEX_TEST, test_key) == 1);
		assertTrue(redisClient.incr(DefaultValues.LOG_INDEX_TEST, test_key) == 2);
		redisClient.del(DefaultValues.LOG_INDEX_TEST, test_key);
	}
	
	@Test
	public void test_DelId()
	{
		test_Init();
		String test_key = "test_key";
		RedisClient redisClient = rcMap.elements().nextElement();	
		assertTrue(redisClient.del(DefaultValues.LOG_INDEX_TEST, test_key));
	}
}
