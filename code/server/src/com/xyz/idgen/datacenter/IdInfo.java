package com.xyz.idgen.datacenter;


import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.Utils;

/**
 * 内部类IdInfo，用于存放一个类型的id的所有信息
 * */
public class IdInfo
{
	public static long INVALID_ID = -1;
	private String idName;/*id的名称*/
	private String projectName;/*所属项目名称*/
	private long 	idStart;/*不可为空，默认1000*/
	private long 	idEnd;/*不可为空，默认1000*/
	public long getIdEnd()
	{
		return idEnd;
	}
	public void setIdEnd(long idEnd)
	{
		this.idEnd = idEnd;
	}
	/*Id前缀，产生字符串ID时，可默认添加的前缀，
	 * 不填此项，则不会为返回的ID添加任何前缀*/
	private String idPrefix;
	private long maxRange;//申请ID段的最大范围
	private long curRange;//当前申请ID段的大小
	public long getCurRange()
	{
		return curRange;
	}
	public void setCurRange(long curRange)
	{
		this.curRange = curRange;
	}
	private long minRange;//申请ID段的最小范围
	private long updateId;//更新ID，达到此ID时，需更新ID段
	public void setUpdateId(long updateId)
	{
		this.updateId = updateId;
	}
	private long loadTime;//本次申请id段的时间
	private String applicant;/*申请人*/
	private int idState;/*当前ID的状态, 1：有效；2：被删除*/
	private String redisFlag;	/*当前ID被分配的Redis标识*/
	private String others;/*备注信息，长度小于128位*/
	/*从数据库中加载数据的最小间隔时间，
	 * 此参数可防止频繁操作数据库，单位：秒*/
	private int minLoadInterval;
	
	/*加载比例，即当前ID段损耗达到此比例时
	 * 开始从数据库中申请新的ID段。*/
	private float loadPercentage;
	/*分布式锁的超期时间*/
	private int lockExpire;
	public long getUpdateId()
	{
		return updateId;
	}
	public String getIdName()
	{
		return idName;
	}

	public void setIdName(String idName)
	{
		this.idName = idName;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public void setProjectName(String projectName)
	{
		this.projectName = projectName;
	}

	public long getIdStart()
	{
		return idStart;
	}

	public void setIdStart(long idStart)
	{
		this.idStart = idStart;
	}

	public String getIdPrefix()
	{
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix)
	{
		this.idPrefix = idPrefix;
	}


	public long getMaxRange()
	{
		return maxRange;
	}

	public void setMaxRange(long maxRange)
	{
		this.maxRange = maxRange;
	}

	public long getMinRange()
	{
		return minRange;
	}

	public void setMinRange(long minRange)
	{
		this.minRange = minRange;
	}

	public long getLoadTime()
	{
		return loadTime;
	}

	public void setLoadTime(long loadTime)
	{
		this.loadTime = loadTime;
	}

	public String getApplicant()
	{
		return applicant;
	}

	public void setApplicant(String applicant)
	{
		this.applicant = applicant;
	}

	public int getIdState()
	{
		return idState;
	}

	public void setIdState(int idState)
	{
		this.idState = idState;
	}

	public String getRedisFlag()
	{
		return redisFlag;
	}

	public void setRedisFlag(String redisFlag)
	{
		this.redisFlag = redisFlag;
	}

	public String getOthers()
	{
		return others;
	}

	public void setOthers(String others)
	{
		this.others = others;
	}

	@Override
	public String toString() 
	{
		return toJSONObject().toJSONString();		
	}
	
	public JSONObject toJSONObject()
	{		
		JSONObject jo = new JSONObject();
		jo.put(DefaultValues.ID_INFO_NAME, idName);
		jo.put(DefaultValues.ID_INFO_PROJECT, projectName);
		jo.put(DefaultValues.ID_INFO_PREFIX, idPrefix);
		jo.put(DefaultValues.ID_INFO_ID_START, idStart);
		jo.put(DefaultValues.ID_INFO_ID_END, idEnd);
		jo.put(DefaultValues.ID_INFO_MAX_RANGE, maxRange);
		jo.put(DefaultValues.ID_INFO_MIN_RANGE, minRange);
		jo.put(DefaultValues.ID_INFO_LOAD_TIME, loadTime);
		jo.put(DefaultValues.ID_INFO_APPLICANT, applicant);
		jo.put(DefaultValues.ID_INFO_STATE, idState);
		jo.put(DefaultValues.ID_INFO_REDIS_FLAG, redisFlag);
		jo.put(DefaultValues.ID_INFO_OTHERS, others);
		jo.put(DefaultValues.ID_INFO_MIN_LOAD_INTERVAL, minLoadInterval);
		jo.put(DefaultValues.ID_INFO_LOAD_PERCENTAGE, loadPercentage);
		jo.put(DefaultValues.ID_INFO_LOCK_EXPIER, lockExpire);
		jo.put(DefaultValues.ID_INFO_ID_UPDATE, updateId);
		jo.put(DefaultValues.ID_INFO_CUR_RANGE, curRange);
		return jo;	
	}

	public String getInfo()
	{
		return toJSONObject().toJSONString();
	}
	public boolean isvalid()
	{
		boolean res = true;
		res = res && (Utils.checkString(projectName));
		res = res && (Utils.checkString(idName));
		res = res && (maxRange > minRange);
		res = res && (minRange > 0);
		res = res && (Utils.checkString(redisFlag));
		res = res && (minLoadInterval > 0);
		res = res && (loadPercentage > 0);
		res = res && (lockExpire > 0);
		res = res && (idState == DefaultValues.ID_STATE_VALID);
		return res;
	}
	
	public int getMinLoadInterval()
	{
		return minLoadInterval;
	}
	public void setMinLoadInterval(int minLoadInterval)
	{
		this.minLoadInterval = minLoadInterval;
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
	public static Map<String, String> toMap(IdInfo idInfo)
	{
		if(idInfo == null || !idInfo.isvalid())
			return null;
		Map<String, String> res = new HashMap<String, String>();
		res.put(DefaultValues.ID_INFO_PROJECT, idInfo.getProjectName());
		res.put(DefaultValues.ID_INFO_NAME, idInfo.getIdName());
		res.put(DefaultValues.ID_INFO_PREFIX, idInfo.getIdPrefix());
		res.put(DefaultValues.ID_INFO_ID_START, String.valueOf(idInfo.getIdStart()));
		res.put(DefaultValues.ID_INFO_ID_END, String.valueOf(idInfo.getIdEnd()));
		res.put(DefaultValues.ID_INFO_CUR_RANGE, String.valueOf(idInfo.getCurRange()));
		res.put(DefaultValues.ID_INFO_MAX_RANGE, String.valueOf(idInfo.getMaxRange()));
		res.put(DefaultValues.ID_INFO_MIN_RANGE, String.valueOf(idInfo.getMinRange()));
		res.put(DefaultValues.ID_INFO_LOAD_TIME, String.valueOf(idInfo.getLoadTime()));
		res.put(DefaultValues.ID_INFO_APPLICANT, idInfo.getApplicant());
		res.put(DefaultValues.ID_INFO_REDIS_FLAG, idInfo.getRedisFlag());
		res.put(DefaultValues.ID_INFO_STATE, String.valueOf(idInfo.getIdState()));
		res.put(DefaultValues.ID_INFO_OTHERS, idInfo.getOthers());
		res.put(DefaultValues.ID_INFO_ID_UPDATE, String.valueOf(idInfo.getUpdateId()));
		res.put(DefaultValues.ID_INFO_MIN_LOAD_INTERVAL, String.valueOf(idInfo.getMinLoadInterval()));
		res.put(DefaultValues.ID_INFO_LOAD_PERCENTAGE, String.valueOf(idInfo.getLoadPercentage()));
		res.put(DefaultValues.ID_INFO_LOCK_EXPIER, String.valueOf(idInfo.getLockExpire()));

		return res;
	}
	
	public static IdInfo createIdInfo(Map<String, String> dbData)
	{
		if(dbData == null || dbData.isEmpty())
			return null;
		IdInfo newIdInfo = new IdInfo();
		newIdInfo.setProjectName(dbData.get(DefaultValues.ID_INFO_PROJECT));
		newIdInfo.setIdName(dbData.get(DefaultValues.ID_INFO_NAME));
		newIdInfo.setIdPrefix(dbData.get(DefaultValues.ID_INFO_PREFIX));
		newIdInfo.setIdStart(Long.valueOf(dbData.get(DefaultValues.ID_INFO_ID_START)));
		newIdInfo.setIdEnd(Long.valueOf(dbData.get(DefaultValues.ID_INFO_ID_END)));
		newIdInfo.setCurRange(Long.valueOf(dbData.get(DefaultValues.ID_INFO_CUR_RANGE)));
		newIdInfo.setMaxRange(Long.valueOf(dbData.get(DefaultValues.ID_INFO_MAX_RANGE)));
		newIdInfo.setMinRange(Long.valueOf(dbData.get(DefaultValues.ID_INFO_MIN_RANGE)));
		newIdInfo.setLoadTime(Long.valueOf(dbData.get(DefaultValues.ID_INFO_LOAD_TIME)));
		newIdInfo.setApplicant(dbData.get(DefaultValues.ID_INFO_APPLICANT));
		newIdInfo.setRedisFlag(dbData.get(DefaultValues.ID_INFO_REDIS_FLAG));
		newIdInfo.setIdState(Integer.parseInt(dbData.get(DefaultValues.ID_INFO_STATE)));
		newIdInfo.setOthers(dbData.get(DefaultValues.ID_INFO_OTHERS));
		newIdInfo.setUpdateId(Long.valueOf(dbData.get(DefaultValues.ID_INFO_ID_UPDATE)));
		newIdInfo.setMinLoadInterval(Integer.parseInt(dbData.get(DefaultValues.ID_INFO_MIN_LOAD_INTERVAL)));
		newIdInfo.setLoadPercentage(Float.valueOf(dbData.get(DefaultValues.ID_INFO_LOAD_PERCENTAGE)));
		newIdInfo.setLockExpire(Integer.parseInt(dbData.get(DefaultValues.ID_INFO_LOCK_EXPIER)));
		if(!newIdInfo.isvalid())
			return null;
		return newIdInfo;
	}
	
	public static IdInfo createIdInfo(IdDBInfo idDbInfo)
	{
		if(idDbInfo == null || !idDbInfo.isValid())
			return null;
		IdInfo newIdInfo = new IdInfo();
		newIdInfo.setProjectName(idDbInfo.getProject_name());
		newIdInfo.setIdName(idDbInfo.getId_name());
		newIdInfo.setIdPrefix(idDbInfo.getId_prifx());
		newIdInfo.setIdStart(idDbInfo.getId_start());
		newIdInfo.setIdEnd(idDbInfo.getId_start() + DefaultValues.ID_RANGE);
		newIdInfo.setCurRange(DefaultValues.ID_RANGE);
		newIdInfo.setMaxRange(idDbInfo.getMax_range());
		newIdInfo.setMinRange(idDbInfo.getMin_range());
		newIdInfo.setLoadTime(idDbInfo.getLast_load_time());
		newIdInfo.setApplicant(idDbInfo.getApplicant());
		newIdInfo.setRedisFlag(idDbInfo.getRedis_flag());
		newIdInfo.setIdState(idDbInfo.getState());
		newIdInfo.setOthers(idDbInfo.getOther_info());
		newIdInfo.setUpdateId((long)(idDbInfo.getId_start() + DefaultValues.ID_RANGE * idDbInfo.getLoad_percentage()));
		newIdInfo.setMinLoadInterval(idDbInfo.getMin_load_interval());
		newIdInfo.setLoadPercentage(idDbInfo.getLoad_percentage());
		newIdInfo.setLockExpire(idDbInfo.getLock_expire());
		if(!newIdInfo.isvalid())
			return null;
		return newIdInfo;
	}
}