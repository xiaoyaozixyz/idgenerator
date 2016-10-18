package com.xyz.idgen.mainframe;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.common.Utils;
import com.xyz.idgen.config.Config;
import com.xyz.idgen.config.ConfigKeys;
import com.xyz.idgen.jmx.JMX;
import com.xyz.idgen.telnet.TelnetDaemon;
import com.xyz.idgen.thrift.stub.IdGenService;

public class Starter
{
	private static String getClassName()
	{
		return "Starter";
	}

	private static Logger logger = LoggerFactory.getLogger(Starter.class);
	private static final String CONFIG_FILE_PAHT = "conf/idgen.conf";
	private static final String TELNETD_CONFIG_FILE = "/com/xyz/idgen/telnet/telnetd.conf";

	/** Telnet 模块配置文件 */
	private static IdGen idGen = new IdGen();
	private static AsynIdGen asyncIdGen = new AsynIdGen();
	public static IdGen getIdGen()
	{
		return idGen;
	}

	private static Config cfg = null;

	public static String VERSION;
	private static int serverMode;
	private static String strServerMode = null;
	// Thrift server的三种工作模式：线程池，非阻塞，THREADEDSELECTOR
	private static final int SERVER_MODE_THREAD_POOL = 1;
	private static final int SERVER_MODE_NONBLOCKING = 2;
	private static final int SERVER_MODE_THREADEDSELECTOR = 3;
	private static final int SERVER_MODE_TASYN = 4;
	
	private static TServer server = null;
	private static int thriftListenPort;
	private static String serverInfo = null;
	public static String SERVICE_NAME;// 本服务的名字
	
	private static int telnetPort;

	private static int jmxWebPort;
	private static int jmxRmiPort;

	// 以多线程方式工作时的监听线程数和工作线程数
	private static int listerThreadNum;
	private static int workerThreadNum;

	public static void main(String[] args)
	{
		if (!init(DefaultValues.LOG_INDEX_INIT))
		{
			logger.error("<lid:{}>[{}] {} initialization failure...", DefaultValues.LOG_INDEX_INIT, "main", SERVICE_NAME);
			System.exit(0);
		}

		if (!start(DefaultValues.LOG_INDEX_INIT))
		{
			logger.error("<lid:{}>[{}] start service:{}fail...", DefaultValues.LOG_INDEX_INIT, "main", SERVICE_NAME);
			System.exit(0);
		}

	}// end of main()

	private static boolean start(long logIndex)
	{
		String logFlag = getClassName()+".start";
		serverInfo = new String("\n\nID Genertor start success! server information:");
		serverInfo += "\n\n 	**********************************************" 
		        + "\n 	   	---- " + SERVICE_NAME + " ----" 
				+ "\n 	   	Version: " + VERSION 
				+ "\n 	   	listen Port: " + thriftListenPort 
				+ "\n 	   	telnet Port: " + telnetPort 
				+ "\n 	   	jmx web Port: " + jmxWebPort
				+ "\n 	   	jmx rmi port: " + jmxRmiPort 
				+ "\n 	   	server mode: " + strServerMode 
				+ "\n 	**********************************************\n";
		logger.info(serverInfo);
		try
		{
			server.serve();
			return true;
		}
		catch (Exception e)
		{
			logger.error("<lid:{}>[{}] Server start fail! exception happened! detail:{}", logIndex, logFlag, e);
			System.exit(0);
		}
		return false;
	}

	private static boolean init(long logIndex)
	{
		String logFlag = getClassName() + ".init";
		VERSION = getVersion();

		if (!getConfig(logIndex))
		{
			logger.error("<lid:{}>[{}] get configure data fail!", logIndex, logFlag);
			return false;
		}
		
		if(serverMode == SERVER_MODE_TASYN)
		{
			if(!asyncIdGen.init(logIndex, cfg))
			{
				logger.error("<lid:{}>[{}] asyncIdGen init fail!", logIndex, logFlag);
				return false;
			}
		}else
		{
			if (!idGen.init(logIndex, cfg))
			{
				logger.error("<lid:{}>[{}] idGen init fail!", logIndex, logFlag);
				return false;
			}
		}
		
		setMetrics(logIndex);

		if (!startTelnet(logIndex))
		{
			logger.warn("<lid:{}>[{}] start telnet fail!", logIndex, logFlag);
		}

		try
		{
			createServer(logIndex);
		}
		catch (TTransportException ex)
		{
			logger.error("<lid:{}>[{}] exception happened!create thrift server fail!,detail:{}", logIndex, logFlag, ex);
			return false;
		}

		if (!JMX.init(DefaultValues.LOG_INDEX_INIT, cfg))
		{
			logger.error("<lid:{}>[{}] jmx init fail!", logIndex, logFlag);
			return false;
		}

		return true;
	}

	private static void createServer(long logIndex) throws TTransportException
	{
		String logFlag = getClassName() + ".createServer";
		TProcessor tProcessor = new IdGenService.Processor<IdGenService.Iface>(idGen);
		switch (serverMode)
		{
		case SERVER_MODE_NONBLOCKING:
		{
			strServerMode = "NONBLOCKING";
			TNonblockingServerSocket tnbSocketTransport = new TNonblockingServerSocket(thriftListenPort);
			TNonblockingServer.Args tnbArgs = new TNonblockingServer.Args(tnbSocketTransport);
			tnbArgs.maxReadBufferBytes = DefaultValues.THRIFT_MAX_READ_BUF;
			tnbArgs.processor(tProcessor);
			tnbArgs.transportFactory(new TFramedTransport.Factory());
			tnbArgs.protocolFactory(new TBinaryProtocol.Factory());
			// 使用非阻塞式IO，服务端和客户端需要指定TFramedTransport数据传输的方式
			server = new TNonblockingServer(tnbArgs);
			break;
		}
		case SERVER_MODE_THREAD_POOL:
		{
			strServerMode = "THREAD_POOL";
			TServerSocket serverTransport = new TServerSocket(thriftListenPort);
			TThreadPoolServer.Args threadPoolServerArgs = new TThreadPoolServer.Args(serverTransport);
			threadPoolServerArgs.processor(tProcessor);
			threadPoolServerArgs.maxWorkerThreads(workerThreadNum);
			threadPoolServerArgs.protocolFactory(new TBinaryProtocol.Factory());
			// 线程池服务模型，使用标准的阻塞式IO，预先创建一组线程处理请求。
			server = new TThreadPoolServer(threadPoolServerArgs);
			break;
		}
		case SERVER_MODE_TASYN:
		{
			strServerMode = "Asyn mode";
			IdGenService.AsyncProcessor asyncProcessor= new IdGenService.AsyncProcessor(asyncIdGen);
			TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(thriftListenPort);
			TTransportFactory transportFactory = new TFramedTransport.Factory();
			TThreadedSelectorServer.Args threadedSelectorServerArgs = new TThreadedSelectorServer.Args(serverTransport);
			threadedSelectorServerArgs.processor(asyncProcessor);
			threadedSelectorServerArgs.protocolFactory(new TBinaryProtocol.Factory());
			threadedSelectorServerArgs.transportFactory(transportFactory);
			threadedSelectorServerArgs.selectorThreads(listerThreadNum).workerThreads(workerThreadNum);
			// 线程池服务模型，使用标准的阻塞式IO，预先创建一组线程处理请求。
			server = new TThreadedSelectorServer(threadedSelectorServerArgs);
			break;
		}
		case SERVER_MODE_THREADEDSELECTOR:
		{
			strServerMode = "THREADED_SELECTOR";
			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(thriftListenPort);
			TTransportFactory transportFactory = new TFramedTransport.Factory();
			TThreadedSelectorServer.Args tArgs = new TThreadedSelectorServer.Args(serverTransport);
			tArgs.maxReadBufferBytes = DefaultValues.THRIFT_MAX_READ_BUF;
			tArgs.processor(tProcessor);
			tArgs.selectorThreads(listerThreadNum).workerThreads(workerThreadNum).transportFactory(transportFactory);
			server = new TThreadedSelectorServer(tArgs);
			break;
		}
		default:
			logger.error("<lid:{}>[{}] parameter for server mode fail..", logIndex, logFlag);
		}
	}

	/**
	 * 设置metrics相关信息
	 * */
	private static void setMetrics(long logIndex)
	{
		// 设置metrics的输出间隔
		int logGap = cfg.getInt("metrics.logGap");
		if (logGap <= 0)
			logGap = DefaultValues.METRICS_LOGGAP;
		MetricsTool.setLogGap(logGap);
		// 根据配置文件确定metrics是否进行输出
		if (cfg.getInt(ConfigKeys.METRICS_START) != MetricsTool.STOP)
			MetricsTool.start();
		else
			MetricsTool.stop();
	}

	private static boolean getConfig(long logIndex)
	{
		String logFlag = getClassName() + ".getConfig";
		cfg = new Config();
		if (!cfg.loadConfig(CONFIG_FILE_PAHT))
		{
			logger.error("<lid:{}>[{}] Loading configure data from file:{} fail!", logIndex, logFlag, CONFIG_FILE_PAHT);
			return false;
		}
		thriftListenPort = cfg.getInt(ConfigKeys.THRIFT_LISTEN_PORT);
		if (!Utils.checkPort(thriftListenPort))
		{
			logger.error("<lid:{}>[{}] thrift listen port error! port:{}", logIndex, logFlag, thriftListenPort);
			return false;
		}

		telnetPort = cfg.getInt(ConfigKeys.TELNET_PORT);
		if (!Utils.checkPort(telnetPort))
		{
			logger.error("<lid:{}>[{}] telnet port error! port:{}", logIndex, logFlag, telnetPort);
			return false;
		}

		SERVICE_NAME = cfg.getString(ConfigKeys.SERVICE_NAME);
		if (!Utils.checkString(SERVICE_NAME))
		{
			logger.error("<lid:{}>[{}] get service name error! key:{}", logIndex, logFlag, ConfigKeys.SERVICE_NAME);
			return false;
		}
		
		String hostName = getHostName(logIndex);
		if (!Utils.checkString(hostName))
		{
			logger.error("<lid:{}>[{}] get hostname fail!", logIndex, logFlag);
			return false;
		}
		SERVICE_NAME = SERVICE_NAME + "-" + hostName;
		
		jmxWebPort = cfg.getInt(ConfigKeys.JMX_WEB_PORT);
		jmxRmiPort = cfg.getInt(ConfigKeys.JMX_RMI_PORT);
		if (!Utils.checkPort(jmxWebPort) || !Utils.checkPort(jmxRmiPort))
		{
			logger.error("<lid:{}>[{}] jmx port error! web port:{} rmi port: {}", logIndex, logFlag, jmxWebPort, jmxRmiPort);
			return false;
		}

		listerThreadNum = cfg.getInt(ConfigKeys.THRIFT_THREAD_NUM_LISTEN);
		if (listerThreadNum <= 0)
			listerThreadNum = DefaultValues.THRIFT_THREAD_NUM_LISTENER;
		workerThreadNum = cfg.getInt(ConfigKeys.THRIFT_THREAD_NUM_WORKER);
		if (workerThreadNum <= 0)
			workerThreadNum = DefaultValues.THRIFT_THREAD_NUM_LISTENER;

		serverMode = cfg.getInt(ConfigKeys.THRIFT_SERVER_MODE);
		return true;
	}

	/**
	 * 获取内部版本号
	 * */
	private static String getVersion()
	{
		return Version.VERSION;
	}

	/**
	 * 启动 telnet 服务
	 * */
	private static boolean startTelnet(long logIndex)
	{

		String logFlag = getClassName() + ".startTelnet";
		try
		{
			TelnetDaemon.getInstance().startTelnetDaemon(telnetPort, Starter.class.getResourceAsStream(TELNETD_CONFIG_FILE));
			// TelnetDaemon.getInstance().startTelnetDaemon(telnetPort, new
			// FileInputStream(TELNETD_CONFIG_FILE));
		}
		catch (Exception e)
		{
			logger.warn("<lid:{}>[{}]Start telnet fail,telnetPort:{},detail:{}", logIndex, logFlag, telnetPort, e);
			return false;
		}
		return true;
	}

	public static void shutdown(long logIndex)
	{
		String logFlag = getClassName() + "shutdown";
		logger.warn("<lid:{}>[{}] Will Shutdown {}", logIndex, logFlag, SERVICE_NAME);
		try
		{
			idGen.shutdown(logIndex);
			TelnetDaemon.getInstance().stopTelnetDaemon();
		}
		catch (Exception ex)
		{
			logger.warn("<lid:{}>[{}] exception happened! detail {}", logIndex, logFlag, ex);
		}
		finally
		{
			System.exit(0);
		}
	}

	public static String getServerInfo(long logIndex)
	{
		return serverInfo;
	}

	/**
	 * 获取本机hostname
	 * @param logIndex
	 * @return 发生异常返回null
	 */
	public static String getHostName(long logIndex)
	{
		String logFlag = getClassName() + "getHostName";
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			logger.warn("<lid:{}>[{}] exception happened! detail {}", logIndex, logFlag, e);
			return null;
		}
	}

}
