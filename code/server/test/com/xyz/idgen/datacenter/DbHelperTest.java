package com.xyz.idgen.datacenter;

import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.xyz.idgen.common.DefaultValues;

public class DbHelperTest
{
	DbHelper dbHelper = new DbHelper();
	@Test
	public void test_init()
	{
		String host = "11.12.112.208";
		String dbName = "idgen2.0";
		String userName = "root";
		String pwd = "xdja123";
		String jdbcWriteUrl = "jdbc:mysql://" + host + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";
		try
		{
			assertTrue(dbHelper.init(jdbcWriteUrl, userName, pwd, DefaultValues.DB_MIN_POOL_SIZE, DefaultValues.DB_MAX_POOL_SIZE, DefaultValues.DB_MAX_IDLE_TIME, DefaultValues.DB_MAX_STATEMENTS, DefaultValues.DB_CHECKOUT_TIMEOUT));
		}
		catch (PropertyVetoException e)
		{
			assertTrue(false);
			e.printStackTrace();
		}
	}

	@Test
	public void test_getRedisInfoFromDb()
	{
		test_init();
		ConcurrentHashMap<String, RedisClient> res = dbHelper.getRedisInfoFromDb(1);
		assertTrue((res != null) &&(!res.isEmpty()));
		System.out.println("redis client information:");
		for(Map.Entry<String, RedisClient> anEntity : res.entrySet())
		{
			System.out.println(anEntity.getKey() + " : " + anEntity.getValue().getInfo());
		}
		
	}
	
	@Test
	public void test_getConfigInfoFromDB()
	{
		test_init();
		ConcurrentHashMap<String, ConfigInfo> res = dbHelper.getConfigInfoFromDB(1);
		assertTrue((res != null) &&(!res.isEmpty()));
		System.out.println("Config information:");
		for(Map.Entry<String, ConfigInfo> anEntity : res.entrySet())
		{
			System.out.println(anEntity.getKey() + " : " + anEntity.getValue().getInfo());
		}
	}
	@Test
	public void test_getAllIdInfoFromDB()
	{
		test_init();
		ConcurrentHashMap<String, IdInfo> res = dbHelper.getAllIdInfoFromDB(1, 1000);
		assertTrue((res != null) &&(!res.isEmpty()));
		System.out.println("id information:");
		for(Map.Entry<String, IdInfo> anEntity : res.entrySet())
		{
			System.out.println(anEntity.getKey() + " : " + anEntity.getValue().getInfo());
		}
	}
	
	@Test
	public void test_getOneIdSectionFromDB()
	{
		test_init();
		Map<String, String> res = dbHelper.getOneIdSectionFromDB(0, "msgid", 1000);
		assertTrue((res != null) &&(!res.isEmpty()));
		System.out.println("msgid information:");
		for(Map.Entry<String, String> anEntity : res.entrySet())
		{
			System.out.println(anEntity.getKey() + " : " + anEntity.getValue());
		}
	}
}
