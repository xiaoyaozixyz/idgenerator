package com.xyz.rpcstubpool;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class RpcClientInfo<T>
{
	private TTransport transport;
	private T client;
	private TSocket tsocket;

	public RpcClientInfo(T client, TTransport transport, TSocket tsocket)
	{
		this.client = client;
		this.transport = transport;
		this.tsocket = tsocket;
	}

	public TTransport getTTransport()
	{
		return transport;
	}

	public T getClient()
	{
		return client;
	}
	public TSocket getTSocket()
	{
		return tsocket;
	}
}
