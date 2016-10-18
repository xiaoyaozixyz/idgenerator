package com.xyz.idgenclient.common;

public class DefaultValues
{
	public static final String CHARSET = "UTF-8";
	public static final int METRICS_LOGGAP = 10000;//metrics的输出间隔
	
	public static int THRIFT_THREAD_NUM_LISTENER = 10;//thrift监听线程数
	public static int THRIFT_THREAD_NUM_WORKER = 50;//thrift工作线程数
	public static int SERVER_MODE_THREAD_POOL = 1;
	public static int SERVER_MODE_NONBLOCK = 2;
	public static int SERVER_MODE_THREADEDSELECTOR = 3;

	public static long ID_ERROR = -1;//错误的ID
	
}
