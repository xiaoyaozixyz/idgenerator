package com.xyz.idgenclient.start;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgenclient.common.DefaultValues;
import com.xyz.idgenclient.common.ReturnValues;
import com.xyz.rpcstubpool.RpcClientInfo;
import com.xyz.rpcstubpool.RpcStubIdGenFactory;
import com.xyz.rpcstubpool.RpcStubPool;
import com.xyz.rpcstubpool.RpcStubPoolConfig;
import com.xyz.thrift.datatype.ResLong;

public class IDGClient
{
	private static Logger logger = LoggerFactory.getLogger(IDGClient.class);
	private RpcStubIdGenFactory rpcfactory = null;
	private RpcStubPool<RpcClientInfo<IdGenService.Client>> rpcstubpool = null;
	private RpcStubPoolConfig config = new RpcStubPoolConfig();
	private int timeout = 2000;
	private int serverMode = 3;
	private String sHost = "";
	private int sPort = -1;
	public static AtomicLong logIndex = new AtomicLong(0);
	public IDGClient(String serverHost, int serverPort)
	{
		this.sHost = serverHost;
		this.sPort = serverPort;
		config.setMaxActive(100);
		config.setMaxIdle(100);
		config.setMaxWait(3000);
		config.setTestWhileIdle(false);
		config.setMinEvictableIdleTimeMillis(3600000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(false);
	}
	public IDGClient(String serverHost, int serverPort, int serverMode,RpcStubPoolConfig config,int timeout)
	{
		this.timeout = timeout;
		this.config = config;
		this.serverMode = serverMode;
		this.sHost = serverHost;
		this.sPort = serverPort;
	}

	public boolean init()
	{
		String logFlag = "IDGClient.init";
		logger.info("[lid:{}] [{}] serverHost:{} serverPort:{} serverMode:{}", logIndex.getAndDecrement(), logFlag, this.sHost, this.sPort, this.serverMode);
		rpcfactory = new RpcStubIdGenFactory(this.sHost, this.sPort, this.serverMode, this.timeout);
		rpcstubpool = new RpcStubPool<>();
		if (!rpcstubpool.init(logIndex.get(), this.config, rpcfactory))
		{
			logger.error("[lid:{}] [{}]  rpcpool init failed!", logIndex, logFlag);
			return false;
		}
		return true;
	}

	public RpcClientInfo<IdGenService.Client> getServiceClient(long logIndex)
	{
		String logFlag = "IDGClient.getServiceClient";
		RpcClientInfo<IdGenService.Client> rpcconnect = null;
		try
		{
			rpcconnect = rpcstubpool.getResource(logIndex);
			return rpcconnect;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] exception happened! detail:\n{}", logIndex, logFlag, e);
			rpcstubpool.returnBrokenResource(logIndex, rpcconnect);
			return null;
		}
	}
	/**
	 * 根据消息类型获取ID
	 * @param idName  	id名称
	 * @param caller   调用者
	 * @return long 返回申请的ID,失败返回-1
	 */
	public long getId(String caller, String idName)
	{
		String logFlag = "IDGClient.getId";
		logger.debug("[lid:{}] [{}] idName:{}", logIndex, logFlag, idName);
		RpcClientInfo<IdGenService.Client> ci = null;
		try
		{
			ci = getServiceClient(logIndex.getAndDecrement());
			if (ci == null)
			{
				logger.error("[lid:{}] [{}] id:{} cann't get IDGS Client", logIndex, logFlag);
				return DefaultValues.ID_ERROR;
			}
			ResLong res = ci.getClient().getId(caller, idName);
			if (res.res == ReturnValues.SUCCESS)
				logger.debug("[lid:{}] [{}] id:{}", logIndex, logFlag, res.value);
			else
				logger.error("[lid:{}] [{}] cann't get id, result:{}", logIndex, logFlag, res.res);
			rpcstubpool.returnResource(logIndex.get(), ci);
			return res.value;
		}
		catch (TException e)
		{
			logger.error("[lid:{}] [{}] exception happened! detail:\n{}", logIndex, logFlag, e);
			rpcstubpool.returnBrokenResource(logIndex.get(), ci);
			return DefaultValues.ID_ERROR;
		}
	}
	
	public void shutDown()
	{
		String logFlag = "IDGClient.shutDown";
		if (rpcstubpool == null)
			return;
		try
		{
			rpcstubpool.destroy(logIndex.getAndDecrement());
			rpcstubpool = null;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}] [{}]exception happened while shutting down rpcstubpool...", logIndex.get(), logFlag);
		}
	}
}
