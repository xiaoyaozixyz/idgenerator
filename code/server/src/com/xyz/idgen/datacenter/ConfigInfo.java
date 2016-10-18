package com.xyz.idgen.datacenter;

import com.alibaba.fastjson.JSONObject;

public class ConfigInfo
{
	public int getMaxLoadInterval()
	{
		return maxLoadInterval;
	}
	public void setMaxLoadInterval(int maxLoadInterval)
	{
		this.maxLoadInterval = maxLoadInterval;
	}
	public float getLoadPercentage()
	{
		return loadPercentage;
	}
	public void setLoadPercentage(float loadPercentage)
	{
		this.loadPercentage = loadPercentage;
	}
	public int getLockExpire()
	{
		return lockExpire;
	}
	public void setLockExpire(int lockExpire)
	{
		this.lockExpire = lockExpire;
	}
	private String idName;
	
	public String getIdName()
	{
		return idName;
	}
	public void setIdName(String idName)
	{
		this.idName = idName;
	}
	/*从数据库中加载数据的最大间隔时间，
	 * 此参数可防止频繁操作数据库，单位：秒*/
	private int maxLoadInterval;
	
	/*加载比例，即当前ID段损耗达到此比例时
	 * 开始从数据库中申请新的ID段。*/
	private float loadPercentage;
	/*分布式锁的超期时间*/
	private int lockExpire;
	
	public boolean isValid()
	{
		return (maxLoadInterval > 0) && (loadPercentage > 0) && (lockExpire > 0);
	}
	
	public String getInfo()
	{
		JSONObject joMsg = new JSONObject();
		joMsg.put("idName", idName);
		joMsg.put("maxLoadInterval", maxLoadInterval);
		joMsg.put("loadPercentage", loadPercentage);
		joMsg.put("lockExpire", lockExpire);
		return joMsg.toJSONString();
	}
}
