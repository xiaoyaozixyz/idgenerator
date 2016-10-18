package com.xyz.rpcstubpool;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgenclient.common.DefaultValues;
import com.xyz.idgenclient.common.ReturnValues;
import com.xyz.idgenclient.start.IdGenService;
import com.xyz.thrift.datatype.ResStr;

//业务处理对象，创建对象，验证对象
public class RpcStubIdGenFactory extends BasePoolableObjectFactory<RpcClientInfo<IdGenService.Client>>
{
	private final String host;
	private final int port;
	private final int serverMode;
	private final int timeout;
	public AtomicInteger logId = new AtomicInteger(-1);
	private static Logger logger = LoggerFactory.getLogger(RpcStubIdGenFactory.class);

	private String getClassName()
	{// 仅用于内部获取类的最短名字
		return "RpcStubIdGenFactory";
	}

	public RpcStubIdGenFactory(final String host, final int port, final int mode, final int timeout)
	{
		super();
		this.host = host;
		this.port = port;
		this.serverMode = mode;
		this.timeout = timeout;
	}

	@Override
	public RpcClientInfo<IdGenService.Client> makeObject() throws Exception
	{
		String logFlag = getClassName() + ".makeObject";
		TTransport transport = null;
		TProtocol protocol = null;
		logId.getAndDecrement();
		TSocket tsocket = null;
		if (serverMode == DefaultValues.SERVER_MODE_THREAD_POOL)
		{
			tsocket = new TSocket(this.host, this.port, this.timeout);
			transport = tsocket;
			protocol = new TBinaryProtocol(transport);
		}
		else if (serverMode == DefaultValues.SERVER_MODE_NONBLOCK || serverMode == DefaultValues.SERVER_MODE_THREADEDSELECTOR)
		{
			tsocket = new TSocket(this.host, this.port, this.timeout);
			transport = new TFramedTransport(tsocket);
			// 协议要和服务端一致
			protocol = new TBinaryProtocol(transport);
		}
		RpcClientInfo<IdGenService.Client> rpctype = new RpcClientInfo<IdGenService.Client>(new IdGenService.Client(protocol), transport, tsocket);
		try
		{
			rpctype.getTTransport().open();
			RpcStubPool.iNum.incrementAndGet();
			return rpctype;
		}
		catch (TTransportException e)
		{
			logger.warn("<lid:{}>[{}]getTTransport failed! TTransportException detail:{}", logId.get(), logFlag, e);
			return null;
		}
	}

	@Override
	public void destroyObject(RpcClientInfo<IdGenService.Client> obj) throws Exception
	{
		String logFlag = getClassName() + ".destroyObject";
		if (obj == null)
		{
			logger.warn("<lid:{}>[{}]Object is empty!", logId.getAndDecrement(), logFlag);
			return;
		}
		RpcClientInfo<IdGenService.Client> rpctype = obj;
		rpctype.getTTransport().close();
		RpcStubPool.iNum.decrementAndGet();
	}

	@Override
	public boolean validateObject(RpcClientInfo<IdGenService.Client> obj)
	{
		String logFlag = getClassName() + ".validateObject";
		logId.getAndDecrement();
		if (obj == null)
		{
			logger.warn("<lid:{}>[{}]Object is empty!", logId.get(), logFlag);
			return false;
		}
		try
		{
			RpcClientInfo<IdGenService.Client> rpctype = obj;
			if (!checkSocket(rpctype.getTSocket().getSocket()))
			{
				logger.warn("<lid:{}>[{}] checkSocket fail!", logId.get(), logFlag);
				return false;
			}
			ResStr resStr = rpctype.getClient().echo(logFlag, "OK");
			if (resStr.res != ReturnValues.SUCCESS)
			{
				logger.warn("<lid:{}>[{}] err result!", logId.get(), logFlag);
				return false;
			}
			return resStr.value.equals("OK");
		}
		catch (Exception e)
		{
			logger.warn("<lid:{}>[{}]validateObject fail! Exception detail:{}", logId.get(), logFlag, e);
			return false;
		}
	}
	public boolean checkSocket(Socket socket)
	{
		return socket != null && socket.isBound() && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
	}
}
