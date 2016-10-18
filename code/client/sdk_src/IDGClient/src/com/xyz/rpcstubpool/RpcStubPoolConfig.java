package com.xyz.rpcstubpool;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

//连接池配置
public class RpcStubPoolConfig extends Config
{
	public RpcStubPoolConfig()
	{
		// defaults to make your life with connection pool easier :)
		setTestWhileIdle(true);// 向调用者输出“链接”对象时，是否检测它的空闲超时；默认为false。如果“链接”空闲超时，将会被移除。建议保持默认值
		setMinEvictableIdleTimeMillis(5000);// 连接空闲的最小时间，达到此值后空闲连接将可能会被移除。负值(-1)表示不移除
		setTimeBetweenEvictionRunsMillis(30000);// “空闲链接”检测线程，检测的周期，毫秒数。如果为负值，表示不运行“检测线程”。默认为-1.
		setNumTestsPerEvictionRun(-1);// 对于“空闲链接”检测线程而言，每次检测的链接资源的个数。-1表示检测ceil(_pool.size())/abs(numTestsPerEvictionRun)个链接，默认为3.
	}

	public int getMaxIdle()
	{
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle)
	{
		this.maxIdle = maxIdle;
	}

	public int getMinIdle()
	{
		return minIdle;
	}

	public void setMinIdle(int minIdle)
	{
		this.minIdle = minIdle;
	}

	public int getMaxActive()
	{
		return maxActive;
	}

	public void setMaxActive(int maxActive)
	{
		this.maxActive = maxActive;
	}

	public long getMaxWait()
	{
		return maxWait;
	}

	public void setMaxWait(long maxWait)
	{
		this.maxWait = maxWait;
	}

	public byte getWhenExhaustedAction()
	{
		return whenExhaustedAction;
	}

	public void setWhenExhaustedAction(byte whenExhaustedAction)
	{
		this.whenExhaustedAction = whenExhaustedAction;
	}

	public boolean isTestOnBorrow()
	{
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow)
	{
		this.testOnBorrow = testOnBorrow;
	}

	public boolean isTestOnReturn()
	{
		return testOnReturn;
	}

	public void setTestOnReturn(boolean testOnReturn)
	{
		this.testOnReturn = testOnReturn;
	}

	public boolean isTestWhileIdle()
	{
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle)
	{
		this.testWhileIdle = testWhileIdle;
	}

	public long getTimeBetweenEvictionRunsMillis()
	{
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis)
	{
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public int getNumTestsPerEvictionRun()
	{
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun)
	{
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public long getMinEvictableIdleTimeMillis()
	{
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis)
	{
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public long getSoftMinEvictableIdleTimeMillis()
	{
		return softMinEvictableIdleTimeMillis;
	}

	public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis)
	{
		this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
	}

}