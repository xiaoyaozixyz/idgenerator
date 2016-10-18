package com.xyz.idgen.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.alibaba.fastjson.JSONObject;

public class BasicException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2197614540140726886L;
	/**
	 * 请求id
	 */
	private long logIndex;
	/**
	 * 错误码
	 */
	private int errCode;
	/**
	 * 错误描述
	 */
	private String message;
	/**
	 * 异常信息
	 */
	private Throwable excepObj;
	
	public BasicException(long logIndex, int errCode, String message, Throwable excepObj)
	{
		this.logIndex = logIndex;
		this.errCode = errCode;
		this.message = message;
		this.excepObj = excepObj;
	}
	
	public String toString()
	{
		JSONObject jsRes = new JSONObject();
		jsRes.put("logIndex", logIndex);
		jsRes.put("errCode", errCode);
		jsRes.put("message", message);
		jsRes.put("excepObj", getStackTrace(excepObj));
		return jsRes.toJSONString();
	}
	
	public static String getStackTrace(Throwable anexcepObj)
	{
		StringWriter sw = null;
		PrintWriter printWriter = null;
		try{
			if(anexcepObj != null)
			{
				sw = new StringWriter();
				printWriter = new PrintWriter(sw);
				anexcepObj.printStackTrace(printWriter);
				printWriter.flush();
				sw.flush();
				return sw.toString();
			}
			else
				return null;
		}finally
		{
			
			try
			{
				if(sw != null)
					sw.close();
				if(printWriter != null)
					printWriter.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
