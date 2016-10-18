package com.xyz.idgen.datacenter;

import com.alibaba.fastjson.JSONObject;
import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.Utils;

public class IdDBInfo
{
	private String project_name;/*所属项目名称*/
	private String id_name;/*id的名称*/
	private String id_prifx;
	private long   id_start;/*不可为空，默认1000*/
	private long last_range;//申请ID段的最小范围
	private long max_range;//申请ID段的最大范围
	private long min_range;//申请ID段的最小范围
	private long last_load_time;//本次申请id段的时间
	private String applicant;/*申请人*/
	private String redis_flag;	/*当前ID被分配的Redis标识*/
	private int state;/*当前ID的状态, 1：有效；2：被删除*/
	private int min_load_interval;
	private float load_percentage;
	private int lock_expire;
	private IdDBInfo(){}
	public static IdDBInfo createIdDbInfo()
	{
		IdDBInfo newIdDBInfo = new IdDBInfo();
		newIdDBInfo.id_start = 1000l;
		newIdDBInfo.last_range = 1000l;
		newIdDBInfo.max_range = 1000000l;
		newIdDBInfo.min_range = 1000l;
		newIdDBInfo.last_load_time = System.currentTimeMillis();
		newIdDBInfo.min_load_interval = 3600;
		newIdDBInfo.load_percentage = 0.9f;
		newIdDBInfo.lock_expire = 60;
		return newIdDBInfo;
	}
	
	public static IdDBInfo createIdDbInfo(String idDbInfo)
	{
		JSONObject joIdDBInfo = Utils.str2Json(DefaultValues.LOG_INDEX_INIT, idDbInfo);
		if(joIdDBInfo == null)
		{
			return null;
		}
		IdDBInfo newIdDBInfo = new IdDBInfo();
		newIdDBInfo.project_name = joIdDBInfo.getString("project_name");
		newIdDBInfo.id_name = joIdDBInfo.getString("id_name");
		newIdDBInfo.id_prifx = joIdDBInfo.getString("id_prifx");
		newIdDBInfo.id_start = joIdDBInfo.getLongValue("id_start");
		newIdDBInfo.last_range = joIdDBInfo.getLongValue("last_range");
		newIdDBInfo.max_range = joIdDBInfo.getLongValue("max_range");
		newIdDBInfo.min_range = joIdDBInfo.getLongValue("min_range");
		newIdDBInfo.last_load_time = joIdDBInfo.getLongValue("last_load_time");
		newIdDBInfo.applicant = joIdDBInfo.getString("applicant");
		newIdDBInfo.redis_flag = joIdDBInfo.getString("redis_flag");
		newIdDBInfo.state = joIdDBInfo.getIntValue("state");
		newIdDBInfo.min_load_interval = joIdDBInfo.getIntValue("min_load_interval");
		newIdDBInfo.load_percentage = joIdDBInfo.getFloatValue("load_percentage");
		newIdDBInfo.lock_expire = joIdDBInfo.getIntValue("lock_expire");
		newIdDBInfo.other_info = joIdDBInfo.getString("other_info");
		return newIdDBInfo;
	}
	public JSONObject toJsonObject()
	{
		JSONObject joIdDbInfo = new JSONObject();
		joIdDbInfo.put("project_name", project_name);
		joIdDbInfo.put("id_name", id_name);
		joIdDbInfo.put("id_prifx", id_prifx);
		joIdDbInfo.put("id_start", id_start);
		joIdDbInfo.put("last_range", last_range);
		joIdDbInfo.put("max_range", max_range);
		joIdDbInfo.put("min_range", min_range);
		joIdDbInfo.put("last_load_time", last_load_time);
		joIdDbInfo.put("applicant", applicant);
		joIdDbInfo.put("redis_flag", redis_flag);
		joIdDbInfo.put("state", state);
		joIdDbInfo.put("min_load_interval", min_load_interval);
		joIdDbInfo.put("load_percentage", load_percentage);
		joIdDbInfo.put("lock_expire", lock_expire);
		joIdDbInfo.put("other_info", other_info);

		return joIdDbInfo;
	}
	
	public String toString()
	{
		return toJsonObject().toJSONString();
	}
	
	public boolean isValid()
	{
		boolean res = true;
		res = res && Utils.checkString(project_name);
		res = res && Utils.checkString(id_name);
		res = res && Utils.checkString(applicant);
		res = res && Utils.checkString(redis_flag);
		res = res && (max_range > min_range);
		res = res && (state == DefaultValues.ID_STATE_VALID);
		res = res && (lock_expire > 0);
		res = res && (load_percentage > 0);
		res = res && (load_percentage < 1);
		res = res && (min_range > 0);
		res = res && (min_load_interval > 0);
		res = res && (min_range > 0);
		res = res && (id_start > 0);
		res = res && (last_range >= 0);
		return res;
	}
	
	public String getProject_name()
	{
		return project_name;
	}
	public void setProject_name(String project_name)
	{
		this.project_name = project_name;
	}
	public String getId_name()
	{
		return id_name;
	}
	public void setId_name(String id_name)
	{
		this.id_name = id_name;
	}
	public String getId_prifx()
	{
		return id_prifx;
	}
	public void setId_prifx(String id_prifx)
	{
		this.id_prifx = id_prifx;
	}
	public long getId_start()
	{
		return id_start;
	}
	public void setId_start(long id_start)
	{
		this.id_start = id_start;
	}
	public long getLast_range()
	{
		return last_range;
	}
	public void setLast_range(long last_range)
	{
		this.last_range = last_range;
	}
	public long getMax_range()
	{
		return max_range;
	}
	public void setMax_range(long max_range)
	{
		this.max_range = max_range;
	}
	public long getMin_range()
	{
		return min_range;
	}
	public void setMin_range(long min_range)
	{
		this.min_range = min_range;
	}
	public long getLast_load_time()
	{
		return last_load_time;
	}
	public void setLast_load_time(long last_load_time)
	{
		this.last_load_time = last_load_time;
	}
	public String getApplicant()
	{
		return applicant;
	}
	public void setApplicant(String applicant)
	{
		this.applicant = applicant;
	}
	public String getRedis_flag()
	{
		return redis_flag;
	}
	public void setRedis_flag(String redis_flag)
	{
		this.redis_flag = redis_flag;
	}
	public int getState()
	{
		return state;
	}
	public void setState(int state)
	{
		this.state = state;
	}
	public int getMin_load_interval()
	{
		return min_load_interval;
	}
	public void setMin_load_interval(int min_load_interval)
	{
		this.min_load_interval = min_load_interval;
	}
	public float getLoad_percentage()
	{
		return load_percentage;
	}
	public void setLoad_percentage(float load_percentage)
	{
		this.load_percentage = load_percentage;
	}
	public int getLock_expire()
	{
		return lock_expire;
	}
	public void setLock_expire(int lock_expire)
	{
		this.lock_expire = lock_expire;
	}
	public String getOther_info()
	{
		return other_info;
	}
	public void setOther_info(String other_info)
	{
		this.other_info = other_info;
	}
	private String other_info;/*备注信息，长度小于128位*/
}
