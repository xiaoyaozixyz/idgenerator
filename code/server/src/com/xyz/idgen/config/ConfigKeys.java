package com.xyz.idgen.config;

public class ConfigKeys
{
	//	配置文件里面用到的配置参数的key值
	public static String THRIFT_LISTEN_PORT = "thrift.listen.port";
	public static String THRIFT_SERVER_MODE = "thrift.server.mode";	
	public static String THRIFT_THREAD_NUM_LISTEN = "thrift.threadNum.listen";
	public static String THRIFT_THREAD_NUM_WORKER = "thrift.threadNum.worker";
	
	//metrics相关配置参数的key
	public static String METRICS_START = "metrics.start";
	public static String METRICS_LOGGAP = "metrics.logGap";
	
	//telnet相关配置的参数的key
	public static String TELNET_PORT = "telnet.port";
	public static final String SERVICE_NAME = "service.name";
	
	//JMX相关配置的参数的key
	public static String JMX_WEB_PORT = "jmx.web.port";
	public static String JMX_RMI_PORT = "jmx.rmi.port";
	public static String JMX_RMI_URL_PREX = "jmx.rmi.url.prefix";
	
	public static final String IDGEN_ID_NUM = "idGen.id.num";
	public static final String DB_HOST = "db.host";
	public static final String DB_PORT = "db.port";
	public static final String DB_DB_NAME = "db.dbName";
	public static final String DB_PASSWORD = "db.password";
	public static final String DB_USER_NAME = "db.userName";

	
	
	
}
