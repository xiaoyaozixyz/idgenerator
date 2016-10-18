package com.xyz.idgenclient.common;

/**
 * Created by liuxiaohu on 2015/10/9.
 */
public class ReturnValues
{

	public static final String VERSION = "1.0.0";
	/**
	 * 版本1.0.0是初始版本：
	 * 注：ThriftRes的EXCEPTION、INDEX_ERROR、UNKNOWN_ERROR、INNER_ERROR统一归为INNER_ERROR：553
	 * */

	public static int SUCCESS = 200;				/*完全成功*/
	public static int SUC_PARTIAL = 201;			/*部分成功*/
	public static int SERVER_UNWORKING = 550;		/*服务器处于非Working状态*/
	public static int NO_CONTENT = 551;				/*请求结果不存在*/
	public static int PARAMETER_ERROR = 552;		/*参数错误*/
	public static int INNER_ERROR = 553;			/*内部错误*/
}
