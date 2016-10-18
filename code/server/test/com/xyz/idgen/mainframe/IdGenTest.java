package com.xyz.idgen.mainframe;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.ReturnValues;
import com.xyz.idgen.datacenter.DataCenterTest;
import com.xyz.idgen.thrift.stub.IdGenService;
import com.xyz.thrift.datatype.ResLong;

public class IdGenTest
{
	private static String getClassName()
	{
		return "IdGenTest";
	}
	private static long logIndex = DefaultValues.LOG_INDEX_TEST;
	private static Logger logger = LoggerFactory.getLogger(DataCenterTest.class);
	private static final String THRIFT_HOST = "127.0.0.1";
	private static final int THRIFT_PORT = 5800;
	private static final int SERVER_MODE_THREAD_POOL = 1;
	private static final int SERVER_MODE_NONBLOCK = 2;
	private static final int SERVER_MODE_THREADEDSELECTOR = 3;
	// private int serverMode = SERVER_MODE_THREADEDSELECTOR;

	TTransport m_transport = null;

	public IdGenService.Client getServiceClient(int serverMode)
	{
		TProtocol protocol = null;
		if (serverMode == SERVER_MODE_THREAD_POOL)
		{
			// System.err.println(m_transport = new TSocket(THRIFT_HOST,
			// THRIFT_PORT,5000);
			m_transport = new TSocket(THRIFT_HOST, THRIFT_PORT, 2000);
			protocol = new TBinaryProtocol(m_transport);
		}
		else if (serverMode == SERVER_MODE_NONBLOCK || serverMode == SERVER_MODE_THREADEDSELECTOR)
		{
			m_transport = new TFramedTransport(new TSocket(THRIFT_HOST, THRIFT_PORT, 2000));
			// 协议要和服务端一致
			protocol = new TBinaryProtocol(m_transport);
		}
		return new IdGenService.Client(protocol);
	}

	@Test
	public void test_getHostName()
	{
		System.out.println(Starter.getHostName(DefaultValues.LOG_INDEX_TEST));
	}
	
	@Test
	public void test_getIdByAllType()
	{
		String logFlag = getClassName() + ".test_getIdByAllType";
		boolean bSuc = true;
		try
		{
			IdGenService.Client client = getServiceClient(SERVER_MODE_THREADEDSELECTOR);
			m_transport.open();
			
			List<String> lIdTypes = new ArrayList<String>();
			lIdTypes.add("idg.userId");
			lIdTypes.add("idg.messageId");
			lIdTypes.add("idg.notificationId");
			lIdTypes.add("idg.logId");
			lIdTypes.add("idg.xx");
			lIdTypes.add("idg.yy");
			lIdTypes.add("idg.zz");
			
			for (String idType : lIdTypes)
			{
				ResLong res = client.getId("test", idType);
				if (res.res == ReturnValues.SUCCESS)
				{		
					logger.info("<lid:{}>[{}] {}:{}", logIndex, logFlag, idType, res.value);
				}
				else
				{
					logger.error("<lid:{}>[{}] {}:{}", logIndex, logFlag, idType, res.value);
					bSuc = false;					
				}				
			}//结束类型遍历		
		}
		catch (TException x)
		{
			x.printStackTrace();
			assertTrue(false);
		}
		finally 
		{
			m_transport.close();
		}
		assertTrue(bSuc);
	}

	@Test
	public void test_getId2()
	{
		try
		{
			IdGenService.Client client = getServiceClient(SERVER_MODE_THREADEDSELECTOR);
			int testNum = 10000 * 100;
			int i = testNum;
			long startTime = System.currentTimeMillis();
			m_transport.open();
			while (i > 0)
			{

				ResLong res = client.getId("test", "messageId");
				assertTrue(res.res == ReturnValues.SUCCESS);
				if (i % 100 == 0)
					System.out.println("id :" + res.value);

				i--;
			}
			m_transport.close();
			long endTime = System.currentTimeMillis();
			System.out.println("\nid number :" + testNum + "; time: " + (endTime - startTime) + "ms");

		}
		catch (TException x)
		{
			x.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void test_getIdMultiThread()
	{

		String idFlag = "messageId";

		TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen("thread1", idFlag, THRIFT_HOST, THRIFT_PORT);
		Thread t1 = new Thread(asynUpdateTask1);
		t1.start();
		TestThreadIdGen asynUpdateTask2 = new TestThreadIdGen("thread2", idFlag, THRIFT_HOST, THRIFT_PORT);
		Thread t2 = new Thread(asynUpdateTask2);
		t2.start();
		TestThreadIdGen asynUpdateTask3 = new TestThreadIdGen("thread3", idFlag, THRIFT_HOST, THRIFT_PORT);
		Thread t3 = new Thread(asynUpdateTask3);
		t3.start();
		TestThreadIdGen asynUpdateTask4 = new TestThreadIdGen("thread4", idFlag, THRIFT_HOST, THRIFT_PORT);
		Thread t4 = new Thread(asynUpdateTask4);
		t4.start();
		TestThreadIdGen asynUpdateTask5 = new TestThreadIdGen("thread5", idFlag, THRIFT_HOST, THRIFT_PORT);
		Thread t5 = new Thread(asynUpdateTask5);
		t5.start();
		TestThreadIdGen asynUpdateTask6 = new TestThreadIdGen("thread6", idFlag, THRIFT_HOST, THRIFT_PORT);
		Thread t6 = new Thread(asynUpdateTask6);
		t6.start();

		try
		{
			t1.join();
			t2.join();
			t3.join();
			t4.join();
			t5.join();
			t6.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test_getIdMultiThread2()
	{// 测试多线程获取同一个ID的情况
		String idFlag = "messageId";
		int threadNum = 50;

		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 1; i <= threadNum; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, THRIFT_HOST, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		try
		{
			for (Thread t : threadList)
			{
				t.join();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test_getId_MultiThread_multiId()
	{// 测试多线程获取同一个ID的情况
		int threadNum = 9;
		List<Thread> threadList = new ArrayList<Thread>();

		String idFlag = "messageId";
		int curStart = 1;
		int curEnd = (int) (threadNum * 0.3);
		for (int i = curStart; i <= curEnd; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, THRIFT_HOST, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		idFlag = "userId";
		curStart = curEnd + 1;
		curEnd = (int) (curStart + threadNum * 0.3);
		for (int i = curStart; i <= curEnd; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, THRIFT_HOST, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		idFlag = "notificationId";
		curStart = curEnd + 1;
		curEnd = (int) (curStart + threadNum * 0.3);
		for (int i = curStart; i <= curEnd; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, THRIFT_HOST, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		try
		{
			for (Thread t : threadList)
			{
				t.join();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test_getIdMultiThread_multiserver()
	{// 测试多线程获取同一个ID的情况
		String thrift_Host1 = "192.168.1.201";
		String thrift_Host2 = "192.168.1.202";
		String thrift_Host3 = "192.168.1.203";
		int threadNum = 50;
		List<Thread> threadList = new ArrayList<Thread>();

		String idFlag = "messageId";
		int curStart = 1;
		int curEnd = (int) (threadNum * 0.3);
		for (int i = curStart; i <= curEnd; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, thrift_Host1, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		idFlag = "userId";
		curStart = curEnd + 1;
		curEnd = (int) (curStart + threadNum * 0.3);
		for (int i = curStart; i <= curEnd; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, thrift_Host2, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		idFlag = "notificationId";
		curStart = curEnd + 1;
		curEnd = (int) (curStart + threadNum * 0.3);
		for (int i = curStart; i <= curEnd; i++)
		{
			String threadFlag = "thread" + i;
			TestThreadIdGen asynUpdateTask1 = new TestThreadIdGen(threadFlag, idFlag, thrift_Host3, THRIFT_PORT);
			Thread t = new Thread(asynUpdateTask1);
			t.start();
			threadList.add(t);
		}

		try
		{
			for (Thread t : threadList)
			{
				t.join();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	class TestThreadIdGen implements Runnable
	{
		private IdGenService.Client idGen = null;
		String idFlag;
		String threadFlag;
		TTransport transport = null;
		String thriftHost;
		int thriftPort;

		public TestThreadIdGen(String threadFlag, String idFlag, String thriftHost, int thriftPort)
		{
			this.threadFlag = threadFlag;
			this.idFlag = idFlag;
			this.thriftHost = thriftHost;
			this.thriftPort = thriftPort;
		}

		@Override
		public void run()
		{
			try
			{
				transport = new TFramedTransport(new TSocket(thriftHost, thriftPort, 2000));
				idGen = new IdGenService.Client(new TBinaryProtocol(transport));

				int outCounter = 10000;
				int i = 0;
				long lastStart = System.currentTimeMillis();
				transport.open();
				while (true)
				{
					i++;
					ResLong res;
					res = idGen.getId("test", "messageId");
					assertTrue(res.res == ReturnValues.SUCCESS);
					if (i % outCounter == 0)
					{
						long timeInteval = System.currentTimeMillis() - lastStart;
						System.out.println("caller: " + threadFlag + "; IdType: " + idFlag + "; outCounter: " + outCounter + "; time: " + timeInteval
								+ "; speed(number/s): " + (i * 1000 / timeInteval));
						lastStart = System.currentTimeMillis();
						i = 0;
					}
				}
			}
			catch (TException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				transport.close();
			}
		}

	}
}
