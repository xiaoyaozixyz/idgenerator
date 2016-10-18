package com.xyz.idgen.common;

public class DefaultValues
{

	public static final int METRICS_LOGGAP = 10000;// metrics的输出间隔
	// 默认的两个日志索引
	public static long LOG_INDEX_0 = 0;
	public static long LOG_INDEX_INIT = -2;
	public static long LOG_INDEX_START = 1;// 日志索引的起始值
	public static long LOG_INDEX_TEST = -1;// 自测时使用

	public static int THRIFT_THREAD_NUM_LISTENER = 3;// thrift监听线程数
	public static int THRIFT_THREAD_NUM_WORKER = 10;// thrift工作线程数
	public static int SERVER_MODE_THREAD_POOL = 1;
	public static int SERVER_MODE_NONBLOCK = 2;
	public static int SERVER_MODE_THREADEDSELECTOR = 3;
	
	public static int DB_MIN_POOL_SIZE = 3;
	public static int DB_MAX_POOL_SIZE = 10;
	public static int DB_MAX_IDLE_TIME = 3600;
	public static int DB_MAX_STATEMENTS = 50;
	public static int DB_CHECKOUT_TIMEOUT = 3000;
	public static int DB_ACQUIRE_RETRY_DELAY = 1000;
	public static int DB_ACQUIRE_RETRY_ATTEMPT = 3;
	public static long ID_ERROR = -1;// 错误的ID
	
	public static int ID_STATE_VALID = 1;
	public static int ID_STATE_DEL = 2;
	public static int ID_STATE_OUT_OF_RANGE = 3;

	public static final String CACHE_ID_PREFIX = "idg.";//id名称的全局前缀
	public static final String CACHE_ID_VALUE_SUFFIX = ".v";//id值的后缀
	public static final String CACHE_ID_PROPERTY_SUFFIX = ".i";//id属性的后缀
	public static final String CACHE_ID_LOCK_SUFFIX = ".l";//id分布式锁的后缀
	public static final String CACHE_ID_LOCK_VALUE = "isusing";//id分布式锁的值
	
	public static long LOG_INDEX_JMX = -100;//JMX调用时默认日志索引
	public static final String JMX_CALLER = "JMX";//JMX调用时默认caller
	
	public static final String ID_INFO_NAME = "idName";
	public static final String ID_INFO_PROJECT  = "idProject";
	public static final String ID_INFO_PREFIX = "idPrefix";
	public static final String ID_INFO_ID_START = "idStart";
	public static final String ID_INFO_ID_END = "idEnd";
	public static final String ID_INFO_ID_UPDATE = "idUpdate";
	public static final String ID_INFO_CUR_RANGE = "curRange";
	public static final String ID_INFO_MAX_RANGE = "maxRange";
	public static final String ID_INFO_MIN_RANGE = "minRange";
	public static final String ID_INFO_LOAD_TIME = "loadTime";
	public static final String ID_INFO_APPLICANT = "applicant";
	public static final String ID_INFO_REDIS_FLAG = "redisFlag";
	public static final String ID_INFO_STATE = "state";
	public static final String ID_INFO_OTHERS = "others";
	public static final String ID_INFO_MIN_LOAD_INTERVAL = "minLoadInterval";
	public static final String ID_INFO_LOAD_PERCENTAGE = "loadPercentage";
	public static final String ID_INFO_LOCK_EXPIER = "lockExpire";
	public static final long THRIFT_MAX_READ_BUF = 16384000L;//thrift 内部申请处理socket buffer数据缓冲区大小
	public static final long REDIS_SETNX_SUC = 1l;
	public static final long REDIS_SETNX_FAIL = 0l;
	public static final int NUM_HANDLER_THREAD = 5;
	public static final double NEW_ID_SECTION_SCALING_RATIO = 1.5;
	public static final int REDIS_LOCK_EXPIRE = 120;//second
	public static final long ID_RANGE = 100000l;//默认加载ID的段长；
	
}
