package com.xyz.idgen.datacenter;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.config.Config;

public class DataCenterTest
{
	private static String getClassName()
	{
		return "DataCenterTest";
	}
	private static long logIndex = DefaultValues.LOG_INDEX_TEST;
	private static Logger logger = LoggerFactory.getLogger(DataCenterTest.class);
	
	private static final String FILE_PATH = "conf/idgen.conf";
	private Config cfg = null;
	private DataCenter dc = null;
	private void loadCfg()
	{
		cfg = new Config();
		assertTrue(cfg.loadConfig(FILE_PATH));
	}
	
	@Test
	public void testInit()
	{
		loadCfg();
		dc = new DataCenter();
		assertTrue(dc.init(DefaultValues.LOG_INDEX_TEST, cfg));
		try
		{
			Thread.currentThread().join();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetId()
	{
		testInit();
		String test_key = "msgid";
		assertTrue(dc.getId(DefaultValues.LOG_INDEX_TEST, test_key) > 1);
	}
	
	@Test
	public void test_MultiGetId()
	{
		testInit();
		String test_key = "msgid";
		long lastId = 0;
		while(true)
		{
			long newId = dc.getId(DefaultValues.LOG_INDEX_TEST, test_key);
			System.out.println("newId: " + newId + ";lastId:" + lastId);
			if(newId<=lastId)
				System.out.println("***************************");
		}
	}
	
	@Test
	public void testGetStrId()
	{
		String logFlag = getClassName() + ".testGetStrId";
		testInit();
		boolean usePrefix = true;
		ConcurrentHashMap<String, IdInfo> idMaps = dc.getIdMaps();
		for (String s : idMaps.keySet())
		{
			String strId =  dc.getStrId(logIndex, s, usePrefix);
			logger.info("<lid:{}>[{}] strId:{}", logIndex, logFlag, strId);
			if (null == strId)
			{
				assertTrue(false);
				return;
			}			
		}
		
		usePrefix = false;
		for (String s : idMaps.keySet())
		{
			String strId =  dc.getStrId(logIndex, s, usePrefix);
			logger.info("<lid:{}>[{}] strId:{}", logIndex, logFlag, strId);
			if (null == strId)
			{
				assertTrue(false);
				return;
			}			
		}
		assertTrue(true);
	}
	
	@Test
	public void test_getStrId()
	{
		String logFlag = getClassName() + ".testGetStrId";
		testInit();
		boolean usePrefix = true;
		ConcurrentHashMap<String, IdInfo> idMaps = dc.getIdMaps();
		for (String s : idMaps.keySet())
		{
			String strId =  dc.getStrId(logIndex, s, usePrefix);
			logger.info("<lid:{}>[{}] strId:{}", logIndex, logFlag, strId);
			if (null == strId)
			{
				assertTrue(false);
				return;
			}			
		}
		
		usePrefix = false;
		for (String s : idMaps.keySet())
		{
			String strId =  dc.getStrId(logIndex, s, usePrefix);
			logger.info("<lid:{}>[{}] strId:{}", logIndex, logFlag, strId);
			if (null == strId)
			{
				assertTrue(false);
				return;
			}			
		}
		assertTrue(true);
	}
	
	@Test
	public void test_addNewId()
	{
		testInit();
		IdDBInfo idDbInfo =	IdDBInfo.createIdDbInfo();
		idDbInfo.setProject_name("auto-t1");
		idDbInfo.setId_name("auto-test-id");
		idDbInfo.setId_prifx("auto-t");
		idDbInfo.setId_start(1000);
		idDbInfo.setLast_range(11000);
		idDbInfo.setMax_range(20000000);
		idDbInfo.setMin_range(5600);
		idDbInfo.setLast_load_time(System.currentTimeMillis());
		idDbInfo.setApplicant("auto-test-jason");
		idDbInfo.setRedis_flag("11.12.112.203:6379");
		idDbInfo.setState(1);
		idDbInfo.setMin_load_interval(3600);
		idDbInfo.setLoad_percentage(0.9f);
		idDbInfo.setLock_expire(60);
		idDbInfo.setOther_info("nothing for this test id");
		assertTrue(dc.addNewId(0, idDbInfo.toString()));
	}
}
