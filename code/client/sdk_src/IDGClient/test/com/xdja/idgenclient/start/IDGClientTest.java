package com.xdja.idgenclient.start;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgenclient.start.IDGClient;

public class IDGClientTest
{
	private static Logger logger = LoggerFactory.getLogger(IDGClient.class);
	private static final String THRIFT_HOST = "11.12.112.201";
	private static final int THRIFT_PORT = 5900;
	int serverMode = 3;
	private long logIndex = -1;
	IDGClient idgclient = null;
	public boolean init()
	{
		String logFlag = "IDGClient.init";
		logger.debug("<lid:{}>[{}] serverHost:{} serverPort:{} serverMode:{}", -1, logFlag, THRIFT_HOST, THRIFT_PORT, serverMode);
		idgclient = new IDGClient(THRIFT_HOST, THRIFT_PORT);
		if (!idgclient.init())
		{
			return false;
		}
		
		return true;
	}
	@Test
	public void test_init()
	{
		String logFlag = "IDGClient.test_init";
		logger.debug("<lid:{}>[{}] serverHost:{} serverPort:{} serverMode:{}", -1, logFlag, THRIFT_HOST, THRIFT_PORT, serverMode);

		IDGClient idgclient = new IDGClient(THRIFT_HOST, THRIFT_PORT);
		assertTrue(idgclient.init()==true);
	}
	@Test
	public void test_getId()
	{
		String logFlag = "PMClient.test_getserver";
		if (!init())
		{
			logger.warn("<lid:{}>[{}] init fail!", logIndex, logFlag);
		}
		long newId = idgclient.getId("test_getId()", "log");
		System.out.println("Id: " + newId);
		assertTrue(newId > 0);
	}

}
