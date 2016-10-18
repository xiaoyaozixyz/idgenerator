package com.xyz.idgen.datacenter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.alibaba.fastjson.JSONObject;
import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.common.ReturnValues;
import com.xyz.idgen.common.Utils;
import com.xyz.thrift.datatype.ResBool;
import com.xyz.thrift.datatype.ResSetStr;

public class RedisClient
{
	private String getClassName()
	{//仅用于内部获取类的最短名字
		return "RedisClient";
	}
	private static Logger logger = LoggerFactory.getLogger(RedisClient.class);
	
	private JedisPool jedisPool = null;
	private String host = null;
	private int port;
	private int maxActive;
	private int maxIdle;
	private int maxWait;
	private String password;
	private String myFlag;
	
	public String getMyInfo()
	{
		JSONObject joRedisClientInfo = new JSONObject();
		joRedisClientInfo.put("RedisFlag", myFlag);
		joRedisClientInfo.put("host", host);
		joRedisClientInfo.put("port", port);
		joRedisClientInfo.put("maxActive", maxActive);
		joRedisClientInfo.put("maxIdle", maxIdle);
		joRedisClientInfo.put("maxWait", maxWait);
		joRedisClientInfo.put("password", password);
		return joRedisClientInfo.toJSONString();
	}
	public String getMyFlag()
	{
		return myFlag;
	}
	private boolean isInited;
	private RedisClient(){}
	/**
	 * 函数名称：create
	 * 函数功能：创建一个RedisClient对象
	 * @author houjixin
	 * @param long logIndex 	日志索引
 	 * @param String host 		redis所在的主机名
 	 * @param int port 			redis所使用的端口号
 	 * @param String password   登陆redis所使用的密码
 	 * @param int maxActive		jedis连接池所用，控制一个pool可分配多少个jedis实例
 	 * @param int maxIdle		jedis连接池所用，控制一个pool最多有多少个状态为idle(空闲的)的jedis实例
 	 * @param int maxWait		jedis连接池所用，表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
 	 * @return RedisClient 		创建成功返回一个新建的RedisClient对象，失败返回空
	 * */
	
	public static RedisClient create(long logIndex, String redisFlag, String host, int port, String password, int maxActive, int maxIdle, int maxWait)
	{
		String logFlag = "RedisClient.init()";
		RedisClient rc = new RedisClient();
		rc.password 	= password;
		
		if(!Utils.checkString(redisFlag))
		{
			logger.error("[lid:{}] [{}] parameter error! redis flag is null!", logIndex, logFlag);
			return null;
		}
		rc.myFlag = redisFlag;
		if(!Utils.checkString(host))
		{
			logger.error("[lid:{}] [{}] parameter error! redis host is null!", logIndex, logFlag);
			return null;
		}
		rc.host 		= host;
		if(!Utils.checkPort(port))
		{
			logger.error("[lid:{}] [{}] parameter error! redis port is invalid! your value:{}", logIndex, logFlag, port);
			return null;
		}
		rc.port 		= port;
		if(maxActive <= 0)
		{
			logger.error("[lid:{}] [{}] parameter error! maxActive of jedis pool must bigger than 0! your value:{}", logIndex, logFlag, maxActive);
			return null;
		}
		rc.maxActive 	= maxActive;
		if(maxIdle <= 0)
		{
			logger.error("[lid:{}] [{}] parameter error! maxIdle of jedis pool must bigger than 0! your value:{}", logIndex, logFlag, maxIdle);
			return null;
		}
		rc.maxIdle 	= maxIdle;
		if(maxWait <= 0)
		{
			logger.error("[lid:{}] [{}] parameter error! maxWait of jedis pool must bigger than 0! your value:{}", logIndex, logFlag, maxWait);
			return null;
		}
		rc.maxWait 	= maxWait;
		rc.isInited = false;
		return rc;
	}
	public boolean init(long logIndex)
	{
		String logFlag = getClassName()+".init";
		JedisPoolConfig config = new JedisPoolConfig();
		 //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
       //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
       config.setMaxActive(maxActive);
       
       config.setMaxIdle(maxIdle);
       
       //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
       config.setMaxWait(maxWait);
       //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
       config.setTestOnBorrow(true);
       
       jedisPool = new JedisPool(config, host, port);
       isInited = test(logIndex);

       logger.info("[lid:{}] [{}]  "
    			    + "\n----------------------------------------------------"
  	     	 	 	+ "\n||  Redis Client informaiton: "
  	     	 	 	+ "\n|| host: " + host + "; port: " + port
  	     	 	 	+ "\n|| maxActive:" + maxActive + "; maxIdle:" + maxIdle
  	     	 	 	+ "\n|| maxWait:" + maxWait
  	     	 	 	+"\n----------------------------------------------------", logIndex, logFlag);
       if(!isInited)
       {
    	   logger.error("[lid:{}] [{}]  initialize fail!", logIndex, logFlag);
       }
       return isInited;
	}
	
	public void shutDown(long logIndex)
	{
		String logFlag = getClassName()+".shutDown";
		if(jedisPool == null)
			return;
		try
		{
			jedisPool.destroy();
			jedisPool = null;
		}catch(Exception ex)
		{
			logger.error("[lid:{}] [{}]  exception happened while shutting down jedis..., detail:{}", logIndex, logFlag, ex);
		}
	}


	/**
	 * 函数功能，测试redis是否连接成功
	 * */
	private boolean test(long logIndex)
	{
		String logFlag = getClassName()+".test";
		Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String test_key = "idg.test_key";
            String test_value = "idg.test_value";
            jedis.set(test_key, test_value);
            return test_value.equals(jedis.get(test_key));
        } catch (Exception e) {
            //释放redis对象
        	jedisPool.returnBrokenResource(jedis);
        	logger.error("[lid:{}] [{}]  exception happened!,test redis client({}:{}) fail! detail:{}", logIndex, logFlag, host, port,e);
        	return false;
        } finally {
            //返还到连接池
        	jedisPool.returnResource(jedis);
        }
	}

	/**
	 * 函数名称：incr
	 * 函数功能：执行redis的incr命令，将key对应的值加1，并返回加之后的值
	 * @author houjixin
	 * @param long logIndex 日志索引
 	 * @param String key id的类型
 	 * @return long 操作成功返回获取到的long型值，否则返回DefaultValues.ID_ERROR
	 * */
	public long incr(long logIndex, String key)
	{
		String logFlag = getClassName()+".incr";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		long id = DefaultValues.ID_ERROR;		
		if(!isInited)
		{
			//metrics数据统计结束
			mTimer.stop();
			return id;
		}
		Jedis jds = null;
		try
		{
			jds = jedisPool.getResource();
			id = jds.incr(key);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return id;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}]  Redis exeception, IdName:{} RedisclientInfo:{}; \ndetail:{}",logIndex, logFlag, key, getInfo(), e);
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return id;
		}
		
	}
	
	/**
	 * 函数名称：del
	 * 函数功能：执行redis的del命令，删除redis中的某个 key
	 * @author houjixin
	 * @param long logIndex 日志索引
 	 * @param String key 操作redis的key值
 	 * @return boolean 成功删除ID时，返回true，否则返回false
	 * */
	public boolean del(long logIndex, String key)
	{
		String logFlag = getClassName()+".del";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		if(!isInited)
		{
			//metrics数据统计结束
			mTimer.stop();
			return false;
		}
		Jedis jds = null;
		try
		{
			jds = jedisPool.getResource();
			jds.del(key);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return true;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}]  Redis exeception, function:del,key:{}, detail:{}", logIndex, logFlag, key, e);
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return false;
		}
	
	}
	
	public String getInfo()
	{
		JSONObject joMsg = new JSONObject();
		joMsg.put("myFlag", myFlag);
		joMsg.put("host", host);
		joMsg.put("port", port);
		joMsg.put("maxActive", maxActive);
		joMsg.put("maxIdle", maxIdle);
		joMsg.put("maxWait", maxWait);
		joMsg.put("password", password);
		return joMsg.toJSONString();
	}
	
	public Long setnx(long logIndex, String key, String value)
	{
		String logFlag = getClassName() + ".setnx";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		Long res = null;
		Jedis jds = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.setnx(key, value);
			jedisPool.returnResource(jds);
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			jedisPool.returnBrokenResource(jds);
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}, value:{}, detail:  {}", logIndex, logFlag, key, value, e);
			mTimer.stop();
			return null;
		}
	}
	
	public boolean setLock(long logIndex, String key, String value, int expire)
	{
		String logFlag = getClassName() + ".setLock";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		String res = null;
		Jedis jds = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.set(key, value, "nx", "ex", expire);
			jedisPool.returnResource(jds);
			mTimer.stop();
			return "OK".equalsIgnoreCase(res);
		}
		catch (Exception e)
		{
			jedisPool.returnBrokenResource(jds);
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}, value:{}, expire:{}, detail:  {}", logIndex, logFlag, key, value, expire, e);
			mTimer.stop();
			return false;
		}
	}
	public String hget(long logIndex, String key, String field)
	{//
		String logFlag = getClassName() + ".hget";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		String res = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.hget(key, field);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！parameters: key {}; field {}; detail:  {}", logIndex, logFlag, field, key, e);
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return null;
		}
		

	}

	public ResSetStr hVals(long logIndex, String key)
	{
		String logFlag = getClassName() + ".hVals";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		ResSetStr res = new ResSetStr();
		try
		{
			jds = jedisPool.getResource();
			res.value = new HashSet<String>(jds.hvals(key));
			res.res = ReturnValues.SUCCESS;
			jedisPool.returnResource(jds);
			
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！parameters: key {}; detail:  {}", logIndex, logFlag, key, e);
			res.value = null;
			res.res = ReturnValues.INNER_ERROR;
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		
	}

	public boolean hset(long logIndex, String key, String field, String value)
	{
		String logFlag = getClassName() + ".hset";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		boolean res = false;

		try
		{
			jds = jedisPool.getResource();
			jds.hset(key, field, value);
			res = true;
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！ parameters:  key {}; field {},value {}; detail:  {}", logIndex, logFlag, key, field, value, e);
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		
	}

	/**
	 * 查询hash中某个域是否存在
	 *
	 * @param String key 待查询的key
	 * @param String field 待查询的field
	 * @return 查询成功 result值为：ReturnValues.SUCCESS，
	 * 此时如果field存在，则value为true，field不存在value为false
	 * 如果查询中出现了异常result值为ThriftRes.EXCEPTION, value值为false
	 */
	public ResBool hexists(long logIndex, String key, String field)
	{
		String logFlag = getClassName() + ".hexists";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		ResBool res = null;
		try
		{
			jds = jedisPool.getResource();
			boolean resv = jds.hexists(key, field);
			res = new ResBool(ReturnValues.SUCCESS, resv, null);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			jedisPool.returnBrokenResource(jds);
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}; field {}, detail:  {}", logIndex, logFlag, key, field, e);
			res = new ResBool(ReturnValues.INNER_ERROR, false, null);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
	
	}

	public boolean hmset(long logIndex, String key, Map<String, String> hash)
	{
		String logFlag = getClassName() + ".hmset";
		Jedis jds = null;
		boolean res = false;
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		try
		{
			jds = jedisPool.getResource();
			jds.hmset(key, hash);
			res = true;
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}; hash {}, detail:  {}", logIndex, logFlag, key, hash.toString(), e);
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return res;
		}
		
	}

	public Map<String, String> hgetAll(long logIndex, String key)
	{
		String logFlag = getClassName() + ".hgetAll";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		Map<String, String> res = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.hgetAll(key);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return ((res == null || res.isEmpty()) ? null : res);

		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}; detail:  {}", logIndex, logFlag, key, e);
			jedisPool.returnBrokenResource(jds);
			return null;
		}

			}

	public List<String> hmget(long logIndex, String key, String[] fields)
	{
		String logFlag = getClassName() + ".hmget";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		List<String> res = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.hmget(key, fields);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return ((res == null || res.isEmpty()) ? null : res);

		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}, fields {}; detail:  {}", logIndex, logFlag, key, java.util.Arrays.toString(fields),
					e);
			jedisPool.returnBrokenResource(jds);
			return null;
		}
	}

	public boolean hdel(long logIndex, String key, String field)
	{
		String logFlag = getClassName() + ".hdel";
		Jedis jds = null;
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		try
		{
			jds = jedisPool.getResource();
			jds.hdel(key, field);
			jedisPool.returnResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return true;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}, field {}; detail:  {}", logIndex, logFlag, key, field, e);
			jedisPool.returnBrokenResource(jds);
			//metrics数据统计结束
			mTimer.stop();
			return false;
		}
		
	}

	public boolean set(long logIndex, String key, String value)
	{
		String logFlag = getClassName() + ".set";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, "set");

		Jedis jds = null;
		try
		{
			jds = jedisPool.getResource();
			jds.set(key, value);
			jedisPool.returnResource(jds);
			mTimer.stop();
			return true;
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}] Redis exeception！ parameters: key {}, value {}; detail:\n {}", logIndex, logFlag, key, value, e);
			jedisPool.returnBrokenResource(jds);
			mTimer.stop();
			return false;
		}
	}
	
	public long expired(long logIndex, String key, int seconds)
	{
		String logFlag = getClassName() + ".expired";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);
		Long res = null;
		Jedis jds = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.expire(key, seconds);
			jedisPool.returnResource(jds);
			mTimer.stop();
			return res;
		}
		catch (Exception e)
		{
			jedisPool.returnBrokenResource(jds);
			logger.error("[lid:{}] [{}] Redis set expired exeception！ parameters: key {}, detail:\n {}", logIndex, logFlag, key, e);
			mTimer.stop();
			res = (long) 0;
			return res;
		}
	}
	
	public String get(long logIndex, String key)
	{
		String logFlag = getClassName() + ".get";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, "get");

		Jedis jds = null;
		String res = null;
		try
		{
			jds = jedisPool.getResource();
			res = jds.get(key);
			jedisPool.returnResource(jds);
		}
		catch (Exception e)
		{
			logger.warn("[lid:{}] [{}] Redis exeception！ parameters: key {}; detail:  {}", logIndex, logFlag, key, e);
			res = null;
			jedisPool.returnBrokenResource(jds);
		}
		//metrics数据统计结束
		mTimer.stop();
		return res;
	}
	
	/**
	 * 判断指定key是否在redis中存在，
	 * 如果查询正常，则result为ThriftRes.SUCCESS，结果放在valuez中，即如果存在为true，不存在为false
	 * 如果查询过程出现异常，则result为ThriftRes.EXCEPTION，value为false
	 * 简而言之：只有key在redis中时，value才为true，其他情况都是false，而result则返回操作解雇的原因
	 */
	public boolean exists(long logIndex, String key)
	{
		String logFlag = getClassName() + ".exists";
		//metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(RedisClient.class, logFlag);

		Jedis jds = null;
		boolean value = false;
		try
		{
			jds = jedisPool.getResource();
			value = jds.exists(key);

			jedisPool.returnResource(jds);
			mTimer.stop();
			return value;
		}
		catch (Exception e)
		{
			jedisPool.returnBrokenResource(jds);
			logger.warn("[lid:{}] [{}] Redis exeception！ parameters: key {}; detail:  {}", logIndex, logFlag, key, e);
			mTimer.stop();
			return false;
		}
	}
}
