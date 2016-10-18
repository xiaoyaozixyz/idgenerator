package com.xyz.idgen.mainframe;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.idgen.common.DefaultValues;
import com.xyz.idgen.common.MetricsTool;
import com.xyz.idgen.common.ReturnValues;
import com.xyz.idgen.common.Utils;
import com.xyz.idgen.config.Config;
import com.xyz.idgen.datacenter.DataCenter;
import com.xyz.idgen.thrift.stub.IdGenService;
import com.xyz.thrift.datatype.ResBool;
import com.xyz.thrift.datatype.ResLong;
import com.xyz.thrift.datatype.ResStr;

//import com.xdja.thrift.datatype.ReturnValues;
public class IdGen implements IdGenService.Iface
{
	private String getClassName()
	{
		return "IdGen";
	}

	private static Logger logger = LoggerFactory.getLogger(IdGen.class);
	private DataCenter dataCenter = null;
	private boolean isWorking = false;

	public boolean init(long logIndex, Config cfg)
	{
		String logFlag = getClassName() + ".init";
		isWorking = false;
		dataCenter = new DataCenter();
		if (!dataCenter.init(logIndex, cfg))
		{
			logger.error("[lid:{}] [{}] initialize mainHandler fail!", logIndex, logFlag);
			return false;
		}
		logger.info("[lid:{}] [{}] idgs initialized success!", logIndex, logFlag);
		isWorking = true;
		return true;
	}
	
	public boolean getWorkingStat(long logIndex)
	{
		return isWorking;
	}

	/**
	 * 函数名称：getId 函数功能：获取一个指定类型的ID
	 * 
	 * @author houjixin
	 * @param String 	caller 调用方的标识
	 * @param String	idName id的类型
	 * @return ResLong 成功获取到ID时，result值为ReturnValues.SUCCESS，value值为获取到的id，
	 *         获取失败时，返回各失败的状态描述如下：
	 *         （1）id生成器没有处于工作状态，则result返回ReturnValues.SERVER_UNWORKING，value值为
	 *         -1 （2）传入参数错误时，则result返回ReturnValues.PARAMETER_ERROR，value值为 -1
	 *         （3）id生成器内部获取不到ID时，例如id生成器与redis之间断开连接，则result返回ReturnValues.
	 *         INNER_ERROR，value值为 -1
	 *         （4）内部发送异常时，则result返回ReturnValues.EXCEPTION，value值为 -1
	 * */
	@Override
	public ResLong getId(String caller, String idName) throws TException
	{
		long logIndex = Utils.getSelfId();
		String logFlag = getClassName() + ".getId";
		logger.info("[lid:{}] [{}] caller:{}, idName:{}", logIndex, logFlag, caller, idName);
		
		if (!isWorking)
		{
			logger.error("[lid:{}] [{}] id generator is unworking! idName:{}", logIndex, logFlag, idName);
			return new ResLong(ReturnValues.SERVER_UNWORKING, DefaultValues.ID_ERROR, null);
		}

		if (!Utils.checkString(idName))
		{
			logger.error("[lid:{}] [{}] parameter error idName:{}", logIndex, logFlag, idName);
			return new ResLong(ReturnValues.PARAMETER_ERROR, DefaultValues.ID_ERROR, null);
		}
		// metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(IdGen.class, logFlag);
		try
		{
			ResLong res = null;
			long id = dataCenter.getId(logIndex, idName);
			if (id == DefaultValues.ID_ERROR)
			{
				res = new ResLong(ReturnValues.INNER_ERROR, DefaultValues.ID_ERROR, null);
				logger.error("[lid:{}] [{}] result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			}
			else
			{
				res = new ResLong(ReturnValues.SUCCESS, id, null);
			}

			logger.info("[lid:{}] [{}] result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			return res;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}] [{}]<< idName:{},exception happened! detail:{}", logIndex, logFlag, idName, ex);
			return new ResLong(ReturnValues.INNER_ERROR, DefaultValues.ID_ERROR, null);
		}
		finally
		{
			mTimer.stop();
		}

	}

	/**
	 * 函数名称：getStrId 函数功能：获取一个字符串格式的指定类型的ID，可以使用默认的前缀
	 * 
	 * @author houjixin
	 * @param String 	caller 调用方的标识
	 * @param String   	idName id的类型
	 * @param boolean usePrefix 是否使用默认前缀
	 * @return ResStr 成功获取到ID时，result值为ReturnValues.SUCCESS，value值为获取到的id，
	 *         获取失败时，返回各失败的状态描述如下：
	 *         （1）id生成器没有处于工作状态，则result返回ReturnValues.SERVER_UNWORKING
	 *         ，value值为null
	 *         （2）传入参数错误时，则result返回ReturnValues.PARAMETER_ERROR，value值为 null
	 *         （3）id生成器内部获取不到ID时
	 *         ，例如id生成器与redis之间断开连接，则result返回ReturnValues.INNER_ERROR，value值为
	 *         null （4）内部发送异常时，则result返回ReturnValues.EXCEPTION，value值为 null
	 * */
	@Override
	public ResStr getStrId(String caller, String idName, boolean usePrefix) throws TException
	{
		long logIndex = Utils.getSelfId(); 
		String logFlag = getClassName() + ".getStrId";
		logger.info("[lid:{}] [{}]>> caller:{}, idName:{}", logIndex, logFlag, caller, idName);

		if (!isWorking)
		{
			logger.error("[lid:{}] [{}]<< id generator is unworking! idName:{}", logIndex, logFlag, idName);
			return new ResStr(ReturnValues.SERVER_UNWORKING, null, null);
		}

		if (!Utils.checkString(idName))
		{
			logger.error("[lid:{}] [{}]<< parameter error idName:{}", logIndex, logFlag, idName);
			return new ResStr(ReturnValues.PARAMETER_ERROR, null, null);
		}
		// metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(IdGen.class, logFlag);
		try
		{
			ResStr res = null;
			String id = dataCenter.getStrId(logIndex, idName, usePrefix);
			if (!Utils.checkString(id))
				res = new ResStr(ReturnValues.INNER_ERROR, null, null);
			else
				res = new ResStr(ReturnValues.SUCCESS, id, null);

			logger.info("[lid:{}] [{}]<< result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			return res;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}] [{}]<< idName:{},exception happened! detail:{}", logIndex, logFlag, idName, ex);
			return new ResStr(ReturnValues.INNER_ERROR, null, null);
		}
		finally
		{
			mTimer.stop();
		}
	}

	public void shutdown(long logIndex)
	{
		dataCenter.shutdown(logIndex);
	}

	@Override
	public ResStr echo(String caller, String srcStr) throws TException
	{
		long logIndex = Utils.getSelfId();
		String logFlag = "IdGen.echo";
		if(logger.isDebugEnabled())
			logger.debug("[lid:{}] [{}]>> caller:{}, srcStr:{}", logIndex, logFlag, caller, srcStr);

		if (!isWorking)
		{
			logger.error("[lid:{}] [{}] service is unworking!", logIndex, logFlag);
			return new ResStr(ReturnValues.SERVER_UNWORKING, null, null);
		}

		if (!Utils.checkString(caller))
		{
			logger.error("[lid:{}] [{}] parameter error!", logIndex, logFlag);
			return new ResStr(ReturnValues.PARAMETER_ERROR, null, null);
		}
		// metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(IdGen.class, logFlag);
		try
		{
			if(logger.isDebugEnabled())
				logger.debug("[lid:{}] [{}]<< res:{}", logIndex, logFlag, srcStr);
			return new ResStr(ReturnValues.SUCCESS, srcStr, null);
		}
		catch (Exception e)
		{
			logger.error("[lid:{}] [{}]<< exception happened! detail:{}", logIndex, logFlag, e);
			return new ResStr(ReturnValues.INNER_ERROR, null, null);
		}
		finally
		{
			mTimer.stop();
		}
	}

	public String getAllIdInfo(long logIndex)
	{
		return dataCenter.getAllIdInfo(logIndex);
	}

	public String getid(long logIndex, String idName)
	{
		return String.valueOf(dataCenter.getId(logIndex, idName));
	}

	@Override
	public ResBool addNewId(String caller, String newIdInfo) throws TException
	{
		long logIndex = Utils.getSelfId(); 
		String logFlag = getClassName() + ".addNewId";
		logger.info("[lid:{}] [{}]>> caller:{}, newIdInfo:{}", logIndex, logFlag, newIdInfo);

		if (!isWorking)
		{
			logger.error("[lid:{}] [{}]<< id generator is unworking!", logIndex, logFlag);
			return new ResBool(ReturnValues.SERVER_UNWORKING, false, null);
		}

		if (!Utils.checkString(newIdInfo))
		{
			logger.error("[lid:{}] [{}]<< parameter error! newIdInfo:{}", logIndex, logFlag, newIdInfo);
			return new ResBool(ReturnValues.PARAMETER_ERROR, false, null);
		}
		// metrics数据统计开始
		MetricsTool.MetricsTimer mTimer = MetricsTool.getAndStartTimer(IdGen.class, logFlag);
		try
		{
			ResBool res = null;
			boolean localRes = dataCenter.addNewId(logIndex, newIdInfo);
			if (!localRes)
				res = new ResBool(ReturnValues.INNER_ERROR, localRes, null);
			else
				res = new ResBool(ReturnValues.SUCCESS, localRes, null);

			logger.info("[lid:{}] [{}]<< result:{}, value:{}", logIndex, logFlag, res.res, res.value);
			return res;
		}
		catch (Exception ex)
		{
			logger.error("[lid:{}] [{}]<< exception happened! detail:{}", logIndex, logFlag, ex);
			return new ResBool(ReturnValues.INNER_ERROR, false, null);
		}
		finally
		{
			mTimer.stop();
		}
	}

}
