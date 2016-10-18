package com.xyz.idgen.datacenter;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.Utils;
import com.xyz.idgen.config.Config;
import com.xyz.idgen.config.ConfigKeys;

public class DataCenter
{
	private static String getClassName()
	{
		return "DataCenter";
	}

	private static Logger logger = LoggerFactory.getLogger(DataCenter.class);
	public static Object lock = new Object();	
	private ConcurrentHashMap<String, RedisClient> rcMap;//根据RedisFlag，例如Redis的ip:port查redis对象
	private boolean isWorking = false;
	private ConcurrentHashMap<String, IdInfo> idMap;
	private DbHelper dbHelper = new DbHelper();
	private ExecutorService jobHandler = Executors.newFixedThreadPool(DefaultValues.NUM_HANDLER_THREAD);
	public ConcurrentHashMap<String, IdInfo> getIdMaps()
	{
		return idMap;
	}
	 
	public boolean init(long logIndex, Config cfg) 
	{
		String logFlag = getClassName() + ".init";
		isWorking = false;
		if(!initDbHelper(logIndex, cfg))
		{
			logger.error("[lid:{}] [{}] mysql initialized fail!", logIndex, logFlag);
			return false;
		}
		if(!loadDataFromDB(logIndex))
		{
			logger.error("[lid:{}] [{}] Loading data from mysql fail!", logIndex, logFlag);
			return false;
		}
		if(!initRedisClientPool(logIndex))
		{
			logger.error("[lid:{}] [{}] redis client pool initialized fail!", logIndex, logFlag);
			return false;
		}
		if(!initIds(logIndex))
		{
			logger.error("[lid:{}] [{}] redis client initialized fail!", logIndex, logFlag);
			return false;
		}
		logger.info("[lid:{}] [{}] data center initialized success!", logIndex, logFlag);
		isWorking = true;
		return isWorking;
	}
	
	/**
	 * 函数名称：initIds
	 * 函数功能：根据数据库中读取的配置信息，初始化ID在Redis中的数据结构
	 * */
	private boolean initIds(long logIndex)
	{
		String logFlag = getClassName() + ".initIds";
		for(Map.Entry<String, IdInfo> anEntity : idMap.entrySet())
		{
			IdInfo anIdInfo = anEntity.getValue();
			if(anIdInfo == null || !anIdInfo.isvalid())
			{
				logger.error("[lid:{}] [{}] ID information error! detail:{}", logIndex, logFlag, anIdInfo);
				return false;
			}
			logger.info("[lid:{}] [{}] create init Job for id:{}; detail:{}", logIndex, logFlag, anIdInfo.getIdName(), anIdInfo);
			// 产生一个申请新ID段的任务
			jobHandler.execute(new JobGetNewIdSection(logIndex, IdInfo.INVALID_ID, anIdInfo.getIdName(), dbHelper, rcMap, idMap));
		}
		return true;
	}
	
	/**
	 * 函数名称：initRedisClientPool
	 * 函数功能：根据数据库中读取的配置信息，初始化Jedis连接池
	 * */
	private boolean initRedisClientPool(long logIndex)
	{
		String logFlag = getClassName() + ".initRedisClientPool";
		for(Map.Entry<String, RedisClient> anEntity : rcMap.entrySet())
		{
			RedisClient rc = anEntity.getValue();
			if(rc == null || !rc.init(logIndex))
			{
				logger.error("[lid:{}] [{}] Redis Client initialized fail!", logIndex, logFlag);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 函数名称：loadDataFromDB
	 * 函数功能：从数据库中加载所有的数据，包括：redis信息
	 * 			配置数据、ID相关信息等；
	 * @author xiaoyaozi
	 * @param long logIndex 日志索引
	 * @return boolean 加载成功返回true；失败返回false
	 * */
	private boolean loadDataFromDB(long logIndex)
	{
		String logFlag = getClassName() + ".loadDataFromDB";
		rcMap = dbHelper.getRedisInfoFromDb(logIndex);
		if(rcMap == null)
		{
			logger.error("[lid:{}] [{}] get redis information from mysql fail!", logIndex, logFlag );
			return false;
		}
		idMap = dbHelper.getAllIdInfoFromDB(logIndex, DefaultValues.ID_RANGE);
		if(idMap == null)
		{
			logger.error("[lid:{}] [{}] get id information from mysql fail!", logIndex, logFlag);
			return false;
		}
		return true;
	}


	/**
	 * 函数名称：initDbHelper
	 * 函数功能：根据配置文件中的参数，初始化mysql连接池
	 * @param long logIndex 日志索引
	 * @param  Config cfg 配置文件中的配置数据
	 * @return boolean 成功返回true，失败返回false
	 * */
	private boolean initDbHelper(long logIndex, Config cfg)
	{
		String logFlag = getClassName() + ".initDbHelper";
		String dbHost = cfg.getString(ConfigKeys.DB_HOST);
		if(!Utils.checkString(dbHost))
		{
			logger.error("[lid:{}] [{}] parameter error ! your parameter :dbHost :{}", logIndex, logFlag, dbHost);
			return false;
		}
		int dbPort = cfg.getInt(ConfigKeys.DB_PORT);
		if(!Utils.checkPort(dbPort))
		{
			logger.error("[lid:{}] [{}] parameter error ! your parameter :dbPort :{}", logIndex, logFlag, dbPort);
			return false;
		}
		String dbDbName = cfg.getString(ConfigKeys.DB_DB_NAME);
		if(!Utils.checkString(dbDbName))
		{
			logger.error("[lid:{}] [{}] parameter error ! your parameter :dbDbName :{}", logIndex, logFlag, dbDbName);
			return false;
		}
		String dbUserName = cfg.getString(ConfigKeys.DB_USER_NAME);
		String dbPassword = cfg.getString(ConfigKeys.DB_PASSWORD);
		String jdbcUrl = "jdbc:mysql://" + dbHost + "/" + dbDbName + "?useUnicode=true&characterEncoding=UTF-8";
		try
		{
			if(!dbHelper.init(jdbcUrl, dbUserName, dbPassword, DefaultValues.DB_MIN_POOL_SIZE, DefaultValues.DB_MAX_POOL_SIZE, DefaultValues.DB_MAX_IDLE_TIME, DefaultValues.DB_MAX_STATEMENTS, DefaultValues.DB_CHECKOUT_TIMEOUT))
			{
				logger.error("[lid:{}] [{}] mysql initialized fail! your jdbc url :{}", logIndex, logFlag, jdbcUrl);
				return false;
			}
		}
		catch (PropertyVetoException e)
		{
			logger.error("[lid:{}] [{}] exception happened! jdbcUrl:{}, dbUserName:{}, dbPassword:{}, min pool size:{}, max pool size:{}, max idle time:{}, maxstatements:{}, checkout timeout:{}, detail:\n{}", logIndex, logFlag, jdbcUrl, dbUserName, dbPassword, DefaultValues.DB_MIN_POOL_SIZE, DefaultValues.DB_MAX_POOL_SIZE, DefaultValues.DB_MAX_IDLE_TIME, DefaultValues.DB_MAX_STATEMENTS, DefaultValues.DB_CHECKOUT_TIMEOUT, e);
		}
		return true;
	}

//	private boolean getRedisInfoFromDB(long logIndex)
//	{
//		String logFlag = getClassName() + ".getRedisInfoFromDB";
//		try
//		{
//			;
//			return !rcMap.isEmpty();
//		}
//		catch (Exception e)
//		{
//			logger.error("[lid:{}] [{}] exception happened! jdbcUrl:{}, dbUserName:{}, dbPassword:{}, min pool size:{}, max pool size:{}, max idle time:{}, maxstatements:{}, checkout timeout:{}, detail:\n{}", logIndex, logFlag, e);
//			return false;
//		}
//	}
	

	/**
	 * 函数名称：getId 函数功能：获取一个指定类型的ID
	 * 
	 * @author xiaoyaozi
	 * @param long logIndex 日志索引
	 * @param String idName id的名字
	 * @return long 成功获取到ID时，返回一个long型的ID，否则返回无效的ID值
	 * */
	public long getId(long logIndex, String idName)
	{
		String logFlag = getClassName() + ".getId";
		if (!isWorking)
		{
			logger.error("[lid:{}] [{}] id generator is unworking! idType:{}", logIndex, logFlag, idName);
			return DefaultValues.ID_ERROR;
		}
		if(logger.isDebugEnabled())
			logger.debug("[lid:{}] [{}] will get id :{}", logIndex, logFlag, idName);
		
		IdInfo idInfo = idMap.get(idName);
		if (idInfo == null)
		{
			logger.error("[lid:{}] [{}] Not support this ID! id Name:{}", logIndex, logFlag, idName);
			return DefaultValues.ID_ERROR;
		}
		if(idInfo.getIdState() != DefaultValues.ID_STATE_VALID)
		{
			logger.error("[lid:{}] [{}] ID state error! id Name:{}; state:{}; detail information:{}", logIndex, logFlag, idName, idInfo.getIdState(), idInfo.toString());
			if(logger.isDebugEnabled())
				logger.debug("[lid:{}] [{}] We need a job to check our local id information! Create new job for id {}!", logIndex, logFlag, idName);
			// 产生一个申请新ID段的任务
			jobHandler.execute(new JobGetNewIdSection(logIndex, IdInfo.INVALID_ID, idName, dbHelper, rcMap, idMap));

			return DefaultValues.ID_ERROR;
		}
		RedisClient redisClient = rcMap.get(idInfo.getRedisFlag());
		if (null == redisClient)//如果rc为null，说明ip:port对应的rc没有初始化		
		{
			logger.error("[lid:{}] [{}] Cann't find redisclient with RedisFlag:{}", logIndex, logFlag, idInfo.getRedisFlag());
			return DefaultValues.ID_ERROR;
		}
		String key = DefaultValues.CACHE_ID_PREFIX + idName + DefaultValues.CACHE_ID_VALUE_SUFFIX;
		long newId = redisClient.incr(logIndex, key);
		//新申请ID在起始ID和更新ID之间，则直接返回
		if(idInfo.getIdStart() < newId && newId <= idInfo.getUpdateId())
		{
			return newId;
		}
		logger.debug("[lid:{}] [{}] we may need a new job for current id! create new job for id {}!", logIndex, logFlag, idName);
		// 产生一个申请新ID段的任务
		jobHandler.execute(new JobGetNewIdSection(logIndex, newId, idName, dbHelper, rcMap, idMap));
		if(idInfo.getUpdateId() < newId && newId < idInfo.getIdEnd())
		{
			return newId;
		}
		idInfo.setIdState(DefaultValues.ID_STATE_OUT_OF_RANGE);
		logger.error("[lid:{}] [{}] current id is invalid! id:{}; redisFlag:{}; out of range:[{}, {})", logIndex, logFlag, newId, idInfo.getRedisFlag(), idInfo.getIdStart(), idInfo.getIdEnd());
		return DefaultValues.ID_ERROR;
	}


	/**
	 * 函数名称：getStrId 函数功能：获取一个字符串格式的指定类型的ID，可以为之添加一个前缀
	 * 
	 * @author xiaoyaozi
	 * @param long logIndex 日志索引
	 * @param String idName id的类型
	 * @param boolean usePrefix 是否使用默认前缀
	 * @return String 成功获取到ID时，返回一个String型的ID，否则返回null
	 * */
	public String getStrId(long logIndex, String idName, boolean usePrefix)
	{
		String logFlag = getClassName() + ".getStrId";
		if (!isWorking)
		{
			logger.error("[lid:{}] [{}] id generator is unworking! idName:{}", logIndex, logFlag, idName);
			return null;
		}

		long newId = getId(logIndex, idName);
		 
		String idprefix = "";
		if (usePrefix && !Utils.checkString(idMap.get(idName).getIdPrefix()))
			idprefix = idMap.get(idName).getIdPrefix();

		String res = idprefix + String.valueOf(newId);
		if(logger.isDebugEnabled())
			logger.debug("[lid:{}] [{}] {}:{}", logIndex, logFlag, idName, res);
		return res;
	}

	/**
	 * 函数名称：delIdTypes 函数功能：删除一个id类型
	 * 
	 * @author xiaoyaozi
	 * @param long     logIndex 日志索引
	 * @param String   idType 待删除的ID类型
	 * @return boolean 成功删除ID时，返回true，否则返回false
	 * */
	public boolean delIdTypes(long logIndex, String idType)
	{
		String logFlag = getClassName() + ".delId";
		if (!isWorking)
		{
			logger.error("[lid:{}] [{}] id generator is unworking! idType:{}", logIndex, logFlag, idType);
			return false;
		}
		//TODO
		return false;
	}

	public void shutdown(long logIndex)
	{
		isWorking = false;

		for(RedisClient redisClient : rcMap.values())//遍历rcMap
		{
			redisClient.shutDown(logIndex);
		}
	}

	public String getAllIdInfo(long logIndex)
	{
		JSONObject joRes = new JSONObject();
		for(Map.Entry<String, IdInfo> anEntity : idMap.entrySet())
		{
			joRes.put(anEntity.getKey(), anEntity.getValue().toString());
		}
		return joRes.toJSONString();
	}

	public boolean addNewId(long logIndex, String idParamInfo)
	{
		String logFlag = getClassName() + ".addNewId";
		IdDBInfo idDbInfo = IdDBInfo.createIdDbInfo(idParamInfo);
		if(idDbInfo == null || !idDbInfo.isValid())
		{
			logger.error("[lid:{}] [{}] check your id infomation fail! your parameter:\n{}", logIndex, logFlag, idParamInfo);
			return false;
		}
		RedisClient rc = rcMap.get(idDbInfo.getRedis_flag());
		if(rc == null)
		{
			logger.error("[lid:{}] [{}] We can't find a redis for your id! please check your redisflag, your parameter:\n{}", logIndex, logFlag, idParamInfo);
			return false;
		}
		if(idDbInfo.getLast_load_time() <= 0)
		{
			idDbInfo.setLast_load_time(System.currentTimeMillis());
		}
		Map<String, String> idProperty = IdInfo.toMap(IdInfo.createIdInfo(idDbInfo));
		if(idProperty == null || idProperty.isEmpty())
		{
			logger.error("[lid:{}] [{}] change your id infomation into map fail! your data{}", logIndex, logFlag, idDbInfo);
			return false;
		}
		//修改存入数据的起始ID为下一段的起始ID
		idDbInfo.setId_start(idDbInfo.getId_start() + idDbInfo.getLast_range());
		if(!dbHelper.addNewId(logIndex, idDbInfo))
		{
			logger.error("[lid:{}] [{}] Insert your id infomation into mysql fail! your parameter:\n{}", logIndex, logFlag, idParamInfo);
			return false;
		}
		
		String idPropertyKey = DefaultValues.CACHE_ID_PREFIX + idDbInfo.getId_name() + DefaultValues.CACHE_ID_PROPERTY_SUFFIX;
		String idValueKey = DefaultValues.CACHE_ID_PREFIX + idDbInfo.getId_name() + DefaultValues.CACHE_ID_VALUE_SUFFIX;
		boolean res = rc.hmset(logIndex, idPropertyKey, idProperty);
		res = res && rc.set(logIndex, idValueKey, idProperty.get(DefaultValues.ID_INFO_ID_START));
		if(!res)
		{
			logger.error("[lid:{}] [{}] Set id infomation into redis fail! your data\n{}", logIndex, logFlag, idProperty);
			return false;
		}
		return true;
	}
	
	private Map<String, String> checkNewIdParam(long logIndex, String idParamInfo)
	{
		String logFlag = getClassName() + ".checkNewIdParam";
		JSONObject joIdInfo = JSONObject.parseObject(idParamInfo);
		if(joIdInfo == null)
		{
			logger.error("[lid:{}] [{}] Change your id infomation to a JSON object fail! your parameter:\n{}", logIndex, logFlag, idParamInfo);
			return null;
		}
		Map<String, String> res = new HashMap<String, String>();
		//TODO
		return res;
	}
}
