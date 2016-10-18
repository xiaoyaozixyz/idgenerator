package com.xyz.idgen.common;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/*
 * 通用工具类，主要提供通用的工具操作，例如检查字符串，检查端口等
 * */
public class Utils
{
	private static Logger m_logger = LoggerFactory.getLogger(Utils.class);
	private static final int MIN_PORT = 1024;
	private static final int MAX_PORT = 65535;
	/**
	 * ID 生成器内部自己使用的id生成器
	 * */
	private static AtomicLong selfUsedIdSource = new AtomicLong(1000);
	/**
	 * 函数名：checkString
	 * 
	 * @author Jason.hou 函数功能：检查字符串是否有效，判断依据是其是否不为空且长度不为0
	 * @param String
	 *            param 待检查的字符串
	 * @return boolean 如果被检查的字符串为空或者长度为0则返回失败，否则返回成功，
	 * */
	public static boolean checkString(String param)
	{
		return param != null && param.length() > 0;
	}

	/**
	 * 获取内部使用的ID
	 * */
	public static long getSelfId()
	{
		return selfUsedIdSource.incrementAndGet();
	}
	/**
	 * 函数名：checkStringValue
	 * 
	 * @author joniers.jia 函数功能：检查字符串值是否有效，判断依据可以进行正确的正则表达式匹配，主要用于校验web接口参数
	 * @param String
	 *            param 待检查的字符串
	 * @return boolean 如果被检查的字符串不符合要求返回失败，否则返回成功
	 * */
	public static boolean checkStringValue(String param, String regExString) throws PatternSyntaxException
	{
		Pattern p = Pattern.compile(regExString);
		Matcher m = p.matcher(param);
		return m.matches();
	}

	/**
	 * 函数名：checkPort 函数功能：检查端口是否有效
	 * 
	 * @author Jason.hou
	 * @param int port 待检查的端口号码
	 * @return boolean 如果端口号在有效范围内，即[MIN_PORT,MAX_PORT]则返回成功，否则返回失败
	 * */
	public static boolean checkPort(int port)
	{
		return port >= MIN_PORT && port <= MAX_PORT;
	}

	public static JSONObject str2Json(long logIndex, String str)
	{
		String logFlag = "Utils.str2Json";
		try
		{
			return JSONObject.parseObject(str);
		}
		catch (Exception ex)
		{
			m_logger.error("[lid:{}] [{}] exception! String: {} , detail:{}", logIndex, logFlag, str, ex);
			return null;
		}
	}

	public static int getMin(int firstVal, int secVal)
	{
		return firstVal > secVal ? secVal : firstVal;
	}



	/**
	 * 获取本机hostname
	 * 
	 * @param logIndex
	 *            long 日志索引
	 * @return hostName 失败则返回空字符串
	 * */
	public static String getHostName(long logIndex)
	{
		String logFlag = "Utils.getHostName";

		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			m_logger.error("[lid:{}] [{}] exception happened! detail {}", logIndex, logFlag, e);
			return "";
		}

	}
	/**
	 * 函数名：checkCollection 函数功能：检查集合是否有效
	 * 
	 * @author joniers.jia
	 * @param Collection<T> collection 待检查的集合
	 * @return boolean 如果集合有效，则返回成功，否则返回失败
	 * */
	public static <T> boolean checkCollection(Collection<T> collection)
	{
		if (collection == null || collection.isEmpty())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * 函数名：checkServerMode 函数功能：检查服务器模式是否有效
	 * 
	 * @author joniers.jia
	 * @param checkServerMode(int mode) checkServerMode 待检查的集合
	 * @return boolean 如果有效，则返回成功，否则返回失败
	 * */
	public static boolean checkServerMode(int mode)
	{
		if (mode == DefaultValues.SERVER_MODE_NONBLOCK || mode == DefaultValues.SERVER_MODE_THREAD_POOL || mode == DefaultValues.SERVER_MODE_THREADEDSELECTOR)
		{
			return true;
		}
		return false;
	}

	
	public static Long str2long(long logIndex, String longStr)
	{
		String logFlag = "Utils.str2long";
		if(!Utils.checkString(longStr))
			return 0l;
		try{
			return Long.parseLong(longStr);
		}catch(Exception ex)
		{
			m_logger.error("[lid:{}] [{}] exception! String: {} , detail:{}", logIndex, logFlag, longStr, ex);
			return 0l;
		}
	}
}
