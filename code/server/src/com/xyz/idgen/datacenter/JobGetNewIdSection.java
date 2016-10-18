package com.xyz.idgen.datacenter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.Utils;

public class JobGetNewIdSection implements Runnable
{
	private static Logger logger = LoggerFactory.getLogger(JobGetNewIdSection.class);
	private String idName;
	private DbHelper dbHelper;
	private ConcurrentHashMap<String, RedisClient> rcMap;
	private ConcurrentHashMap<String, IdInfo> idMap; 
	private long jobIndex;
	private long curId;
	private static int UPDATE_REDIS = 1;//更新redis
	private static int UPDATE_LOCAL_CACHE = 2;//只更新id生成器程序的本地缓存，无需更新redis
	private static int UPDATE_DONOTHING = 3;//无需更新
	public JobGetNewIdSection(long jobIndex, long curId, String idName, DbHelper dbHelper, ConcurrentHashMap<String, RedisClient> rcMap, ConcurrentHashMap<String, IdInfo> idMap)
	{
		this.curId = curId;
		this.idName = idName;
		this.jobIndex = jobIndex;
		this.dbHelper = dbHelper;
		this.rcMap = rcMap;
		this.idMap = idMap;
	}

	@Override
	public void run()
	{
		String logFlag = "JobGetNewIdSection.run";
		IdInfo idInfo = idMap.get(idName); 
		if(idInfo == null)
		{
			logger.error("[lid:{}] [{}] cann't get id informaton for id {};!", jobIndex, logFlag, idName);
			return;
		}
		if((curId != IdInfo.INVALID_ID) && (idInfo.getUpdateId() > curId))
		{//本地缓存的更新ID大于当前的ID，说明本地缓存已经被
		//当前IDGen服务的其他线程更新过了，直接返回即可。
			return;
		}
		//从缓存中读取当前的ID的信息
		RedisClient rc = rcMap.get(idInfo.getRedisFlag());
		if(rc == null)
		{
			logger.error("[lid:{}] [{}] cann't get redis client for id name:{}; redis flag:{}!", jobIndex, logFlag, idInfo.getIdName(), idInfo.getRedisFlag());
			return;
		}
		String idValueKey = DefaultValues.CACHE_ID_PREFIX + idInfo.getIdName() + DefaultValues.CACHE_ID_VALUE_SUFFIX;
		String idLockKey = DefaultValues.CACHE_ID_PREFIX + idInfo.getIdName() + DefaultValues.CACHE_ID_LOCK_SUFFIX;
		if(rc.setLock(jobIndex, idLockKey, DefaultValues.CACHE_ID_LOCK_VALUE, DefaultValues.REDIS_LOCK_EXPIRE))
		{
			//计算新申请ID段长
			double idConsumeSpeed = (double)(curId - idInfo.getIdStart()) * 1000 / (System.currentTimeMillis() - idInfo.getLoadTime());
			long newIdSection = (long)(idConsumeSpeed * idInfo.getMinLoadInterval() * DefaultValues.NEW_ID_SECTION_SCALING_RATIO);
			if(newIdSection > idInfo.getMaxRange())
				newIdSection = idInfo.getMaxRange();
			if(newIdSection < idInfo.getMinRange())
				newIdSection = idInfo.getMinRange();
			if(newIdSection <= 0)
				newIdSection = DefaultValues.ID_RANGE;
			logger.info("[lid:{}] [{}] id name:{}; new id section:{}; current information:{}", jobIndex, logFlag, idInfo.getIdName(), newIdSection, idInfo);

			
			String idPropertyKey = DefaultValues.CACHE_ID_PREFIX + idInfo.getIdName() + DefaultValues.CACHE_ID_PROPERTY_SUFFIX;
			/*
			 * 如果当前的ID状态还是有效，则要检查Redis数据源；
			 * 否则，无条件从数据库中加载
			 * */
			if(idInfo.getIdState() == DefaultValues.ID_STATE_VALID)
			{
				Map<String, String> cacheIdProperty = rc.hgetAll(jobIndex, idPropertyKey);
				/*
				 * (1)redis中不存在该id的有效信息时必须更新redis；
				 * (2)redis中该id的信息有效时，本地缓存和redis不一致，则更新本地缓存；
				 * (3)redis中该id的信息有效时，本地缓存和redis一致，则不更新任何信息
				*/
				int upDataDatus = checkUpdateStatus(cacheIdProperty, idInfo, rc);
				if(upDataDatus == UPDATE_LOCAL_CACHE)
				{
					if(logger.isDebugEnabled())
						logger.debug("[lid:{}] [{}] only update local cache from redis, local info:{}; redis info:{}; redis flag:{}!", jobIndex, logFlag, idInfo.toString(), cacheIdProperty, idInfo.getRedisFlag());
					IdInfo newIdInfo = IdInfo.createIdInfo(cacheIdProperty);
					idMap.put(newIdInfo.getIdName(), newIdInfo);
					rc.del(jobIndex, idLockKey);
					return ;
				}
				else if(upDataDatus == UPDATE_DONOTHING)
				{
					if(logger.isDebugEnabled())
						logger.debug("[lid:{}] [{}] Do nothing for id:{}, local info:{}; redis info:{}; redis flag:{}!", jobIndex, logFlag, idName, idInfo.toString(), cacheIdProperty, idInfo.getRedisFlag());
					return;
				}
			}
			
			Map<String, String> dbData = dbHelper.getOneIdSectionFromDB(jobIndex, idName, newIdSection);
			if(dbData == null)
			{
				logger.debug("[lid:{}] [{}] We cann't get new section for id: {} from data base!; ", jobIndex, logFlag, idInfo.getIdName());
				return;
			}
			/*如下图所示，新申请段的起始ID应该为curId，而不是从数据库中返回的起始ID，
			 * 如果不做调整，将浪费约从curID到lastEndId之间的ID；
			 * 	+---------------------+-------------+--------------------------------+
			 * 	|         	 		  |	            |								 |
			 * 	+---------------------+-------------+--------------------------------+
			 * lastStartID          curId		lastEndId(db start id)		new section end Id
			 * */
			long dbStartID = Long.parseLong(dbData.get(DefaultValues.ID_INFO_ID_START));
			/*
			 * 这里设置的起始ID不需要非常精确，他只是下次取ID分段时使用的起始ID
			 * 起始ID将被用于计算新段长，因此这里还是要将其调整一下，否则段长计算的
			 * 就不太清楚了。
			 * */
			long newStartId;
			if(curId <= 0)
				newStartId = dbStartID;
			else
				newStartId = curId;
			dbData.put(DefaultValues.ID_INFO_ID_START, String.valueOf(newStartId));
			float loadPercentage = Float.parseFloat(dbData.get(DefaultValues.ID_INFO_LOAD_PERCENTAGE));
			long updateId = (long)(dbStartID + newIdSection * loadPercentage);
			dbData.put(DefaultValues.ID_INFO_ID_UPDATE, String.valueOf(updateId));
			long endId = dbStartID + newIdSection;
			dbData.put(DefaultValues.ID_INFO_ID_END, String.valueOf(endId));
			//ID value的键不存在，或者不在正常范围内:(newStartId, endId)，就需要设置ID的值到缓存中
			logger.debug("[lid:{}] [{}] id name:{}; redisCurId:{}; newStartId:{}; endId:{}!; ", jobIndex, logFlag, idInfo.getIdName(), curId, newStartId, endId);
			if((curId == IdInfo.INVALID_ID) || !rc.exists(jobIndex, idValueKey))
			{
				if(!rc.set(jobIndex, idValueKey, String.valueOf(newStartId)))
				{
					logger.error("[lid:{}] [{}] set new id:{} into redis client :{} fail!; ", jobIndex, logFlag, idInfo.getIdName(), idInfo.getRedisFlag());
					return;
				}
			}
			//防止数据污染，直接删掉
			rc.del(jobIndex, idPropertyKey);
			if(rc.hmset(jobIndex, idPropertyKey, dbData))
			{
				IdInfo newIdInfo = IdInfo.createIdInfo(dbData);
				idMap.put(newIdInfo.getIdName(), newIdInfo);
				rc.del(jobIndex, idLockKey);
			}
			rc.del(jobIndex, idLockKey);
		}
		/*
		 * 加锁失败时，说明其他id生成器正在进行该ID的新段申请操作，直接返回
		 * */
	}
	
	/*
	 * 函数名称：checkUpdateStatus
	 * 函数功能：检查更新状态
	 * 描述：
	 * (1)redis中不存在该id的有效信息时必须更新redis；
	 * (2)redis中该id的信息有效时，本地缓存和redis不一致，则更新本地缓存；
	 * (3)redis中该id的信息有效时，本地缓存和redis一致，则不更新任何信息
	 * */
	private int checkUpdateStatus(Map<String, String> cacheIdProperty, IdInfo idInfo, RedisClient rc)
	{
		/*
		 * (1)~(3)用于检查redis中的信息是否有效
		 * */
		//(1)redis中没有包含该ID的属性信息，则返回失败，必须更新Redis
		if(cacheIdProperty == null || cacheIdProperty.isEmpty())
			return UPDATE_REDIS;
		
		String idValueKey = DefaultValues.CACHE_ID_PREFIX + idInfo.getIdName() + DefaultValues.CACHE_ID_VALUE_SUFFIX;
		//(2)Redis中id值、起始ID、结束ID和更新ID这些关键信息不存在，则必须更新Redis
		String strIdValue = rc.get(jobIndex, idValueKey);
		String strIdStart = cacheIdProperty.get(DefaultValues.ID_INFO_ID_START);
		String strIdEnd = cacheIdProperty.get(DefaultValues.ID_INFO_ID_END);
		String strIdUpdate = cacheIdProperty.get(DefaultValues.ID_INFO_ID_UPDATE);
		if(!Utils.checkString(strIdValue) || !Utils.checkString(strIdStart) || !Utils.checkString(strIdEnd) || !Utils.checkString(strIdUpdate))
			return UPDATE_REDIS;

		long idValue = Utils.str2long(jobIndex, strIdValue);
		long idStart = Utils.str2long(jobIndex, strIdStart);
		long idUpdate = Utils.str2long(jobIndex, strIdUpdate);
		long idEnd = Utils.str2long(jobIndex, strIdEnd);
		//(3)Redis中id值在redis的更新之后，则必须更新Redis
		if(idValue > idUpdate)
			return UPDATE_REDIS;
		/*
		 * (4)程序走到这里，说明Redis中存在该id的信息，并且有效，因此，无需更新redis，
		 * 接下来就需要判断是否更新本地缓存，以使本地缓存和redis中的数据 一致；
		 * 
		 * 如果Redis数据源中的ID信息与本地缓存一致，说明本地缓存已经被本进程内的其他异步任务更新过，
		 * 则无需更新本地缓存；如果不一致，则说明需要更新本地缓存；
		 * */
		boolean isEqual = true; 
		isEqual = isEqual && (Integer.parseInt(cacheIdProperty.get(DefaultValues.ID_INFO_STATE)) == DefaultValues.ID_STATE_VALID);
		isEqual = isEqual && (Utils.str2long(jobIndex, cacheIdProperty.get(DefaultValues.ID_INFO_LOAD_TIME)) == idInfo.getLoadTime());
		isEqual = isEqual && (idStart == idInfo.getIdStart());
		isEqual = isEqual && (Utils.str2long(jobIndex, cacheIdProperty.get(DefaultValues.ID_INFO_CUR_RANGE)) == idInfo.getCurRange());
		isEqual = isEqual && (idEnd == idInfo.getIdEnd());
		isEqual = isEqual && (Utils.str2long(jobIndex, cacheIdProperty.get(DefaultValues.ID_INFO_MAX_RANGE)) == idInfo.getMaxRange());
		isEqual = isEqual && (Utils.str2long(jobIndex, cacheIdProperty.get(DefaultValues.ID_INFO_MIN_RANGE)) == idInfo.getMinRange());
		isEqual = isEqual && idUpdate == idInfo.getUpdateId();
		if(Utils.checkString(cacheIdProperty.get(DefaultValues.ID_INFO_PREFIX)))
		{
			isEqual = isEqual && (cacheIdProperty.get(DefaultValues.ID_INFO_PREFIX).equals(idInfo.getIdPrefix()));	
		}
		if(Utils.checkString(cacheIdProperty.get(DefaultValues.ID_INFO_PROJECT)))
		{
			isEqual = isEqual && (cacheIdProperty.get(DefaultValues.ID_INFO_PROJECT).equals(idInfo.getProjectName()));	
		}
		isEqual = isEqual && (cacheIdProperty.get(DefaultValues.ID_INFO_REDIS_FLAG).equals(idInfo.getRedisFlag()));
		isEqual = isEqual && (Integer.parseInt(cacheIdProperty.get(DefaultValues.ID_INFO_STATE)) == idInfo.getIdState());
		
		return isEqual ? UPDATE_DONOTHING : UPDATE_LOCAL_CACHE;
	}
	
}
