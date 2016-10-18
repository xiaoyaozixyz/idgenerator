package com.xyz.rpcstubpool;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcStubPool<T>
{
	protected GenericObjectPool<T> internalPool;
	public AtomicInteger logId = new AtomicInteger(-1);
	public static AtomicInteger iNum = new AtomicInteger(0);
	private static Logger logger = LoggerFactory.getLogger(RpcStubPool.class);
	private AtomicBoolean isInit = new AtomicBoolean(false);

	/*
	 * 检测是否进行初始化
	 */
	private boolean isInit()
	{
		return isInit.get();
	}

	private String getClassName()
	{// 仅用于内部获取类的最短名字
		return "RpcStubPool";
	}

	/*
	 * 初始化
	 */
	public boolean init(long logIndex, final Config poolConfig, PoolableObjectFactory<T> factory)
	{
		isInit.set(true);
		String logFlag = getClassName() + ".init";
		if (this.internalPool != null)
		{
			try
			{
				destroy(logIndex);
			}
			catch (Exception e)
			{
				logger.warn("<lid:{}>[{}]resource is null! Exception detail:{}", logIndex, logFlag, e);
				return false;
			}
		}

		this.internalPool = new GenericObjectPool<T>(factory, poolConfig);
		return (this.internalPool != null) ? true : false;
	}

	/*
	 * 销毁对象池
	 */
	public void destroy(long logIndex)
	{
		String logFlag = getClassName() + ".destroy";
		if (!isInit())
		{
			logger.warn("<lid:{}>[{}]rpcpool is not init", logIndex, logFlag);
			return;
		}
		try
		{
			internalPool.close();
		}
		catch (Exception e)
		{
			logger.warn("<lid:{}>[{}]destory failed! Exception detail:{}", logIndex, logFlag, e);
		}
	}

	/*
	 * 从对象池借用对象
	 */
	public T getResource(long logIndex)
	{
		String logFlag = getClassName() + ".getResource";
		if (!isInit())
		{
			logger.warn("<lid:{}>[{}]rpcpool is not init", logIndex, logFlag);
			return null;
		}
		T res = null;
		try
		{
			res = internalPool.borrowObject();
			return res;
		}
		catch (Exception e)
		{
			logger.warn("<lid:{}>[{}]Could not get a resource from the pool! Exception detail:{}", logIndex, logFlag, e);
			returnBrokenResource(logIndex, res);
			return null;
		}
	}

	/*
	 * 归还异常的对象到对象池，对象池会直接销毁该对象
	 */
	public void returnBrokenResource(long logIndex, final T resource)
	{
		String logFlag = getClassName() + ".returnBrokenResource";
		if (!isInit())
		{
			logger.warn("<lid:{}>[{}]rpcpool is not init", logIndex, logFlag);
			return;
		}
		if (resource == null)
		{
			logger.warn("<lid:{}>[{}]resource is null!", logIndex, logFlag);
			return;
		}
		try
		{
			internalPool.invalidateObject(resource);
		}
		catch (Exception e)
		{
			logger.warn("<lid:{}>[{}]invalidateObject failed!  Exception detail:{}", logIndex, logFlag, e);
		}

	}

	/*
	 * 归还对象到对象池
	 */
	public void returnResource(long logIndex, final T resource)
	{
		String logFlag = getClassName() + ".returnResource";
		if (!isInit())
		{
			logger.warn("<lid:{}>[{}]rpcpool is not init", logIndex, logFlag);
			return;
		}
		if (resource == null)
		{
			logger.warn("<lid:{}>[{}]resource is null!", logIndex, logFlag);
			return;
		}
		try
		{
			internalPool.returnObject(resource);
		}
		catch (Exception e)
		{
			logger.warn("<lid:{}>[{}]returnObject failed!  Exception detail:{}", logIndex, logFlag, e);
		}
	}

}
